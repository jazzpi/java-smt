// This file is part of JavaSMT,
// an API wrapper for a collection of SMT solvers:
// https://github.com/sosy-lab/java-smt
//
// SPDX-FileCopyrightText: 2021 Dirk Beyer <https://www.sosy-lab.org>
//
// SPDX-License-Identifier: Apache-2.0

package org.sosy_lab.java_smt.test;

import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;
import org.sosy_lab.java_smt.api.RegexFormula;
import org.sosy_lab.java_smt.api.SolverException;
import org.sosy_lab.java_smt.api.StringFormula;

@SuppressWarnings("ConstantConditions")
@SuppressFBWarnings(value = "DLS_DEAD_LOCAL_STORE", justification = "test code")
@RunWith(Parameterized.class)
public class StringFormulaManagerTest extends SolverBasedTest0 {

  private StringFormula hello;
  private RegexFormula a2z;

  @Parameters(name = "{0}")
  public static Object[] getAllSolvers() {
    return Solvers.values();
  }

  @Parameter public Solvers solverUnderTest;

  @Override
  protected Solvers solverToUse() {
    return solverUnderTest;
  }

  @Before
  public void setup() {
    requireStrings();
    hello = smgr.makeString("hello");
    a2z = smgr.range('a', 'z');
  }

  // Utility methods

  private void assertEqual(IntegerFormula num1, IntegerFormula num2)
      throws SolverException, InterruptedException {
    assertThatFormula(imgr.equal(num1, num2)).isTautological();
  }

  private void assertDistinct(IntegerFormula num1, IntegerFormula num2)
      throws SolverException, InterruptedException {
    assertThatFormula(imgr.distinct(List.of(num1, num2))).isTautological();
  }

  private void assertEqual(StringFormula str1, StringFormula str2)
      throws SolverException, InterruptedException {
    assertThatFormula(smgr.equal(str1, str2)).isTautological();
  }

  private void assertDistinct(StringFormula str1, StringFormula str2)
      throws SolverException, InterruptedException {
    assertThatFormula(smgr.equal(str1, str2)).isUnsatisfiable();
  }

  // Tests

  @Test
  public void testRegexAll() throws SolverException, InterruptedException {
    RegexFormula regex = smgr.all();
    assertThatFormula(smgr.in(hello, regex)).isSatisfiable();
  }

  @Test
  public void testRegexAll3() throws SolverException, InterruptedException {
    // This is not ALL_CHAR! This matches ".*" literally!
    RegexFormula regex = smgr.makeRegex(".*");
    assertThatFormula(smgr.in(hello, regex)).isUnsatisfiable();
    assertThatFormula(smgr.in(smgr.makeString(".*"), regex)).isSatisfiable();
  }

  @Test
  public void testStringRegex2() throws SolverException, InterruptedException {
    RegexFormula regex = smgr.concat(smgr.closure(a2z), smgr.makeRegex("ll"), smgr.closure(a2z));
    assertThatFormula(smgr.in(hello, regex)).isSatisfiable();
  }

  @Test
  public void testStringRegex3() throws SolverException, InterruptedException {
    RegexFormula regex = smgr.makeRegex(".*ll.*");
    assertThatFormula(smgr.in(hello, regex)).isUnsatisfiable();
  }

  @Test
  public void testEmptyRegex() throws SolverException, InterruptedException {
    RegexFormula regex = smgr.none();
    assertThatFormula(smgr.in(hello, regex)).isUnsatisfiable();
  }

  @Test
  public void testStringConcat() throws SolverException, InterruptedException {
    StringFormula str1 = smgr.makeString("hello");
    StringFormula str2 = smgr.makeString("world");
    StringFormula concat = smgr.concat(str1, str2);
    StringFormula complete = smgr.makeString("helloworld");

    assertEqual(concat, complete);
  }

  @Test
  public void testStringConcatEmpty() throws SolverException, InterruptedException {
    StringFormula empty = smgr.makeString("");

    assertEqual(empty, smgr.concat(ImmutableList.of()));
    assertEqual(empty, smgr.concat(empty));
    assertEqual(empty, smgr.concat(empty, empty));
    assertEqual(empty, smgr.concat(ImmutableList.of(empty, empty, empty, empty)));
  }

  @Test
  public void testStringPrefixSuffixConcat() throws SolverException, InterruptedException {
    // check whether "prefix + suffix == concat"
    StringFormula prefix = smgr.makeVariable("prefix");
    StringFormula suffix = smgr.makeVariable("suffix");
    StringFormula concat = smgr.makeVariable("concat");

    assertThatFormula(
            bmgr.and(
                smgr.prefix(prefix, concat),
                smgr.suffix(suffix, concat),
                imgr.equal(
                    smgr.length(concat), imgr.add(smgr.length(prefix), smgr.length(suffix)))))
        .implies(smgr.equal(concat, smgr.concat(prefix, suffix)));
  }

  @Test
  public void testStringPrefixSuffix() throws SolverException, InterruptedException {
    // check whether "prefix == suffix iff equal length"
    StringFormula prefix = smgr.makeVariable("prefix");
    StringFormula suffix = smgr.makeVariable("suffix");

    assertThatFormula(bmgr.and(smgr.prefix(prefix, suffix), smgr.suffix(suffix, prefix)))
        .implies(smgr.equal(prefix, suffix));
    assertThatFormula(
            bmgr.and(
                smgr.prefix(prefix, suffix), imgr.equal(smgr.length(prefix), smgr.length(suffix))))
        .implies(smgr.equal(prefix, suffix));
    assertThatFormula(
            bmgr.and(
                smgr.suffix(suffix, prefix), imgr.equal(smgr.length(prefix), smgr.length(suffix))))
        .implies(smgr.equal(prefix, suffix));
  }

  @Test
  public void testStringToIntConversion() throws SolverException, InterruptedException {
    IntegerFormula ten = imgr.makeNumber(10);
    StringFormula zeroStr = smgr.makeString("0");

    for (int i = 0; i < 100; i += 7) {
      StringFormula str = smgr.makeString(Integer.toString(i));
      IntegerFormula num = imgr.makeNumber(i);
      IntegerFormula numInc = imgr.makeNumber(i + 1);

      assertEqual(str, smgr.toStringFormula(num));
      assertDistinct(str, smgr.toStringFormula(numInc));
      assertDistinct(str, smgr.toStringFormula(imgr.add(num, numInc)));

      assertEqual(num, smgr.toIntegerFormula(str));
      assertDistinct(numInc, smgr.toIntegerFormula(str));
      assertEqual(imgr.multiply(num, ten), smgr.toIntegerFormula(smgr.concat(str, zeroStr)));
      assertDistinct(imgr.multiply(numInc, ten), smgr.toIntegerFormula(smgr.concat(str, zeroStr)));

      assertEqual(num, smgr.toIntegerFormula(smgr.toStringFormula(num)));
      assertEqual(numInc, smgr.toIntegerFormula(smgr.toStringFormula(numInc)));
    }
  }

  @Test
  public void testStringToIntConversionCornerCases() throws SolverException, InterruptedException {
    assertEqual(imgr.makeNumber(-1), smgr.toIntegerFormula(smgr.makeString("-1")));
    assertEqual(imgr.makeNumber(-1), smgr.toIntegerFormula(smgr.makeString("-12")));
    assertEqual(imgr.makeNumber(-1), smgr.toIntegerFormula(smgr.makeString("-123")));
    assertEqual(imgr.makeNumber(-1), smgr.toIntegerFormula(smgr.makeString("-1234")));

    assertDistinct(imgr.makeNumber(-12), smgr.toIntegerFormula(smgr.makeString("-1")));
    assertDistinct(imgr.makeNumber(-12), smgr.toIntegerFormula(smgr.makeString("-12")));
    assertDistinct(imgr.makeNumber(-12), smgr.toIntegerFormula(smgr.makeString("-123")));
    assertDistinct(imgr.makeNumber(-12), smgr.toIntegerFormula(smgr.makeString("-1234")));

    assertEqual(imgr.makeNumber(-1), smgr.toIntegerFormula(smgr.makeString("")));
    assertEqual(imgr.makeNumber(-1), smgr.toIntegerFormula(smgr.makeString("a")));
    assertEqual(imgr.makeNumber(-1), smgr.toIntegerFormula(smgr.makeString("1a")));
    assertEqual(imgr.makeNumber(-1), smgr.toIntegerFormula(smgr.makeString("a1")));

    assertDistinct(imgr.makeNumber(-12), smgr.toIntegerFormula(smgr.makeString("")));
    assertDistinct(imgr.makeNumber(-12), smgr.toIntegerFormula(smgr.makeString("a")));
    assertDistinct(imgr.makeNumber(-12), smgr.toIntegerFormula(smgr.makeString("1a")));
    assertDistinct(imgr.makeNumber(-12), smgr.toIntegerFormula(smgr.makeString("a1")));
  }

  @Test
  public void testIntToStringConversionCornerCases() throws SolverException, InterruptedException {
    assertEqual(smgr.makeString("123"), smgr.toStringFormula(imgr.makeNumber(123)));
    assertEqual(smgr.makeString("1"), smgr.toStringFormula(imgr.makeNumber(1)));
    assertEqual(smgr.makeString("0"), smgr.toStringFormula(imgr.makeNumber(0)));
    assertEqual(smgr.makeString(""), smgr.toStringFormula(imgr.makeNumber(-1)));
    assertEqual(smgr.makeString(""), smgr.toStringFormula(imgr.makeNumber(-123)));

    assertDistinct(smgr.makeString("1"), smgr.toStringFormula(imgr.makeNumber(-1)));
  }

  @Test
  public void testStringLength() throws SolverException, InterruptedException {
    assertEqual(imgr.makeNumber(0), smgr.length(smgr.makeString("")));
    assertEqual(imgr.makeNumber(1), smgr.length(smgr.makeString("a")));
    assertEqual(imgr.makeNumber(2), smgr.length(smgr.makeString("aa")));
    assertEqual(imgr.makeNumber(9), smgr.length(smgr.makeString("aaabbbccc")));

    assertDistinct(imgr.makeNumber(5), smgr.length(smgr.makeString("")));
    assertDistinct(imgr.makeNumber(5), smgr.length(smgr.makeString("a")));
    assertDistinct(imgr.makeNumber(5), smgr.length(smgr.makeString("aa")));
    assertDistinct(imgr.makeNumber(5), smgr.length(smgr.makeString("aaabbbcc")));
  }

  @Test
  public void testStringLengthWithVariable() throws SolverException, InterruptedException {
    StringFormula var = smgr.makeVariable("var");

    assertThatFormula(imgr.equal(imgr.makeNumber(0), smgr.length(var)))
        .implies(smgr.equal(var, smgr.makeString("")));

    assertThatFormula(
            bmgr.and(
                imgr.equal(imgr.makeNumber(5), smgr.length(var)),
                smgr.prefix(smgr.makeString("aba"), var),
                smgr.suffix(smgr.makeString("aba"), var)))
        .implies(smgr.equal(smgr.makeVariable("var"), smgr.makeString("ababa")));

    assertThatFormula(
            bmgr.and(
                imgr.equal(imgr.makeNumber(4), smgr.length(var)),
                smgr.prefix(smgr.makeString("aba"), var),
                smgr.suffix(smgr.makeString("aba"), var)))
        .isUnsatisfiable();

    assertThatFormula(
            bmgr.and(
                imgr.equal(imgr.makeNumber(4), smgr.length(var)),
                smgr.prefix(smgr.makeString("ab"), var),
                smgr.suffix(smgr.makeString("ba"), var),
                smgr.equal(smgr.makeString("c"), smgr.charAt(var, imgr.makeNumber(3)))))
        .isUnsatisfiable();

    assertThatFormula(
            bmgr.and(
                imgr.equal(imgr.makeNumber(5), smgr.length(var)),
                smgr.prefix(smgr.makeString("ab"), var),
                smgr.suffix(smgr.makeString("ba"), var),
                smgr.equal(smgr.makeString("c"), smgr.charAt(var, imgr.makeNumber(3)))))
        .implies(smgr.equal(smgr.makeVariable("var"), smgr.makeString("abcba")));
  }

  /*
   * Test const Strings = String variables + prefix and suffix constraints.
   */
  @Test
  public void testConstStringEqStringVar() throws SolverException, InterruptedException {
    String string1 = "";
    String string2 = "a";
    String string3 = "ab";
    String string4 = "abcdefghijklmnopqrstuvwxyz";
    StringFormula string1c = smgr.makeString(string1);
    StringFormula string2c = smgr.makeString(string2);
    StringFormula string3c = smgr.makeString(string3);
    StringFormula string4c = smgr.makeString(string4);
    StringFormula string1v = smgr.makeVariable("string1v");
    StringFormula string2v = smgr.makeVariable("string1v");
    StringFormula string3v = smgr.makeVariable("string1v");
    StringFormula string4v = smgr.makeVariable("string1v");

    BooleanFormula formula =
        bmgr.and(
            smgr.equal(string1c, string1v),
            smgr.equal(string2c, string2v),
            smgr.equal(string3c, string3v),
            smgr.equal(string4c, string4v));

    BooleanFormula string1PrefixFormula =
        bmgr.and(
            smgr.prefix(string1c, string1v),
            bmgr.not(smgr.prefix(string2c, string1v)),
            bmgr.not(smgr.prefix(string3c, string1v)),
            bmgr.not(smgr.prefix(string4c, string1v)));

    BooleanFormula string2PrefixFormula =
        bmgr.and(
            smgr.prefix(string1c, string2v),
            smgr.prefix(string2c, string2v),
            bmgr.not(smgr.prefix(string3c, string2v)),
            bmgr.not(smgr.prefix(string4c, string2v)));

    BooleanFormula string3PrefixFormula =
        bmgr.and(
            smgr.prefix(string3c, string3v),
            smgr.prefix(string2c, string3v),
            smgr.prefix(string3c, string3v),
            bmgr.not(smgr.prefix(string4c, string3v)));

    BooleanFormula string4PrefixFormula =
        bmgr.and(
            smgr.prefix(string1c, string4v),
            smgr.prefix(string2c, string4v),
            smgr.prefix(string3c, string4v),
            smgr.prefix(string4c, string4v));

    BooleanFormula string1SuffixFormula =
        bmgr.and(
            smgr.suffix(string1c, string1v),
            bmgr.not(smgr.suffix(string2c, string1v)),
            bmgr.not(smgr.suffix(string3c, string1v)),
            bmgr.not(smgr.suffix(string4c, string1v)));

    BooleanFormula string2SuffixFormula =
        bmgr.and(
            smgr.suffix(string1c, string2v),
            smgr.suffix(string2c, string2v),
            bmgr.not(smgr.suffix(string3c, string2v)),
            bmgr.not(smgr.suffix(string4c, string2v)));

    BooleanFormula string3SuffixFormula =
        bmgr.and(
            smgr.suffix(string3c, string3v),
            smgr.suffix(string2c, string3v),
            smgr.suffix(string3c, string3v),
            bmgr.not(smgr.suffix(string4c, string3v)));

    BooleanFormula string4SuffixFormula =
        bmgr.and(
            smgr.suffix(string1c, string4v),
            smgr.suffix(string2c, string4v),
            smgr.suffix(string3c, string4v),
            smgr.suffix(string4c, string4v));

    assertThatFormula(bmgr.and(formula))
        .implies(
            bmgr.and(
                string1PrefixFormula,
                string2PrefixFormula,
                string3PrefixFormula,
                string4PrefixFormula,
                string1SuffixFormula,
                string2SuffixFormula,
                string3SuffixFormula,
                string4SuffixFormula));
  }

  /** Test String variables with negative length (UNSAT). */
  @Test
  public void testStringVariableLengthNegative() throws SolverException, InterruptedException {
    StringFormula stringVariable1 = smgr.makeVariable("zeroLength");
    StringFormula stringVariable2 = smgr.makeVariable("negLength");

    // SAT + UNSAT Formula -> UNSAT
    assertThatFormula(
            bmgr.and(
                imgr.equal(smgr.length(stringVariable1), imgr.makeNumber(0)),
                imgr.equal(smgr.length(stringVariable2), imgr.makeNumber(-100))))
        .isUnsatisfiable();
    // UNSAT below
    assertThatFormula(imgr.equal(smgr.length(stringVariable2), imgr.makeNumber(-1)))
        .isUnsatisfiable();
    assertThatFormula(imgr.equal(smgr.length(stringVariable2), imgr.makeNumber(-100)))
        .isUnsatisfiable();
  }

  /**
   * Test String formulas with inequalities in the negative range.
   *
   * <p>-10000 < stringVariable length < 0 -> UNSAT
   *
   * <p>-10000 < stringVariable length < -1 -> UNSAT
   *
   * <p>-10000 <= stringVariable length <= -1 -> UNSAT
   *
   * <p>-10000 <= stringVariable length <= 0 AND stringVariable != "" -> UNSAT
   *
   * <p>-10000 <= stringVariable length <= 0 -> SAT implies stringVariable = ""
   */
  @Test
  public void testStringLengthInequalityNegativeRange()
      throws SolverException, InterruptedException {
    StringFormula stringVariable = smgr.makeVariable("stringVariable");
    IntegerFormula stringVariableLength = smgr.length(stringVariable);
    IntegerFormula minusTenThousand = imgr.makeNumber(-10000);
    IntegerFormula minusOne = imgr.makeNumber(-1);
    IntegerFormula zero = imgr.makeNumber(0);

    // -10000 < stringVariable length < 0 -> UNSAT
    assertThatFormula(
            bmgr.and(
                imgr.lessThan(minusTenThousand, stringVariableLength),
                imgr.lessThan(stringVariableLength, zero)))
        .isUnsatisfiable();
    // -10000 < stringVariable length < -1 -> UNSAT
    assertThatFormula(
            bmgr.and(
                imgr.lessThan(minusTenThousand, stringVariableLength),
                imgr.lessThan(stringVariableLength, minusOne)))
        .isUnsatisfiable();
    // -10000 <= stringVariable length <= -1 -> UNSAT
    assertThatFormula(
            bmgr.and(
                imgr.lessOrEquals(minusTenThousand, stringVariableLength),
                imgr.lessOrEquals(stringVariableLength, zero)))
        .isUnsatisfiable();
    // -10000 <= stringVariable length <= 0 AND stringVariable != "" -> UNSAT
    assertThatFormula(
            bmgr.and(
                imgr.lessOrEquals(minusTenThousand, stringVariableLength),
                imgr.lessOrEquals(stringVariableLength, zero),
                bmgr.not(smgr.equal(stringVariable, smgr.makeString("")))))
        .isUnsatisfiable();
    // -10000 <= stringVariable length <= 0 -> SAT implies stringVariable = ""
    assertThatFormula(
            bmgr.and(
                imgr.lessOrEquals(minusTenThousand, stringVariableLength),
                imgr.lessOrEquals(stringVariableLength, zero)))
        .implies(smgr.equal(stringVariable, smgr.makeString("")));
  }

  /**
   * Test String formulas with inequalities in the negative range.
   *
   * <p>0 < stringVariable length < 1 -> UNSAT
   *
   * <p>0 < stringVariable length < 2 -> SAT
   *
   * <p>0 <= stringVariable length < 1 -> SAT implies stringVariable = ""
   *
   * <p>1 < stringVariable length < 3 -> SAT implies stringVariable length = 2
   */
  @Test
  public void testStringLengthInequalityPositiveRange()
      throws SolverException, InterruptedException {
    StringFormula stringVariable = smgr.makeVariable("stringVariable");
    IntegerFormula stringVariableLength = smgr.length(stringVariable);
    IntegerFormula three = imgr.makeNumber(3);
    IntegerFormula two = imgr.makeNumber(2);
    IntegerFormula one = imgr.makeNumber(1);
    IntegerFormula zero = imgr.makeNumber(0);

    // 0 < stringVariable length < 1 -> UNSAT
    assertThatFormula(
            bmgr.and(
                imgr.lessThan(zero, stringVariableLength),
                imgr.lessThan(stringVariableLength, one)))
        .isUnsatisfiable();
    // 0 < stringVariable length < 2 -> SAT
    assertThatFormula(
            bmgr.and(
                imgr.lessThan(zero, stringVariableLength),
                imgr.lessThan(stringVariableLength, two)))
        .isUnsatisfiable();
    // 0 <= stringVariable length < 1 -> SAT implies stringVariable = ""
    assertThatFormula(
            bmgr.and(
                imgr.lessOrEquals(zero, stringVariableLength),
                imgr.lessThan(stringVariableLength, one)))
        .implies(smgr.equal(stringVariable, smgr.makeString("")));
    // 1 < stringVariable length < 3 -> SAT implies stringVariable length = 2
    assertThatFormula(
            bmgr.and(
                imgr.lessThan(one, stringVariableLength),
                imgr.lessThan(stringVariableLength, three)))
        .implies(imgr.equal(smgr.length(stringVariable), two));
  }
}
