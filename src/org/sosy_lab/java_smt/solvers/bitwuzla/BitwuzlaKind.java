// This file is part of JavaSMT,
// an API wrapper for a collection of SMT solvers:
// https://github.com/sosy-lab/java-smt
//
// SPDX-FileCopyrightText: 2023 Dirk Beyer <https://www.sosy-lab.org>
//
// SPDX-License-Identifier: Apache-2.0

/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.sosy_lab.java_smt.solvers.bitwuzla;

public final class BitwuzlaKind {

  private static int swigNext = 0;
  private final int swigValue;
  private final String swigName;
  public static final BitwuzlaKind BITWUZLA_KIND_CONSTANT =
      BitwuzlaKind.of("BITWUZLA_KIND_CONSTANT", BitwuzlaJNI.BITWUZLA_KIND_CONSTANT_get());
  public static final BitwuzlaKind BITWUZLA_KIND_CONST_ARRAY =
      BitwuzlaKind.of("BITWUZLA_KIND_CONST_ARRAY");
  public static final BitwuzlaKind BITWUZLA_KIND_VALUE = BitwuzlaKind.of("BITWUZLA_KIND_VALUE");
  public static final BitwuzlaKind BITWUZLA_KIND_VARIABLE =
      BitwuzlaKind.of("BITWUZLA_KIND_VARIABLE");
  public static final BitwuzlaKind BITWUZLA_KIND_AND = BitwuzlaKind.of("BITWUZLA_KIND_AND");
  public static final BitwuzlaKind BITWUZLA_KIND_DISTINCT =
      BitwuzlaKind.of("BITWUZLA_KIND_DISTINCT");
  public static final BitwuzlaKind BITWUZLA_KIND_EQUAL = BitwuzlaKind.of("BITWUZLA_KIND_EQUAL");
  public static final BitwuzlaKind BITWUZLA_KIND_IFF = BitwuzlaKind.of("BITWUZLA_KIND_IFF");
  public static final BitwuzlaKind BITWUZLA_KIND_IMPLIES = BitwuzlaKind.of("BITWUZLA_KIND_IMPLIES");
  public static final BitwuzlaKind BITWUZLA_KIND_NOT = BitwuzlaKind.of("BITWUZLA_KIND_NOT");
  public static final BitwuzlaKind BITWUZLA_KIND_OR = BitwuzlaKind.of("BITWUZLA_KIND_OR");
  public static final BitwuzlaKind BITWUZLA_KIND_XOR = BitwuzlaKind.of("BITWUZLA_KIND_XOR");
  public static final BitwuzlaKind BITWUZLA_KIND_ITE = BitwuzlaKind.of("BITWUZLA_KIND_ITE");
  public static final BitwuzlaKind BITWUZLA_KIND_EXISTS = BitwuzlaKind.of("BITWUZLA_KIND_EXISTS");
  public static final BitwuzlaKind BITWUZLA_KIND_FORALL = BitwuzlaKind.of("BITWUZLA_KIND_FORALL");
  public static final BitwuzlaKind BITWUZLA_KIND_APPLY = BitwuzlaKind.of("BITWUZLA_KIND_APPLY");
  public static final BitwuzlaKind BITWUZLA_KIND_LAMBDA = BitwuzlaKind.of("BITWUZLA_KIND_LAMBDA");
  public static final BitwuzlaKind BITWUZLA_KIND_ARRAY_SELECT =
      BitwuzlaKind.of("BITWUZLA_KIND_ARRAY_SELECT");
  public static final BitwuzlaKind BITWUZLA_KIND_ARRAY_STORE =
      BitwuzlaKind.of("BITWUZLA_KIND_ARRAY_STORE");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_ADD = BitwuzlaKind.of("BITWUZLA_KIND_BV_ADD");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_AND = BitwuzlaKind.of("BITWUZLA_KIND_BV_AND");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_ASHR = BitwuzlaKind.of("BITWUZLA_KIND_BV_ASHR");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_COMP = BitwuzlaKind.of("BITWUZLA_KIND_BV_COMP");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_CONCAT =
      BitwuzlaKind.of("BITWUZLA_KIND_BV_CONCAT");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_DEC = BitwuzlaKind.of("BITWUZLA_KIND_BV_DEC");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_INC = BitwuzlaKind.of("BITWUZLA_KIND_BV_INC");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_MUL = BitwuzlaKind.of("BITWUZLA_KIND_BV_MUL");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_NAND = BitwuzlaKind.of("BITWUZLA_KIND_BV_NAND");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_NEG = BitwuzlaKind.of("BITWUZLA_KIND_BV_NEG");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_NOR = BitwuzlaKind.of("BITWUZLA_KIND_BV_NOR");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_NOT = BitwuzlaKind.of("BITWUZLA_KIND_BV_NOT");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_OR = BitwuzlaKind.of("BITWUZLA_KIND_BV_OR");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_REDAND =
      BitwuzlaKind.of("BITWUZLA_KIND_BV_REDAND");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_REDOR =
      BitwuzlaKind.of("BITWUZLA_KIND_BV_REDOR");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_REDXOR =
      BitwuzlaKind.of("BITWUZLA_KIND_BV_REDXOR");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_ROL = BitwuzlaKind.of("BITWUZLA_KIND_BV_ROL");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_ROR = BitwuzlaKind.of("BITWUZLA_KIND_BV_ROR");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_SADD_OVERFLOW =
      BitwuzlaKind.of("BITWUZLA_KIND_BV_SADD_OVERFLOW");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_SDIV_OVERFLOW =
      BitwuzlaKind.of("BITWUZLA_KIND_BV_SDIV_OVERFLOW");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_SDIV = BitwuzlaKind.of("BITWUZLA_KIND_BV_SDIV");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_SGE = BitwuzlaKind.of("BITWUZLA_KIND_BV_SGE");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_SGT = BitwuzlaKind.of("BITWUZLA_KIND_BV_SGT");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_SHL = BitwuzlaKind.of("BITWUZLA_KIND_BV_SHL");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_SHR = BitwuzlaKind.of("BITWUZLA_KIND_BV_SHR");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_SLE = BitwuzlaKind.of("BITWUZLA_KIND_BV_SLE");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_SLT = BitwuzlaKind.of("BITWUZLA_KIND_BV_SLT");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_SMOD = BitwuzlaKind.of("BITWUZLA_KIND_BV_SMOD");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_SMUL_OVERFLOW =
      BitwuzlaKind.of("BITWUZLA_KIND_BV_SMUL_OVERFLOW");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_SREM = BitwuzlaKind.of("BITWUZLA_KIND_BV_SREM");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_SSUB_OVERFLOW =
      BitwuzlaKind.of("BITWUZLA_KIND_BV_SSUB_OVERFLOW");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_SUB = BitwuzlaKind.of("BITWUZLA_KIND_BV_SUB");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_UADD_OVERFLOW =
      BitwuzlaKind.of("BITWUZLA_KIND_BV_UADD_OVERFLOW");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_UDIV = BitwuzlaKind.of("BITWUZLA_KIND_BV_UDIV");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_UGE = BitwuzlaKind.of("BITWUZLA_KIND_BV_UGE");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_UGT = BitwuzlaKind.of("BITWUZLA_KIND_BV_UGT");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_ULE = BitwuzlaKind.of("BITWUZLA_KIND_BV_ULE");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_ULT = BitwuzlaKind.of("BITWUZLA_KIND_BV_ULT");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_UMUL_OVERFLOW =
      BitwuzlaKind.of("BITWUZLA_KIND_BV_UMUL_OVERFLOW");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_UREM = BitwuzlaKind.of("BITWUZLA_KIND_BV_UREM");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_USUB_OVERFLOW =
      BitwuzlaKind.of("BITWUZLA_KIND_BV_USUB_OVERFLOW");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_XNOR = BitwuzlaKind.of("BITWUZLA_KIND_BV_XNOR");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_XOR = BitwuzlaKind.of("BITWUZLA_KIND_BV_XOR");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_EXTRACT =
      BitwuzlaKind.of("BITWUZLA_KIND_BV_EXTRACT");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_REPEAT =
      BitwuzlaKind.of("BITWUZLA_KIND_BV_REPEAT");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_ROLI = BitwuzlaKind.of("BITWUZLA_KIND_BV_ROLI");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_RORI = BitwuzlaKind.of("BITWUZLA_KIND_BV_RORI");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_SIGN_EXTEND =
      BitwuzlaKind.of("BITWUZLA_KIND_BV_SIGN_EXTEND");
  public static final BitwuzlaKind BITWUZLA_KIND_BV_ZERO_EXTEND =
      BitwuzlaKind.of("BITWUZLA_KIND_BV_ZERO_EXTEND");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_ABS = BitwuzlaKind.of("BITWUZLA_KIND_FP_ABS");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_ADD = BitwuzlaKind.of("BITWUZLA_KIND_FP_ADD");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_DIV = BitwuzlaKind.of("BITWUZLA_KIND_FP_DIV");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_EQUAL =
      BitwuzlaKind.of("BITWUZLA_KIND_FP_EQUAL");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_FMA = BitwuzlaKind.of("BITWUZLA_KIND_FP_FMA");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_FP = BitwuzlaKind.of("BITWUZLA_KIND_FP_FP");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_GEQ = BitwuzlaKind.of("BITWUZLA_KIND_FP_GEQ");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_GT = BitwuzlaKind.of("BITWUZLA_KIND_FP_GT");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_IS_INF =
      BitwuzlaKind.of("BITWUZLA_KIND_FP_IS_INF");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_IS_NAN =
      BitwuzlaKind.of("BITWUZLA_KIND_FP_IS_NAN");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_IS_NEG =
      BitwuzlaKind.of("BITWUZLA_KIND_FP_IS_NEG");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_IS_NORMAL =
      BitwuzlaKind.of("BITWUZLA_KIND_FP_IS_NORMAL");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_IS_POS =
      BitwuzlaKind.of("BITWUZLA_KIND_FP_IS_POS");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_IS_SUBNORMAL =
      BitwuzlaKind.of("BITWUZLA_KIND_FP_IS_SUBNORMAL");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_IS_ZERO =
      BitwuzlaKind.of("BITWUZLA_KIND_FP_IS_ZERO");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_LEQ = BitwuzlaKind.of("BITWUZLA_KIND_FP_LEQ");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_LT = BitwuzlaKind.of("BITWUZLA_KIND_FP_LT");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_MAX = BitwuzlaKind.of("BITWUZLA_KIND_FP_MAX");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_MIN = BitwuzlaKind.of("BITWUZLA_KIND_FP_MIN");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_MUL = BitwuzlaKind.of("BITWUZLA_KIND_FP_MUL");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_NEG = BitwuzlaKind.of("BITWUZLA_KIND_FP_NEG");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_REM = BitwuzlaKind.of("BITWUZLA_KIND_FP_REM");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_RTI = BitwuzlaKind.of("BITWUZLA_KIND_FP_RTI");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_SQRT = BitwuzlaKind.of("BITWUZLA_KIND_FP_SQRT");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_SUB = BitwuzlaKind.of("BITWUZLA_KIND_FP_SUB");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_TO_FP_FROM_BV =
      BitwuzlaKind.of("BITWUZLA_KIND_FP_TO_FP_FROM_BV");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_TO_FP_FROM_FP =
      BitwuzlaKind.of("BITWUZLA_KIND_FP_TO_FP_FROM_FP");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_TO_FP_FROM_SBV =
      BitwuzlaKind.of("BITWUZLA_KIND_FP_TO_FP_FROM_SBV");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_TO_FP_FROM_UBV =
      BitwuzlaKind.of("BITWUZLA_KIND_FP_TO_FP_FROM_UBV");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_TO_SBV =
      BitwuzlaKind.of("BITWUZLA_KIND_FP_TO_SBV");
  public static final BitwuzlaKind BITWUZLA_KIND_FP_TO_UBV =
      BitwuzlaKind.of("BITWUZLA_KIND_FP_TO_UBV");
  public static final BitwuzlaKind BITWUZLA_KIND_NUM_KINDS =
      BitwuzlaKind.of("BITWUZLA_KIND_NUM_KINDS");

  public int swigValue() {
    return swigValue;
  }

  @Override
  public String toString() {
    return swigName;
  }

  public static BitwuzlaKind swigToEnum(int swigValue) {
    if (swigValue < swigValues.length
        && swigValue >= 0
        && swigValues[swigValue].swigValue == swigValue) {
      return swigValues[swigValue];
    }
    for (int i = 0; i < swigValues.length; i++) {
      if (swigValues[i].swigValue == swigValue) {
        return swigValues[i];
      }
    }
    throw new IllegalArgumentException(
        "No enum " + BitwuzlaKind.class + " with value " + swigValue);
  }

  private BitwuzlaKind(String swigName) {
    this.swigName = swigName;
    this.swigValue = BitwuzlaKind.swigNext;
  }

  private static BitwuzlaKind of(String swigName) {
    BitwuzlaKind kind = new BitwuzlaKind(swigName);
    BitwuzlaKind.swigNext++;
    return kind;
  }

  private BitwuzlaKind(String swigName, int swigValue) {
    this.swigName = swigName;
    this.swigValue = swigValue;
  }

  private static BitwuzlaKind of(String swigName, int swigValue) {
    BitwuzlaKind kind = new BitwuzlaKind(swigName, swigValue);
    BitwuzlaKind.swigNext = swigValue + 1;
    return kind;
  }

  private static BitwuzlaKind[] swigValues = {
    BITWUZLA_KIND_CONSTANT,
    BITWUZLA_KIND_CONST_ARRAY,
    BITWUZLA_KIND_VALUE,
    BITWUZLA_KIND_VARIABLE,
    BITWUZLA_KIND_AND,
    BITWUZLA_KIND_DISTINCT,
    BITWUZLA_KIND_EQUAL,
    BITWUZLA_KIND_IFF,
    BITWUZLA_KIND_IMPLIES,
    BITWUZLA_KIND_NOT,
    BITWUZLA_KIND_OR,
    BITWUZLA_KIND_XOR,
    BITWUZLA_KIND_ITE,
    BITWUZLA_KIND_EXISTS,
    BITWUZLA_KIND_FORALL,
    BITWUZLA_KIND_APPLY,
    BITWUZLA_KIND_LAMBDA,
    BITWUZLA_KIND_ARRAY_SELECT,
    BITWUZLA_KIND_ARRAY_STORE,
    BITWUZLA_KIND_BV_ADD,
    BITWUZLA_KIND_BV_AND,
    BITWUZLA_KIND_BV_ASHR,
    BITWUZLA_KIND_BV_COMP,
    BITWUZLA_KIND_BV_CONCAT,
    BITWUZLA_KIND_BV_DEC,
    BITWUZLA_KIND_BV_INC,
    BITWUZLA_KIND_BV_MUL,
    BITWUZLA_KIND_BV_NAND,
    BITWUZLA_KIND_BV_NEG,
    BITWUZLA_KIND_BV_NOR,
    BITWUZLA_KIND_BV_NOT,
    BITWUZLA_KIND_BV_OR,
    BITWUZLA_KIND_BV_REDAND,
    BITWUZLA_KIND_BV_REDOR,
    BITWUZLA_KIND_BV_REDXOR,
    BITWUZLA_KIND_BV_ROL,
    BITWUZLA_KIND_BV_ROR,
    BITWUZLA_KIND_BV_SADD_OVERFLOW,
    BITWUZLA_KIND_BV_SDIV_OVERFLOW,
    BITWUZLA_KIND_BV_SDIV,
    BITWUZLA_KIND_BV_SGE,
    BITWUZLA_KIND_BV_SGT,
    BITWUZLA_KIND_BV_SHL,
    BITWUZLA_KIND_BV_SHR,
    BITWUZLA_KIND_BV_SLE,
    BITWUZLA_KIND_BV_SLT,
    BITWUZLA_KIND_BV_SMOD,
    BITWUZLA_KIND_BV_SMUL_OVERFLOW,
    BITWUZLA_KIND_BV_SREM,
    BITWUZLA_KIND_BV_SSUB_OVERFLOW,
    BITWUZLA_KIND_BV_SUB,
    BITWUZLA_KIND_BV_UADD_OVERFLOW,
    BITWUZLA_KIND_BV_UDIV,
    BITWUZLA_KIND_BV_UGE,
    BITWUZLA_KIND_BV_UGT,
    BITWUZLA_KIND_BV_ULE,
    BITWUZLA_KIND_BV_ULT,
    BITWUZLA_KIND_BV_UMUL_OVERFLOW,
    BITWUZLA_KIND_BV_UREM,
    BITWUZLA_KIND_BV_USUB_OVERFLOW,
    BITWUZLA_KIND_BV_XNOR,
    BITWUZLA_KIND_BV_XOR,
    BITWUZLA_KIND_BV_EXTRACT,
    BITWUZLA_KIND_BV_REPEAT,
    BITWUZLA_KIND_BV_ROLI,
    BITWUZLA_KIND_BV_RORI,
    BITWUZLA_KIND_BV_SIGN_EXTEND,
    BITWUZLA_KIND_BV_ZERO_EXTEND,
    BITWUZLA_KIND_FP_ABS,
    BITWUZLA_KIND_FP_ADD,
    BITWUZLA_KIND_FP_DIV,
    BITWUZLA_KIND_FP_EQUAL,
    BITWUZLA_KIND_FP_FMA,
    BITWUZLA_KIND_FP_FP,
    BITWUZLA_KIND_FP_GEQ,
    BITWUZLA_KIND_FP_GT,
    BITWUZLA_KIND_FP_IS_INF,
    BITWUZLA_KIND_FP_IS_NAN,
    BITWUZLA_KIND_FP_IS_NEG,
    BITWUZLA_KIND_FP_IS_NORMAL,
    BITWUZLA_KIND_FP_IS_POS,
    BITWUZLA_KIND_FP_IS_SUBNORMAL,
    BITWUZLA_KIND_FP_IS_ZERO,
    BITWUZLA_KIND_FP_LEQ,
    BITWUZLA_KIND_FP_LT,
    BITWUZLA_KIND_FP_MAX,
    BITWUZLA_KIND_FP_MIN,
    BITWUZLA_KIND_FP_MUL,
    BITWUZLA_KIND_FP_NEG,
    BITWUZLA_KIND_FP_REM,
    BITWUZLA_KIND_FP_RTI,
    BITWUZLA_KIND_FP_SQRT,
    BITWUZLA_KIND_FP_SUB,
    BITWUZLA_KIND_FP_TO_FP_FROM_BV,
    BITWUZLA_KIND_FP_TO_FP_FROM_FP,
    BITWUZLA_KIND_FP_TO_FP_FROM_SBV,
    BITWUZLA_KIND_FP_TO_FP_FROM_UBV,
    BITWUZLA_KIND_FP_TO_SBV,
    BITWUZLA_KIND_FP_TO_UBV,
    BITWUZLA_KIND_NUM_KINDS,
  };
}
