/*
 *  JavaSMT is an API wrapper for a collection of SMT solvers.
 *  This file is part of JavaSMT.
 *
 *  Copyright (C) 2007-2019  Dirk Beyer
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.sosy_lab.java_smt.solvers.cvc4;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import edu.nyu.acsys.CVC4.Expr;
import edu.nyu.acsys.CVC4.ExprManager;
import edu.nyu.acsys.CVC4.ExprManagerMapCollection;
import edu.nyu.acsys.CVC4.Result;
import edu.nyu.acsys.CVC4.SExpr;
import edu.nyu.acsys.CVC4.SmtEngine;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.java_smt.api.BasicProverEnvironment;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;
import org.sosy_lab.java_smt.api.Model.ValueAssignment;
import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.SolverContext.ProverOptions;
import org.sosy_lab.java_smt.api.SolverException;
import org.sosy_lab.java_smt.basicimpl.AbstractProverWithAllSat;

class CVC4TheoremProver extends AbstractProverWithAllSat<Void>
    implements ProverEnvironment, BasicProverEnvironment<Void> {

  private final CVC4FormulaCreator creator;
  private SmtEngine smtEngine;
  private boolean changedSinceLastSatQuery = false;

  protected final AtomicBoolean interrupted = new AtomicBoolean(false);

  /** Tracks formulas on the stack, needed for model generation. */
  protected final Deque<List<Expr>> assertedFormulas = new ArrayDeque<>();

  /**
   * Tracks provided models to inform them when the SmtEngine is closed. We can no longer access
   * model evaluation after closing the SmtEngine.
   */
  private final Set<CVC4Model> models = new LinkedHashSet<>();

  /**
   * The local exprManager allows to set options per Prover (and not globally). See <a
   * href="https://github.com/CVC4/CVC4/issues/3055">Issue 3055</a> for details.
   *
   * <p>TODO If the overhead of importing/exporting the expressions is too expensive, we can disable
   * this behavior. This change would cost us the flexibility of setting options per Prover.
   */
  private final ExprManager exprManager = new ExprManager();

  /** We copy expression between different ExprManagers. The map serves as cache. */
  private final ExprManagerMapCollection exportMapping = new ExprManagerMapCollection();

  // CVC4 does not support separation login in incremental mode.
  private final boolean incremental;

  protected CVC4TheoremProver(
      CVC4FormulaCreator pFormulaCreator,
      ShutdownNotifier pShutdownNotifier,
      int randomSeed,
      Set<ProverOptions> pOptions,
      BooleanFormulaManager pBmgr) {
    super(pOptions, pBmgr, pShutdownNotifier);

    creator = pFormulaCreator;
    smtEngine = new SmtEngine(exprManager);
    incremental = !enableSL;
    assertedFormulas.push(new ArrayList<>()); // create initial level

    setOptions(randomSeed, pOptions);
    registerShutdownHandler(pShutdownNotifier);
  }

  private void setOptions(int randomSeed, Set<ProverOptions> pOptions) {
    smtEngine.setOption("incremental", new SExpr(incremental));
    if (pOptions.contains(ProverOptions.GENERATE_MODELS)) {
      smtEngine.setOption("produce-models", new SExpr(true));
    }
    if (pOptions.contains(ProverOptions.GENERATE_UNSAT_CORE)) {
      smtEngine.setOption("produce-unsat-cores", new SExpr(true));
    }
    smtEngine.setOption("produce-assertions", new SExpr(true));
    smtEngine.setOption("dump-models", new SExpr(true));
    // smtEngine.setOption("produce-unsat-cores", new SExpr(true));
    smtEngine.setOption("output-language", new SExpr("smt2"));
    smtEngine.setOption("random-seed", new SExpr(randomSeed));
  }

  protected void setOptionForIncremental() {
    smtEngine.setOption("incremental", new SExpr(true));
  }

  // Due to a bug in CVC4, smtEngine.interrupt() has no effect when it is called too soon.
  // For example in the case of smtEngine.checkSat(), this is if interrupt() is called
  // before the line "Result result = d_propEngine->checkSat();" is called in the CVC4 C++
  // method SmtEngine::check(), which seems to take about 10 ms. When this is fixed in
  // CVC4, we can remove the Thread.sleep(10), the AtomicBoolean interrupted and the while
  // loop surrounding this block.
  private void registerShutdownHandler(ShutdownNotifier pShutdownNotifier) {
    pShutdownNotifier.register(
        (reason) -> {
          interrupted.set(true);
          while (interrupted.get()) {
            smtEngine.interrupt();
            try {
              Thread.sleep(10);
            } catch (InterruptedException e) {
            }
          }
        });
  }

  /** import an expression from global context into this prover's context. */
  protected Expr importExpr(Expr expr) {
    return expr.exportTo(exprManager, exportMapping);
  }

  /** export an expression from this prover's context into global context. */
  protected Expr exportExpr(Expr expr) {
    return expr.exportTo(creator.getEnv(), exportMapping);
  }

  @Override
  public void push() {
    Preconditions.checkState(!closed);
    setChanged();
    assertedFormulas.push(new ArrayList<>());
    if (incremental) {
      smtEngine.push();
    }
  }

  @Override
  public void pop() {
    Preconditions.checkState(!closed);
    setChanged();
    assertedFormulas.pop();
    Preconditions.checkState(assertedFormulas.size() > 0, "initial level must remain until close");
    if (incremental) {
      smtEngine.pop();
    }
  }

  @Override
  public @Nullable Void addConstraint(BooleanFormula pF) throws InterruptedException {
    Preconditions.checkState(!closed);
    setChanged();
    Expr exp = creator.extractInfo(pF);
    assertedFormulas.peek().add(exp);
    if (incremental) {
      smtEngine.assertFormula(importExpr(exp));
    }
    return null;
  }

  @Override
  public CVC4Model getModel() {
    Preconditions.checkState(!closed);
    checkGenerateModels();
    return getModelWithoutChecks();
  }

  @Override
  protected CVC4Model getModelWithoutChecks() {
    Preconditions.checkState(!changedSinceLastSatQuery);
    CVC4Model model = new CVC4Model(this, creator, smtEngine, getAssertedExpressions());
    models.add(model);
    return model;
  }

  void unregisterModel(CVC4Model model) {
    models.remove(model);
  }

  private void setChanged() {
    if (!changedSinceLastSatQuery) {
      changedSinceLastSatQuery = true;
      closeAllModels();
      if (!incremental) {
        // create a new clean smtEngine
        smtEngine = new SmtEngine(exprManager);
        registerShutdownHandler(shutdownNotifier);
      }
    }
  }

  /**
   * whenever the SmtEngine changes, we need to invalidate all models.
   *
   * <p>See for details <a href="https://github.com/CVC4/CVC4/issues/2648">Issue 2648</a> .
   */
  private void closeAllModels() {
    for (CVC4Model model : ImmutableList.copyOf(models)) {
      model.close();
    }
    Preconditions.checkState(models.isEmpty(), "all models should be closed");
  }

  @Override
  public ImmutableList<ValueAssignment> getModelAssignments() throws SolverException {
    Preconditions.checkState(!closed);
    Preconditions.checkState(!changedSinceLastSatQuery);
    try (CVC4Model model = getModel()) {
      return model.toList();
    }
  }

  @Override
  public boolean isUnsat() throws InterruptedException, SolverException {
    Preconditions.checkState(!closed);
    closeAllModels();
    changedSinceLastSatQuery = false;
    if (!incremental) {
      for (Expr expr : getAssertedExpressions()) {
        smtEngine.assertFormula(importExpr(expr));
      }
    }
    Result result = smtEngine.checkSat();
    if (interrupted.get()) {
      interrupted.set(false); // Should we throw InterruptException?
    }
    if (result.isUnknown()) {
      if (result.whyUnknown().equals(Result.UnknownExplanation.INTERRUPTED)) {
        throw new InterruptedException();
      } else {
        throw new SolverException("CVC4 returned null or unknown on sat check (" + result + ")");
      }
    }
    if (result.isSat() == Result.Sat.SAT) {
      return false;
    } else if (result.isSat() == Result.Sat.UNSAT) {
      return true;
    } else {
      throw new SolverException("CVC4 returned unknown on sat check");
    }
  }

  @Override
  public List<BooleanFormula> getUnsatCore() {
    Preconditions.checkState(!closed);
    checkGenerateUnsatCores();
    Preconditions.checkState(!changedSinceLastSatQuery);
    List<BooleanFormula> converted = new ArrayList<>();
    for (Expr aCore : smtEngine.getUnsatCore()) {
      converted.add(creator.encapsulateBoolean(exportExpr(aCore)));
    }
    return converted;
  }

  @Override
  public boolean isUnsatWithAssumptions(Collection<BooleanFormula> pAssumptions)
      throws SolverException, InterruptedException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<List<BooleanFormula>> unsatCoreOverAssumptions(
      Collection<BooleanFormula> pAssumptions) throws SolverException, InterruptedException {
    throw new UnsupportedOperationException();
  }

  protected Collection<Expr> getAssertedExpressions() {
    List<Expr> result = new ArrayList<>();
    assertedFormulas.forEach(result::addAll);
    return result;
  }

  @Override
  public void close() {
    if (!closed) {
      closeAllModels();
      assertedFormulas.clear();
      exportMapping.delete();
      // smtEngine.delete();
      exprManager.delete();
      closed = true;
    }
  }
}
