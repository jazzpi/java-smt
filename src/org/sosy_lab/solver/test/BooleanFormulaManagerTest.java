/*
 *  JavaSMT is an API wrapper for a collection of SMT solvers.
 *  This file is part of JavaSMT.
 *
 *  Copyright (C) 2007-2016  Dirk Beyer
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

package org.sosy_lab.solver.test;

import com.google.common.collect.ImmutableSet;
import com.google.common.truth.Truth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.sosy_lab.solver.SolverContextFactory.Solvers;
import org.sosy_lab.solver.SolverException;
import org.sosy_lab.solver.api.BooleanFormula;

@RunWith(Parameterized.class)
public class BooleanFormulaManagerTest extends SolverBasedTest0 {

  @Parameters(name = "{0}")
  public static Object[] getAllSolvers() {
    return Solvers.values();
  }

  @Parameter(0)
  public Solvers solver;

  @Override
  protected Solvers solverToUse() {
    return solver;
  }

  @Test
  public void testConjunctionArgsExtractionEmpty() throws SolverException, InterruptedException {
    BooleanFormula input = bmgr.makeBoolean(true);
    Truth.assertThat(bmgr.toConjunctionArgs(input, false)).isEmpty();
    assertThatFormula(bmgr.and(bmgr.toConjunctionArgs(input, false))).isEquivalentTo(input);
  }

  @Test
  public void testConjunctionArgsExtraction() throws SolverException, InterruptedException {
    BooleanFormula input =
        bmgr.and(bmgr.makeVariable("a"), imgr.equal(imgr.makeNumber(1), imgr.makeVariable("x")));
    Truth.assertThat(bmgr.toConjunctionArgs(input, false))
        .isEqualTo(
            ImmutableSet.of(
                bmgr.makeVariable("a"), imgr.equal(imgr.makeNumber(1), imgr.makeVariable("x"))));
    assertThatFormula(bmgr.and(bmgr.toConjunctionArgs(input, false))).isEquivalentTo(input);
  }

  @Test
  public void testConjunctionArgsExtractionRecursive()
      throws SolverException, InterruptedException {
    BooleanFormula input =
        bmgr.and(
            bmgr.makeVariable("a"),
            bmgr.makeBoolean(true),
            bmgr.and(
                bmgr.makeVariable("b"),
                bmgr.makeVariable("c"),
                bmgr.and(
                    imgr.equal(imgr.makeNumber(2), imgr.makeVariable("y")),
                    bmgr.makeVariable("d"),
                    bmgr.or(bmgr.makeVariable("e"), bmgr.makeVariable("f")))),
            imgr.equal(imgr.makeNumber(1), imgr.makeVariable("x")));
    Truth.assertThat(bmgr.toConjunctionArgs(input, true))
        .isEqualTo(
            ImmutableSet.of(
                bmgr.makeVariable("a"),
                bmgr.makeVariable("b"),
                bmgr.makeVariable("c"),
                imgr.equal(imgr.makeNumber(1), imgr.makeVariable("x")),
                imgr.equal(imgr.makeNumber(2), imgr.makeVariable("y")),
                bmgr.makeVariable("d"),
                bmgr.or(bmgr.makeVariable("e"), bmgr.makeVariable("f"))));
    assertThatFormula(bmgr.and(bmgr.toConjunctionArgs(input, true))).isEquivalentTo(input);
    assertThatFormula(bmgr.and(bmgr.toConjunctionArgs(input, false))).isEquivalentTo(input);
  }

  @Test
  public void testDisjunctionArgsExtractionEmpty() throws SolverException, InterruptedException {
    BooleanFormula input = bmgr.makeBoolean(false);
    Truth.assertThat(bmgr.toDisjunctionArgs(input, false)).isEmpty();
    assertThatFormula(bmgr.or(bmgr.toDisjunctionArgs(input, false))).isEquivalentTo(input);
  }

  @Test
  public void testDisjunctionArgsExtraction() throws SolverException, InterruptedException {
    BooleanFormula input =
        bmgr.or(bmgr.makeVariable("a"), imgr.equal(imgr.makeNumber(1), imgr.makeVariable("x")));
    Truth.assertThat(bmgr.toDisjunctionArgs(input, false))
        .isEqualTo(
            ImmutableSet.of(
                bmgr.makeVariable("a"), imgr.equal(imgr.makeNumber(1), imgr.makeVariable("x"))));
    assertThatFormula(bmgr.or(bmgr.toConjunctionArgs(input, false))).isEquivalentTo(input);
  }

  @Test
  public void testDisjunctionArgsExtractionRecursive()
      throws SolverException, InterruptedException {
    BooleanFormula input =
        bmgr.or(
            bmgr.makeVariable("a"),
            bmgr.makeBoolean(false),
            bmgr.or(
                bmgr.makeVariable("b"),
                bmgr.makeVariable("c"),
                bmgr.or(
                    imgr.equal(imgr.makeNumber(2), imgr.makeVariable("y")),
                    bmgr.makeVariable("d"),
                    bmgr.and(bmgr.makeVariable("e"), bmgr.makeVariable("f")))),
            imgr.equal(imgr.makeNumber(1), imgr.makeVariable("x")));
    Truth.assertThat(bmgr.toDisjunctionArgs(input, true))
        .isEqualTo(
            ImmutableSet.of(
                bmgr.makeVariable("a"),
                bmgr.makeVariable("b"),
                bmgr.makeVariable("c"),
                imgr.equal(imgr.makeNumber(1), imgr.makeVariable("x")),
                imgr.equal(imgr.makeNumber(2), imgr.makeVariable("y")),
                bmgr.makeVariable("d"),
                bmgr.and(bmgr.makeVariable("e"), bmgr.makeVariable("f"))));
    assertThatFormula(bmgr.or(bmgr.toDisjunctionArgs(input, true))).isEquivalentTo(input);
    assertThatFormula(bmgr.or(bmgr.toDisjunctionArgs(input, false))).isEquivalentTo(input);
  }
}
