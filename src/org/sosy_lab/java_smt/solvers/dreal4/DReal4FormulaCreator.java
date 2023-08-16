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

package org.sosy_lab.java_smt.solvers.dreal4;
import static java.lang.Double.parseDouble;
import static org.sosy_lab.java_smt.api.QuantifiedFormulaManager.Quantifier.FORALL;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.math.BigDecimal;
import org.sosy_lab.common.rationals.Rational;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.Formula;
import org.sosy_lab.java_smt.api.FormulaType;
import org.sosy_lab.java_smt.api.FormulaType.FloatingPointType;
import org.sosy_lab.java_smt.api.FunctionDeclarationKind;
import org.sosy_lab.java_smt.api.visitors.FormulaVisitor;
import org.sosy_lab.java_smt.basicimpl.FormulaCreator;
import org.sosy_lab.java_smt.basicimpl.FunctionDeclarationImpl;
import org.sosy_lab.java_smt.solvers.dreal4.DReal4Formula.DReal4BooleanFormula;
import org.sosy_lab.java_smt.solvers.dreal4.DReal4Formula.DReal4IntegerFormula;
import org.sosy_lab.java_smt.solvers.dreal4.DReal4Formula.DReal4RationalFormula;
import org.sosy_lab.java_smt.solvers.dreal4.drealjni.Config;
import org.sosy_lab.java_smt.solvers.dreal4.drealjni.Context;
import org.sosy_lab.java_smt.solvers.dreal4.drealjni.Expression;
import org.sosy_lab.java_smt.solvers.dreal4.drealjni.ExpressionDoubleMap;
import org.sosy_lab.java_smt.solvers.dreal4.drealjni.ExpressionExpressionMap;
import org.sosy_lab.java_smt.solvers.dreal4.drealjni.ExpressionKind;
import org.sosy_lab.java_smt.solvers.dreal4.drealjni.FormulaKind;
import org.sosy_lab.java_smt.solvers.dreal4.drealjni.FormulaSet;
import org.sosy_lab.java_smt.solvers.dreal4.drealjni.Variable;
import org.sosy_lab.java_smt.solvers.dreal4.drealjni.VariableSet;
import org.sosy_lab.java_smt.solvers.dreal4.drealjni.dreal;

public class DReal4FormulaCreator extends FormulaCreator<DRealTerm<?, ?>, Variable.Type, Config,
    DRealTerm<?, ?>> {

  private final Map<String, DRealTerm<Variable, Variable.Type>> variablesCache = new HashMap<>();
  //private final Map<String, DRealTerm<?, ?>> functionCache = new HashMap<>();

  public DReal4FormulaCreator(Config config) {
    super(config, Variable.Type.BOOLEAN, Variable.Type.INTEGER, Variable.Type.CONTINUOUS, null,
        null);
  }

  @Override
  public Variable.Type getBitvectorType(int bitwidth) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Variable.Type getFloatingPointType(FloatingPointType type) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Variable.Type getArrayType(Variable.Type indexType, Variable.Type elementType) {
    throw new UnsupportedOperationException();
  }

  @Override
  public DRealTerm<Variable, Variable.Type> makeVariable(Variable.Type pType, String varName) {
    if (variablesCache.get(varName) == null) {
      DRealTerm<Variable, Variable.Type> var =
          new DRealTerm<>(new Variable(varName, pType), pType,pType);
      variablesCache.put(varName, var);
      return var;
    } else {
      DRealTerm<Variable, Variable.Type> var = variablesCache.get(varName);
      if (var.getVariable().get_type() == pType) {
        return var;
      } else {
        throw new IllegalArgumentException("Symbol name already in use for different type "
            + var.getType());
      }
    }
  }

  @Override
  public DRealTerm<?, ?> extractInfo(Formula pT) {
    return DReal4FormulaManager.getDReal4Formula(pT);
  }

  @Override
  public FormulaType<?> getFormulaType(DRealTerm<?, ?> formula) {
    if (formula.getType() == Variable.Type.BOOLEAN) {
      return FormulaType.BooleanType;
    } else if (formula.getType() == Variable.Type.INTEGER) {
      return FormulaType.IntegerType;
    } else
      return FormulaType.RationalType;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Formula> T encapsulate(FormulaType<T> pType, DRealTerm<?, ?> pTerm) {
    assert FormulaType.IntegerType.equals(pType)
        || (FormulaType.RationalType.equals(pType)
        && FormulaType.IntegerType.equals(getFormulaType(pTerm)))
        || pType.equals(getFormulaType(pTerm))
        : String.format(
        "Trying to encapsulate formula %s of type %s as %s",
        pTerm.to_string(), getFormulaType(pTerm), pType);
    if (pType.isBooleanType()) {
      return (T) new DReal4BooleanFormula(pTerm);
    } else if (pType.isIntegerType()) {
      return (T) new DReal4IntegerFormula(pTerm);
    } else if (pType.isRationalType()) {
      return (T) new DReal4RationalFormula(pTerm);
    }
    throw new IllegalArgumentException("Cannot create formulas of type " + pType + " in dReal.");
  }

  @Override
  public BooleanFormula encapsulateBoolean(DRealTerm<?, ?> pTerm) {
    assert getFormulaType(pTerm).isBooleanType()
        : String.format(
        "%s is not boolean, but %s (%s)", pTerm.to_string(), pTerm.getType(), getFormulaType(pTerm));
    return new DReal4BooleanFormula(pTerm);
  }
  @Override
  public <R> R visit(FormulaVisitor<R> visitor, Formula formula, DRealTerm<?, ?> f) {

    final FunctionDeclarationKind functionKind;
    FormulaKind formulaKind = null;
    ExpressionKind expressionKind = null;
    List<DRealTerm<?, ?>> functionArgs = null;

    if (f.isVar()) {
      return visitor.visitFreeVariable(formula, f.getVariable().to_string());
    } else if (f.isExp()) {
      ExpressionKind kind = f.getExpressionKind();
      if (kind == ExpressionKind.Constant) {
        String s = f.to_string();
        int idx = s.indexOf(".");
        if (idx == -1) {
          return visitor.visitConstant(formula, new BigInteger(s));
        } else {
          return visitor.visitConstant(formula, Rational.ofBigDecimal(BigDecimal.valueOf(parseDouble(s))));
        }
/*        // not possible to create an Expression from a boolean Variable
        if (f.getType() == Variable.Type.INTEGER) {
          return visitor.visitConstant(formula, new BigInteger(f.getExpression().to_string()));
        } else {
          return visitor.visitConstant(formula,
              BigDecimal.valueOf(parseDouble(f.getExpression().to_string())));
        }*/
      } else if (kind == ExpressionKind.Var) {
        return visitor.visitFreeVariable(formula, f.getExpression().to_string());
      } else if (kind == ExpressionKind.IfThenElse) {
        functionKind = FunctionDeclarationKind.ITE;
        expressionKind = ExpressionKind.IfThenElse;
        functionArgs = getExpressionFromITE(f);
      } else if (kind == ExpressionKind.Pow) {
        // the exponent should only be an int i >= 2, because the only way to get an
        // expression with an exponent is to create an expression like (x*x*x), because pow is
        // not supported in JavaSMT or if a variable gets negated, so pow(x,-1)
        DRealTerm<Expression, ExpressionKind> pow;
        Expression lhs = dreal.get_first_argument(f.getExpression());
        Expression rhs = dreal.get_second_argument(f.getExpression());
        double exponent = parseDouble(rhs.to_string());
        if (exponent < 0) {
          functionKind = FunctionDeclarationKind.DIV;
          expressionKind = ExpressionKind.Div;
          pow = new DRealTerm<>(dreal.Divide(Expression.One(), lhs),
              dreal.get_variable(lhs).get_type(), ExpressionKind.Div);
        } else {
          functionKind = FunctionDeclarationKind.MUL;
          expressionKind = ExpressionKind.Mul;
          Expression exp = dreal.Multiply(lhs, lhs);
          for (int i = 2; i < exponent; i++) {
            exp = dreal.Multiply(exp, lhs);
          }
          pow = new DRealTerm<>(exp, dreal.get_variable(lhs).get_type(), ExpressionKind.Mul);
        }
        functionArgs = getExpressionFromMul(pow);
      } else if (kind == ExpressionKind.Add) {
        functionKind = FunctionDeclarationKind.ADD;
        expressionKind = ExpressionKind.Add;
        functionArgs = getExpressionFromAdd(f);
      } else if (kind == ExpressionKind.Mul) {
        functionKind = FunctionDeclarationKind.MUL;
        expressionKind = ExpressionKind.Mul;
        functionArgs = getExpressionFromMul(f);
      } else {
        functionKind = FunctionDeclarationKind.DIV;
        expressionKind = ExpressionKind.Div;
        functionArgs = getExpressionsFromDiv(f);
      }
    } else {
      FormulaKind kind = f.getFormulaKind();
      if (kind == FormulaKind.Forall) {
        VariableSet set = f.getFormula().getQuantifiedVariables();
        List<Formula> boundVariables = new ArrayList<>();
        DRealTerm<Variable, Variable.Type> var;
        Iterator<Variable> iter = set.iterator();
        Variable next;
        for (int i = 0; i < set.size(); i++) {
          next = iter.next();
          var = new DRealTerm<>(next, next.get_type(), next.get_type());
          boundVariables.add(encapsulate(getFormulaType(var), var));
        }
        DRealTerm<org.sosy_lab.java_smt.solvers.dreal4.drealjni.Formula, FormulaKind> quantifiedFormula =
            new DRealTerm<>(dreal.get_quantified_formula(f.getFormula()),
                Variable.Type.BOOLEAN, f.getFormula().get_kind());
        return visitor.visitQuantifier((BooleanFormula) formula, FORALL, boundVariables,
            encapsulateBoolean(quantifiedFormula));
      } else if (kind == FormulaKind.And) {
        functionKind = FunctionDeclarationKind.AND;
        formulaKind = FormulaKind.And;
        functionArgs = getFormulaArgs(f);
      } else if (kind == FormulaKind.Or) {
        functionKind = FunctionDeclarationKind.OR;
        formulaKind = FormulaKind.Or;
        functionArgs = getFormulaArgs(f);
      } else if (kind == FormulaKind.Not) {
        functionKind = FunctionDeclarationKind.NOT;
        formulaKind = FormulaKind.Not;
        functionArgs = getFormulaFromNot(f);
      } else if (kind == FormulaKind.Eq) {
        functionKind = FunctionDeclarationKind.EQ;
        formulaKind = FormulaKind.Eq;
      } else if (kind == FormulaKind.Geq) {
        functionKind = FunctionDeclarationKind.GTE;
        formulaKind = FormulaKind.Geq;
      } else if (kind == FormulaKind.Gt) {
        functionKind = FunctionDeclarationKind.GT;
        formulaKind = FormulaKind.Gt;
      } else if (kind == FormulaKind.Leq) {
        functionKind = FunctionDeclarationKind.LT;
        formulaKind = FormulaKind.Lt;
      } else if (kind == FormulaKind.Lt) {
        functionKind = FunctionDeclarationKind.LTE;
        formulaKind = FormulaKind.Leq;
      } else if (kind == FormulaKind.Neq) {
        // lhs ≠ rhs -> (lhs > rhs) ∨ (lhs < rhs)
        Expression lhs = dreal.get_lhs_expression(f.getFormula());
        Expression rhs = dreal.get_rhs_expression(f.getFormula());
        DRealTerm<org.sosy_lab.java_smt.solvers.dreal4.drealjni.Formula, FormulaKind> neqTerm =
            new DRealTerm<>(
                new org.sosy_lab.java_smt.solvers.dreal4.drealjni.Formula(dreal.Or(dreal.Grater(lhs,
                    rhs), dreal.Less(lhs, rhs))), Variable.Type.BOOLEAN, FormulaKind.Or);
        functionKind = FunctionDeclarationKind.OR;
        formulaKind = FormulaKind.Or;
        functionArgs = getFormulaArgs(neqTerm);
      } else if (kind == FormulaKind.Var) {
        return visitor.visitFreeVariable(formula, f.getFormula().to_string());
      } else if (kind == FormulaKind.True) {
        return visitor.visitConstant(formula, true);
      } else if (kind == FormulaKind.False) {
        return visitor.visitConstant(formula, false);
      } else {
        return visitor.visitFreeVariable(formula, f.getFormula().to_string());
      }
    }

    String functionName = functionKind.toString();
    if (functionArgs == null) {
      functionArgs = getExpressionArgs(f);
    }

    DRealTerm<?, ?> pDeclaration;

    // Variable should be handled above, just to be sure
    Preconditions.checkState(f.isExp() || f.isFormula());
    if (f.isExp()) {
      pDeclaration = new DRealTerm<>(new Expression(), f.getType(), expressionKind);
    } else {
      pDeclaration = new DRealTerm<>(new org.sosy_lab.java_smt.solvers.dreal4.drealjni.Formula(),
          f.getType(), formulaKind);
    }


    final ImmutableList<FormulaType<?>> argTypes = ImmutableList.copyOf(toType(functionArgs));

    final ImmutableList.Builder<Formula> argsBuilder = ImmutableList.builder();
    for (int i = 0; i < functionArgs.size(); i++) {
      argsBuilder.add(encapsulate(argTypes.get(i), functionArgs.get(i)));
    }
    final ImmutableList<Formula> args = argsBuilder.build();

    return visitor.visitFunction(formula, args, FunctionDeclarationImpl.of(functionName,
        functionKind, argTypes, getFormulaType(f), pDeclaration));

  }
  private List<FormulaType<?>> toType(final List<DRealTerm<?, ?>> args) {
    return Lists.transform(args, this::getFormulaType);
  }

  private static List<DRealTerm<?, ?>> getFormulaArgs(DRealTerm<?, ?> pTerm) {
    List<DRealTerm<?, ?>> formulas = new ArrayList<>();
    org.sosy_lab.java_smt.solvers.dreal4.drealjni.Formula formula;

    if (pTerm.isFormula()) {
      FormulaSet set = dreal.get_operands(pTerm.getFormula());
      Iterator<org.sosy_lab.java_smt.solvers.dreal4.drealjni.Formula> iter = set.iterator();
      for (int i = 0; i < set.size(); i++) {
        formula = iter.next();
        formulas.add(new DRealTerm<>(formula, pTerm.getType(), formula.get_kind()));
      }
      return formulas;
    } else {
      throw new IllegalArgumentException("Term is not a Formula.");
    }
  }

  private static List<DRealTerm<?, ?>> getExpressionArgs(DRealTerm<?, ?> parent) {
    List<DRealTerm<?, ?>> children = new ArrayList<>();
    if (parent.isExp()) {
      throw new IllegalArgumentException("Term is Expression.");
    } else if (parent.isFormula()) {
      Variable.Type type = getTypeForExpressions(dreal.get_lhs_expression(parent.getFormula()));
      if (type == null) {
        type = getTypeForExpressions(dreal.get_rhs_expression(parent.getFormula()));
      }
      Expression left = dreal.get_lhs_expression(parent.getFormula());
      Expression right = dreal.get_rhs_expression(parent.getFormula());
      children.add(new DRealTerm<>(left, type, left.get_kind()));
      children.add(new DRealTerm<>(right, type, right.get_kind()));
      return children;
    } else {
      throw new IllegalArgumentException("Term is Variable.");
    }
  }

  private static List<DRealTerm<?, ?>> getFormulaFromNot(DRealTerm<?, ?> pTerm) {
    List<DRealTerm<?, ?>> formula = new ArrayList<>();
    if (pTerm.isVar() || pTerm.isExp()) {
      throw new IllegalArgumentException("Term is Expression or Variable.");
    } else {
      org.sosy_lab.java_smt.solvers.dreal4.drealjni.Formula term = dreal.get_operand(pTerm.getFormula());
      formula.add(new DRealTerm<>(term, Variable.Type.BOOLEAN, term.get_kind()));
      return formula;
    }
  }

  // TODO: use Preconditions in getExpressionFrom...()
  private static List<DRealTerm<?, ?>> getExpressionsFromDiv(DRealTerm<?, ?> pTerm) {
    List<DRealTerm<?, ?>> children = new ArrayList<>();
    if (pTerm.isVar() || pTerm.isFormula()) {
      throw new IllegalArgumentException("Term is Variable or Formula");
    } else {
      Expression first = dreal.get_first_argument(pTerm.getExpression());
      Expression second = dreal.get_second_argument(pTerm.getExpression());
      children.add(new DRealTerm<>(first, pTerm.getType(), first.get_kind()));
      children.add(new DRealTerm<>(second, pTerm.getType(), second.get_kind()));
      return children;
    }
  }

  private static List<DRealTerm<?, ?>> getExpressionFromITE(DRealTerm<?, ?> pTerm) {
    List<DRealTerm<?, ?>> children = new ArrayList<>();
    if (pTerm.isFormula() || pTerm.isVar()) {
      throw new IllegalArgumentException("Term is Variable or Formula");
    } else {
      org.sosy_lab.java_smt.solvers.dreal4.drealjni.Formula cond =
          dreal.get_conditional_formula(pTerm.getExpression());
      Expression expThen = dreal.get_then_expression(pTerm.getExpression());
      Expression expElse = dreal.get_else_expression(pTerm.getExpression());
      children.add(new DRealTerm<>(cond, pTerm.getType(), cond.get_kind()));
      children.add(new DRealTerm<>(expThen, pTerm.getType(), expThen.get_kind()));
      children.add(new DRealTerm<>(expElse, pTerm.getType(), expElse.get_kind()));
      return children;
    }
  }

  private static List<DRealTerm<?, ?>> getExpressionFromAdd(DRealTerm<?, ?> pTerm) {
    List<DRealTerm<?, ?>> terms = new ArrayList<>();
    if (pTerm.isVar() || pTerm.isFormula()) {
      throw new IllegalArgumentException("Term is Variable or Formula.");
    } else {

      terms.add(new DRealTerm<>(new Expression(dreal.get_constant_in_addition(pTerm.getExpression())),
          pTerm.getType(), ExpressionKind.Constant));
      ExpressionDoubleMap map = dreal.get_expr_to_coeff_map_in_addition(pTerm.getExpression());
      for (Map.Entry<Expression, Double> entry : map.entrySet()) {
        terms.add(new DRealTerm<>(new Expression(dreal.Multiply(entry.getKey(),
            new Expression(entry.getValue()))), pTerm.getType(), ExpressionKind.Mul));
      }
      return terms;
    }
  }

  private static List<DRealTerm<?, ?>> getExpressionFromMul(DRealTerm<?, ?> pTerm) {
    List<DRealTerm<?, ?>> terms = new ArrayList<>();
    if (pTerm.isFormula() || pTerm.isVar()) {
      throw new IllegalArgumentException("Term is Variable or Formula");
    } else {
      terms.add(new DRealTerm<>(
          new Expression(dreal.get_constant_in_multiplication(pTerm.getExpression())),
          pTerm.getType(), ExpressionKind.Constant));
      ExpressionExpressionMap map =
          dreal.get_base_to_exponent_map_in_multiplication(pTerm.getExpression());
      // Map of Variable and exponent, exponent can be -1 or grater than 0, we will return pow
      // (var, exp), visit will handle pow
      for (Map.Entry<Expression, Expression> entry : map.entrySet()) {
        terms.add(new DRealTerm<>(dreal.pow(entry.getKey(), entry.getValue()), pTerm.getType(),
            ExpressionKind.Pow));
      }
      return terms;
    }
  }

  /*
  Needed if we split a formula. For example, we have x + 2 < 10. In visit we will
  extract the first and second expression and need the type for the Expression. Because we
  have only the parent to get the type from, it will always return Boolean Type, but the
  expression should be some kind of NumeralFormula Type
  */
  protected static Variable.Type getTypeForExpressions(Expression pExpression) {
    // If the Expression is Constant we return null, because from constant we cannot get the
    // type, but this function is always called from a formula, so this is called two times, and
    // one of them has a Variable, else this Formula would have been a True or False formula
    if (pExpression.get_kind() == ExpressionKind.Constant) {
      return null;
    } else if (pExpression.get_kind() == ExpressionKind.Var) {
      return dreal.get_variable(pExpression).get_type();
    } else {
      // There is at least one Variable in the Expression, else it would be a constant
      VariableSet varSet = pExpression.getVariables();
      Preconditions.checkState(!varSet.isEmpty());
      Iterator<Variable> iter = varSet.iterator();
      Variable var = iter.next();
      return var.get_type();
    }
  }


  // Not possible to throw an unsupported exception, because in AbstractFormulaManager
  // declareUFImpl is called.
  @Override
  public DRealTerm<?, ?> declareUFImpl(String pName, Variable.Type pReturnType,
                                    List<Variable.Type> pArgTypes) {
    return null;
  }

  // TODO: Funtkion schreiben, um nicht immer wieder die Fälle durchzugehen sondern das auslagern
  //  wenn möglich
  @Override
  public DRealTerm<?, ?> callFunctionImpl(DRealTerm<?, ?> declaration, List<DRealTerm<?, ?>> args) {
    if (args.isEmpty()) {
      // create variable with type of declaration term
      return new DRealTerm<>(new Variable("newVar", declaration.getType()),
          declaration.getType(), declaration.getType());
    } else {
      if (declaration.isExp()) {
        ExpressionKind expressionKind = (ExpressionKind) declaration.getDeclaration();
        if (expressionKind.equals(ExpressionKind.IfThenElse)) {
          // ITE cond has to be a formula
          Preconditions.checkState(args.get(0).isFormula());
          DRealTerm<?, ?> args1 = args.get(1);
          DRealTerm<?, ?> args2 = args.get(2);
          Expression ite;
          // ITE else and then must be expression or variable
          Preconditions.checkState((args1.isExp() || args1.isVar())
              && (args2.isExp() || args2.isVar()));
          if (args1.isVar() && args2.isVar()) {
            ite = dreal.if_then_else(args.get(0).getFormula(),
                new Expression(args1.getVariable()), new Expression(args2.getVariable()));
          } else if (args1.isVar() && args2.isExp()) {
            ite = dreal.if_then_else(args.get(0).getFormula(), new Expression(args1.getVariable()),
                args2.getExpression());
          } else if (args1.isExp() && args2.isVar()) {
            ite = dreal.if_then_else(args.get(0).getFormula(),
                args1.getExpression(), new Expression(args2.getVariable()));
          } else {
            ite = dreal.if_then_else(args.get(0).getFormula(), args1.getExpression(),
                args2.getExpression());
          }
          return new DRealTerm<>(ite, args2.getType(), ExpressionKind.IfThenElse);
        } else if (expressionKind.equals(ExpressionKind.Div)) {
          DRealTerm<?, ?> args1 = args.get(0);
          DRealTerm<?, ?> args2 = args.get(1);
          Expression div;
          // Numerator and denumerator must be expression or variable
          Preconditions.checkState((args1.isExp() || args1.isVar())
              && (args2.isExp() || args2.isVar()));
          if (args1.isVar() && args2.isVar()) {
            div = dreal.Divide(new Expression(args1.getVariable()),
                new Expression(args2.getVariable()));
          } else if (args1.isVar() && args2.isExp()) {
            div = dreal.Divide(new Expression(args1.getVariable()),
                args2.getExpression());
          } else if (args1.isExp() && args2.isVar()) {
            div = dreal.Divide(args1.getExpression(),
                new Expression(args2.getVariable()));
          } else {
            div = dreal.Divide(args1.getExpression(),
                args2.getExpression());
          }
          return new DRealTerm<>(div, args1.getType(), ExpressionKind.Div);
        } else if (expressionKind.equals(ExpressionKind.Mul)) {
          DRealTerm<?, ?> args1 = args.get(0);
          DRealTerm<?, ?> args2 = args.get(1);
          Expression mult;
          // First/Second argument must be expression or variable
          Preconditions.checkState((args1.isExp() || args1.isVar())
              && (args2.isExp() || args2.isVar()));
          if (args1.isVar() && args2.isVar()) {
            mult = dreal.Multiply(new Expression(args1.getVariable()),
                new Expression(args2.getVariable()));
          } else if (args1.isVar() && args2.isExp()) {
            mult = dreal.Multiply(new Expression(args1.getVariable()),
                args2.getExpression());
          } else if (args1.isExp() && args2.isVar()) {
            mult = dreal.Multiply(args1.getExpression(),
                new Expression(args2.getVariable()));
          } else {
            mult = dreal.Multiply(args1.getExpression(),
                args2.getExpression());
          }
          if (args.size() > 2) {
            for (int i = 2; i < args.size(); i++) {
              if (args.get(i).isExp()) {
                mult = dreal.Multiply(mult, args.get(i).getExpression());
              } else {
                mult = dreal.Multiply(mult, new Expression(args.get(i).getVariable()));
              }
            }
          }
          return new DRealTerm<>(mult, args.get(0).getType(), ExpressionKind.Mul);
        } else if (expressionKind.equals(ExpressionKind.Add)) {
          DRealTerm<?, ?> args1 = args.get(0);
          DRealTerm<?, ?> args2 = args.get(1);
          Expression add;
          // First/Second argument must be expression or variable
          Preconditions.checkState((args1.isExp() || args1.isVar())
              && (args2.isExp() || args2.isVar()));
          if (args1.isVar() && args2.isVar()) {
            add = dreal.Add(new Expression(args1.getVariable()),
                new Expression(args2.getVariable()));
          } else if (args1.isVar() && args2.isExp()) {
            add = dreal.Add(new Expression(args1.getVariable()),
                args2.getExpression());
          } else if (args1.isExp() && args2.isVar()) {
            add = dreal.Add(args1.getExpression(),
                new Expression(args2.getVariable()));
          } else {
            add = dreal.Add(args1.getExpression(),
                args2.getExpression());
          }
          if (args.size() > 2) {
            for (int i = 2; i < args.size(); i++) {
              if (args.get(i).isExp()) {
                add = dreal.Add(add, args.get(i).getExpression());
              } else {
                add = dreal.Add(add, new Expression(args.get(i).getVariable()));
              }
            }
          }
          return new DRealTerm<>(add, args.get(0).getType(), ExpressionKind.Add);
        } else {
          throw new UnsupportedOperationException("No known declarationkind and UF's are not "
              + "supported.");
        }
      } else if (declaration.isFormula()) {
        FormulaKind formulaKind = (FormulaKind) declaration.getDeclaration();
        if (formulaKind.equals(FormulaKind.Not)) {
          DRealTerm<?, ?> args1 = args.get(0);
          org.sosy_lab.java_smt.solvers.dreal4.drealjni.Formula not;
          // Argument must be formula or variable
          Preconditions.checkState(args1.isFormula() || args1.isVar());
          if (args1.isVar() && args1.getType() == Variable.Type.BOOLEAN) {
            not = dreal.Not(new Variable(args1.getVariable()));
          }  else if (args1.isFormula()) {
            not = dreal.Not(args1.getFormula());
          } else {
            throw new IllegalArgumentException("Variable is not of boolean type and therefore a "
                + "formula can not be created.");
          }
          return new DRealTerm<>(not, Variable.Type.BOOLEAN,
              FormulaKind.Not);
        } else if (formulaKind.equals(FormulaKind.Eq)) {
          DRealTerm<?, ?> args1 = args.get(0);
          DRealTerm<?, ?> args2 = args.get(1);
          org.sosy_lab.java_smt.solvers.dreal4.drealjni.Formula eq;
          if (args1.isVar() && args2.isVar()) {
            eq = dreal.Equal(args1.getVariable(), args2.getVariable());
          } else if (args1.isVar() && args2.isExp()) {
            eq = dreal.Equal(new Expression(args1.getVariable()), args2.getExpression());
          } else if (args1.isVar() && args2.isFormula()) {
                          eq = dreal.Equal(args1.getVariable(), args2.getFormula());
          } else if (args1.isExp() && args2.isVar()) {
            eq = dreal.Equal(args1.getExpression(), new Expression(args2.getVariable()));
          } else if (args1.isExp() && args2.isExp()) {
            eq = dreal.Equal(args1.getExpression(), args2.getExpression());
          } else if (args1.isFormula() && args2.isVar()) {
            eq = dreal.Equal(args1.getFormula(), args2.getVariable());
          } else if (args1.isFormula() && args2.isFormula()) {
            eq = dreal.Equal(args1.getFormula(), args2.getFormula());
          } else {
            throw new UnsupportedOperationException("Can not create an equal formula from "
                + "Expression and Formula.");
          }
          return new DRealTerm<>(eq, Variable.Type.BOOLEAN, FormulaKind.Eq);
        } else if (formulaKind.equals(FormulaKind.Gt)) {
          DRealTerm<?, ?> args1 = args.get(0);
          DRealTerm<?, ?> args2 = args.get(1);
          org.sosy_lab.java_smt.solvers.dreal4.drealjni.Formula gt;
          // First/Second argument must be expression or variable
          Preconditions.checkState((args1.isExp() || args1.isVar())
              && (args2.isExp() || args2.isVar()));
          if (args1.isVar() && args2.isVar()) {
            gt = dreal.Grater(new Expression(args1.getVariable()),
                new Expression(args2.getVariable()));
          } else if (args1.isVar() && args2.isExp()) {
            gt = dreal.Grater(new Expression(args1.getVariable()),
                args2.getExpression());
          } else if (args1.isExp() && args2.isVar()) {
            gt = dreal.Grater(args1.getExpression(),
                new Expression(args2.getVariable()));
          } else {
            gt = dreal.Grater(args1.getExpression(),
                args2.getExpression());
          }
          return new DRealTerm<>(gt, args.get(0).getType(), FormulaKind.Gt);
        } else if (formulaKind.equals(FormulaKind.Geq)) {
          DRealTerm<?, ?> args1 = args.get(0);
          DRealTerm<?, ?> args2 = args.get(1);
          org.sosy_lab.java_smt.solvers.dreal4.drealjni.Formula geq;
          // First/Second argument must be expression or variable
          Preconditions.checkState((args1.isExp() || args1.isVar())
              && (args2.isExp() || args2.isVar()));
          if (args1.isVar() && args2.isVar()) {
            geq = dreal.GraterEqual(new Expression(args1.getVariable()),
                new Expression(args2.getVariable()));
          } else if (args1.isVar() && args2.isExp()) {
            geq = dreal.GraterEqual(new Expression(args1.getVariable()),
                args2.getExpression());
          } else if (args1.isExp() && args2.isVar()) {
            geq = dreal.GraterEqual(args1.getExpression(),
                new Expression(args2.getVariable()));
          } else {
            geq = dreal.GraterEqual(args1.getExpression(),
                args2.getExpression());
          }
          return new DRealTerm<>(geq, args.get(0).getType(), FormulaKind.Geq);
        } else if (formulaKind.equals(FormulaKind.Lt)) {
          DRealTerm<?, ?> args1 = args.get(0);
          DRealTerm<?, ?> args2 = args.get(1);
          org.sosy_lab.java_smt.solvers.dreal4.drealjni.Formula lt;
          // First/Second argument must be expression or variable
          Preconditions.checkState((args1.isExp() || args1.isVar())
              && (args2.isExp() || args2.isVar()));
          if (args1.isVar() && args2.isVar()) {
            lt = dreal.Less(new Expression(args1.getVariable()),
                new Expression(args2.getVariable()));
          } else if (args1.isVar() && args2.isExp()) {
            lt = dreal.Less(new Expression(args1.getVariable()),
                args2.getExpression());
          } else if (args1.isExp() && args2.isVar()) {
            lt = dreal.Less(args1.getExpression(),
                new Expression(args2.getVariable()));
          } else {
            lt = dreal.Less(args1.getExpression(),
                args2.getExpression());
          }
          return new DRealTerm<>(lt, args.get(0).getType(), FormulaKind.Lt);
        } else if (formulaKind.equals(FormulaKind.Leq)) {
          DRealTerm<?, ?> args1 = args.get(0);
          DRealTerm<?, ?> args2 = args.get(1);
          org.sosy_lab.java_smt.solvers.dreal4.drealjni.Formula leq;
          // First/Second argument must be expression or variable
          Preconditions.checkState((args1.isExp() || args1.isVar())
              && (args2.isExp() || args2.isVar()));
          if (args1.isVar() && args2.isVar()) {
            leq = dreal.LessEqual(new Expression(args1.getVariable()),
                new Expression(args2.getVariable()));
          } else if (args1.isVar() && args2.isExp()) {
            leq = dreal.LessEqual(new Expression(args1.getVariable()),
                args2.getExpression());
          } else if (args1.isExp() && args2.isVar()) {
            leq = dreal.LessEqual(args1.getExpression(),
                new Expression(args2.getVariable()));
          } else {
            leq = dreal.LessEqual(args1.getExpression(),
                args2.getExpression());
          }
          return new DRealTerm<>(leq, args.get(0).getType(), FormulaKind.Leq);
        } else if (formulaKind.equals(FormulaKind.And)) {
          DRealTerm<?, ?> args1 = args.get(0);
          DRealTerm<?, ?> args2 = args.get(1);
          org.sosy_lab.java_smt.solvers.dreal4.drealjni.Formula and;
          if (args1.isVar() && args1.getType() == Variable.Type.BOOLEAN
              && args2.isVar() && args2.getType() == Variable.Type.BOOLEAN) {
            and = dreal.And(new org.sosy_lab.java_smt.solvers.dreal4.drealjni.Formula(args1.getVariable()),
                new org.sosy_lab.java_smt.solvers.dreal4.drealjni.Formula(args2.getVariable()));
          } else if (args1.isVar() && args1.getType() == Variable.Type.BOOLEAN
              && args2.isFormula()) {
            and = dreal.And(new org.sosy_lab.java_smt.solvers.dreal4.drealjni.Formula(args1.getVariable()),
                args2.getFormula());
          } else if (args1.isFormula()
              && args2.isVar() && args2.getType() == Variable.Type.BOOLEAN) {
            and = dreal.And(args1.getFormula(),
                new org.sosy_lab.java_smt.solvers.dreal4.drealjni.Formula(args2.getVariable()));
          } else if (args1.isFormula() && args2.isFormula()){
            and = dreal.And(args1.getFormula(), args2.getFormula());
          } else {
            throw new UnsupportedOperationException("And-Formula from Expression or Variable of "
                + "not boolean type is not supported.");
          }
          if (args.size() > 2) {
            for (int i = 2; i < args.size(); i++) {
              if (args.get(i).isVar() && args.get(i).getType() == Variable.Type.BOOLEAN) {
                and = dreal.And(and, new org.sosy_lab.java_smt.solvers.dreal4.drealjni.Formula(args.get(i).getVariable()));
              } else if (args.get(i).isFormula()) {
                and = dreal.And(and, args.get(i).getFormula());
              } else {
                throw new UnsupportedOperationException("And-Formula from Expression or Variable "
                    + "of not boolean type is not supported.");
              }
            }
          }
          return new DRealTerm<>(and, Variable.Type.BOOLEAN, FormulaKind.And);
        } else if (formulaKind.equals(FormulaKind.Or)) {
          DRealTerm<?, ?> args1 = args.get(0);
          DRealTerm<?, ?> args2 = args.get(1);
          org.sosy_lab.java_smt.solvers.dreal4.drealjni.Formula or;
          if (args1.isVar() && args1.getType() == Variable.Type.BOOLEAN
              && args2.isVar() && args2.getType() == Variable.Type.BOOLEAN) {
            or =
                dreal.Or(new org.sosy_lab.java_smt.solvers.dreal4.drealjni.Formula(args1.getVariable()),
                new org.sosy_lab.java_smt.solvers.dreal4.drealjni.Formula(args2.getVariable()));
          } else if (args1.isVar() && args1.getType() == Variable.Type.BOOLEAN
              && args2.isFormula()) {
            or =
                dreal.Or(new org.sosy_lab.java_smt.solvers.dreal4.drealjni.Formula(args1.getVariable()),
                args2.getFormula());
          } else if (args1.isFormula()
              && args2.isVar() && args2.getType() == Variable.Type.BOOLEAN) {
            or = dreal.Or(args1.getFormula(),
                new org.sosy_lab.java_smt.solvers.dreal4.drealjni.Formula(args2.getVariable()));
          } else if (args1.isFormula() && args2.isFormula()){
            or = dreal.Or(args1.getFormula(), args2.getFormula());
          } else {
            throw new UnsupportedOperationException("or-Formula from Expression or Variable of "
                + "not boolean type is not supported.");
          }
          if (args.size() > 2) {
            for (int i = 2; i < args.size(); i++) {
              if (args.get(i).isVar() && args.get(i).getType() == Variable.Type.BOOLEAN) {
                or = dreal.Or(or,
                    new org.sosy_lab.java_smt.solvers.dreal4.drealjni.Formula(args.get(i).getVariable()));
              } else if (args.get(i).isFormula()) {
                or = dreal.Or(or, args.get(i).getFormula());
              } else {
                throw new UnsupportedOperationException("or-Formula from Expression or Variable "
                    + "of not boolean type is not supported.");
              }
            }
          }
          return new DRealTerm<>(or, Variable.Type.BOOLEAN, FormulaKind.And);
        }
      } else {
        throw new UnsupportedOperationException("No known declarationkind and UF's are not "
            + "supported.");
      }
      throw new IllegalArgumentException("Unknown function declaration.");
    }
  }


  @Override
  protected DRealTerm<?, ?> getBooleanVarDeclarationImpl(DRealTerm<?, ?> pDRealTerm) {
    if (pDRealTerm.isVar()) {
      return new DRealTerm<>(new Variable(), pDRealTerm.getType(), pDRealTerm.getType());
    } else if (pDRealTerm.isExp()) {
      return new DRealTerm<>(new Expression(), pDRealTerm.getType(),
          (ExpressionKind) pDRealTerm.getDeclaration());
    } else {
      return new DRealTerm<>(new org.sosy_lab.java_smt.solvers.dreal4.drealjni.Formula(),
          pDRealTerm.getType(), (FormulaKind) pDRealTerm.getDeclaration());
    }
  }

  @Override
  public Object convertValue(DRealTerm<?, ?> pTerm) {
    Preconditions.checkState(pTerm.isExp() || pTerm.isFormula());
    if (pTerm.isExp()) {
      // This should be a constant
      Preconditions.checkState(pTerm.getExpression().get_kind() == ExpressionKind.Constant);
      String s = pTerm.to_string();
      if (pTerm.getType() == Variable.Type.CONTINUOUS) {
        double res = Double.parseDouble(s);
        // check if double is int
        if ((res == Math.floor(res)) && !Double.isInfinite(res)) {
          BigDecimal b = BigDecimal.valueOf(parseDouble(s));
          return b.toBigInteger();
        }
        return Rational.ofBigDecimal(BigDecimal.valueOf(parseDouble(s)));
      } else {
        return new BigInteger(s);
      }
    } else {
      if (pTerm.getFormulaKind() == FormulaKind.True || pTerm.getFormulaKind() == FormulaKind.False) {
        return dreal.is_true(pTerm.getFormula());
      } else {
        throw new UnsupportedOperationException("Can not convert Formula to Value.");
      }

    }
  }

}