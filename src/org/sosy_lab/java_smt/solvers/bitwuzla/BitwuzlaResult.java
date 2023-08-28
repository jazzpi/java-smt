package org.sosy_lab.java_smt.solvers.bitwuzla;

/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

public final class BitwuzlaResult {
  public static final BitwuzlaResult BITWUZLA_SAT =
      new BitwuzlaResult("BITWUZLA_SAT", bitwuzlaJNI.BITWUZLA_SAT_get());
  public static final BitwuzlaResult BITWUZLA_UNSAT =
      new BitwuzlaResult("BITWUZLA_UNSAT", bitwuzlaJNI.BITWUZLA_UNSAT_get());
  public static final BitwuzlaResult BITWUZLA_UNKNOWN =
      new BitwuzlaResult("BITWUZLA_UNKNOWN", bitwuzlaJNI.BITWUZLA_UNKNOWN_get());

  public final int swigValue() {
    return swigValue;
  }

  public String toString() {
    return swigName;
  }

  public static BitwuzlaResult swigToEnum(int swigValue) {
    if (swigValue < swigValues.length
        && swigValue >= 0
        && swigValues[swigValue].swigValue == swigValue) return swigValues[swigValue];
    for (int i = 0; i < swigValues.length; i++)
      if (swigValues[i].swigValue == swigValue) return swigValues[i];
    throw new IllegalArgumentException(
        "No enum " + BitwuzlaResult.class + " with value " + swigValue);
  }

  private BitwuzlaResult(String swigName) {
    this.swigName = swigName;
    this.swigValue = swigNext++;
  }

  private BitwuzlaResult(String swigName, int swigValue) {
    this.swigName = swigName;
    this.swigValue = swigValue;
    swigNext = swigValue + 1;
  }

  private BitwuzlaResult(String swigName, BitwuzlaResult swigEnum) {
    this.swigName = swigName;
    this.swigValue = swigEnum.swigValue;
    swigNext = this.swigValue + 1;
  }

  private static BitwuzlaResult[] swigValues = {BITWUZLA_SAT, BITWUZLA_UNSAT, BITWUZLA_UNKNOWN};
  private static int swigNext = 0;
  private final int swigValue;
  private final String swigName;
}
