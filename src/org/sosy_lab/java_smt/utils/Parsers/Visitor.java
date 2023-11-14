package org.sosy_lab.java_smt.utils.Parsers;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.sosy_lab.java_smt.api.ArrayFormula;
import org.sosy_lab.java_smt.api.ArrayFormulaManager;
import org.sosy_lab.java_smt.api.BitvectorFormula;
import org.sosy_lab.java_smt.api.BitvectorFormulaManager;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;
import org.sosy_lab.java_smt.api.Formula;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.FormulaType;
import org.sosy_lab.java_smt.api.FormulaType.BitvectorType;
import org.sosy_lab.java_smt.api.FunctionDeclaration;
import org.sosy_lab.java_smt.api.IntegerFormulaManager;
import org.sosy_lab.java_smt.api.Model;
import org.sosy_lab.java_smt.api.Model.ValueAssignment;
import org.sosy_lab.java_smt.api.NumeralFormula;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;
import org.sosy_lab.java_smt.api.NumeralFormula.RationalFormula;
import org.sosy_lab.java_smt.api.RationalFormulaManager;
import org.sosy_lab.java_smt.api.UFManager;
import org.sosy_lab.java_smt.utils.Generators.Generator;
import org.sosy_lab.java_smt.utils.Generators.Tuple;
import scala.Tuple2;


@SuppressWarnings({"CheckReturnValue", "unchecked"})
public class Visitor extends smtlibv2BaseVisitor<Object> {

  public static HashMap<String, ParserFormula> variables = new HashMap<>();
  public static HashMap<String, ParserFormula> letVariables = new HashMap<>();
  public static List<BooleanFormula> constraints = new ArrayList<>();
  private final FormulaManager fmgr;
  private final BooleanFormulaManager bmgr;
  private final @Nullable IntegerFormulaManager imgr;
  private final @Nullable RationalFormulaManager rmgr;
  private final @Nullable BitvectorFormulaManager bimgr;
  private final @Nullable ArrayFormulaManager amgr;
  private final UFManager umgr;
  List<Model.ValueAssignment> assignments = new ArrayList<>();

  public HashMap<String, ParserFormula> getVariables() {
    return variables;
  }

  public List<BooleanFormula> getConstraints() {
    return constraints;
  }

  public List<ValueAssignment> getAssignments() {
    return assignments;
  }

  public boolean isModel = false;

  public Visitor(
      FormulaManager fmgr, BooleanFormulaManager bmgr,
      @Nullable IntegerFormulaManager imgr,
      @Nullable RationalFormulaManager rmgr, @Nullable BitvectorFormulaManager bimgr,
      @Nullable ArrayFormulaManager amgr, UFManager umgr) {
    this.fmgr = fmgr;
    this.bmgr = bmgr;
    this.imgr = imgr;
    this.rmgr = rmgr;
    this.bimgr = bimgr;
    this.amgr = amgr;
    this.umgr = umgr;


  }

  @Override public Object visitStart_script(smtlibv2Parser.Start_scriptContext ctx) {
    BooleanFormula constraint = bmgr.and(constraints);
    return visitChildren(ctx);
  }

  @Override public String visitSpec_constant_bin(smtlibv2Parser.Spec_constant_binContext ctx) {
    String binary = ctx.getText();
    return binary;
  }

  @Override public List<String> visitId_symb(smtlibv2Parser.Id_symbContext ctx) {
    List<String> sort = new ArrayList<>();
    sort.add(ctx.getText());
    return sort;
  }

  @Override public List<String> visitId_symb_idx(smtlibv2Parser.Id_symb_idxContext ctx) {
    List<String> sort = new ArrayList<>();
    sort.add(ctx.symbol().getText());
    for (int i = 0; i < ctx.index().size(); i++) {
      sort.add(ctx.index(i).getText());
    }
    return sort;
  }

  @Override public FormulaType.ArrayFormulaType<?,?> visitMultisort(smtlibv2Parser.MultisortContext ctx) {
    FormulaType<?> idx = (FormulaType<?>) visit(ctx.sort(0));
    FormulaType<?> elem = (FormulaType<?>) visit(ctx.sort(1));
    FormulaType.ArrayFormulaType<?,?> result = FormulaType.getArrayType(idx, elem);

    return result;
  }

  @Override public FormulaType<?> visitSort_id(smtlibv2Parser.Sort_idContext ctx) {
    String type = ctx.getText();

    String bvSize = "";
    FormulaType<?> formulaType;
    if (type.startsWith("(_BitVec")) {
      bvSize = type.split("_BitVec")[1];
      bvSize = bvSize.split("\\)")[0];
      type = "BitVec";
    }

    switch (type) {
      case "Int":
        return FormulaType.IntegerType;
      case "Bool":
        return FormulaType.BooleanType;
      case "Real":
        return FormulaType.RationalType;
      case "BitVec":
        return FormulaType.getBitvectorTypeWithSize(Integer.parseInt(bvSize));
      default:
        throw new ParserException(type + " is not a known Array sort. ");
    }
  }

  @Override public Tuple2<String, FormulaType<?>> visitQual_id_sort(smtlibv2Parser.Qual_id_sortContext ctx) {
    String operator = ctx.identifier().getText();
    FormulaType<?> sort = (FormulaType<?>) visit(ctx.sort());
    Tuple2<String, FormulaType<?>> result = new Tuple2<>(operator, sort);
    return result;
  }

  @Override public Object visitVar_binding(smtlibv2Parser.Var_bindingContext ctx) {
    String name = ctx.symbol().getText();
    Formula formula = (Formula) visit(ctx.term());
    letVariables.put(name, new ParserFormula(name, formula));
    return visitChildren(ctx);
  }

  public static boolean isInteger(String strNum) {
    try {
      Integer d = Integer.parseInt(strNum);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }

  public static boolean isFloat(String strNum) {
    try {
      Float d = Float.parseFloat(strNum);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }
  public static boolean isDouble(String strNum) {
    try {
      double d = Double.parseDouble(strNum);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }

  public static boolean isLong(String strNum) {
    try {
      Long d = Long.parseLong(strNum);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }

  public static boolean isBigInteger(String strNum) {
    try {
      BigInteger d = new BigInteger(strNum);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }

  public static String getNumericType(String strNum) {
    if (isInteger(strNum)) {
      return "Integer";
    } else if (isLong(strNum)) {
      return "Long";
    } else if (isDouble(strNum)) {
      return "Double";
    } else if (isBigInteger(strNum)) {
      return "BigInteger";
    } else if (isFloat(strNum)) {
      return "Float";
    } else {
      return "other";
    }
  }

  @Override public Object visitTerm_spec_const(smtlibv2Parser.Term_spec_constContext ctx) {
    String operand = ctx.getText();
    if (variables.containsKey(operand)) {
      return variables.get(operand).javaSmt;
    } else if (getNumericType(operand).equals("Integer") | getNumericType(operand).equals("Long")) {
      variables.put(operand, new ParserFormula("Int", Objects.requireNonNull(imgr).makeNumber(operand)));
      return variables.get(operand).javaSmt;
    } else if (getNumericType(operand).equals("Double") | getNumericType(operand).equals("Float")) {
      variables.put(operand, new ParserFormula("Real", Objects.requireNonNull(rmgr).makeNumber(operand)));
      return variables.get(operand).javaSmt;
    } else if (operand.startsWith("#b")) {
      String binVal = operand.split("b")[1];
      int index = binVal.length();
      int value = Integer.parseInt(binVal, 2);
      return Objects.requireNonNull(bimgr).makeBitvector(index, value);
    } else if (operand.startsWith("#x")) {
      String hexVal = operand.split("x")[1];
      int index = (hexVal.length()*4);
      BigInteger value = new BigInteger(hexVal, 16);
      return Objects.requireNonNull(bimgr).makeBitvector(index, value);
    } else {
      throw new ParserException("Operand " + operand + " is unknown.");
    }
  }

  @Override public Object visitTerm_qual_id(smtlibv2Parser.Term_qual_idContext ctx) {
    String operand = ctx.getText();
    if (operand.startsWith("|")) {
      operand = operand.replaceAll("\\|", "PIPE");
    }
    List<String> bitVec = (List<String>) visitChildren(ctx);
    if (letVariables.containsKey(operand)) {
      if (letVariables.get(operand).type.equals("UF") && variables.get(operand).inputParams.isEmpty()) {
        return umgr.callUF((FunctionDeclaration<Formula>) letVariables.get(operand).javaSmt,
            new ArrayList<>());
      }
      return letVariables.get(operand).javaSmt;
    } else if (variables.containsKey(operand)) {
      if (variables.get(operand).type.equals("UF") && variables.get(operand).inputParams.isEmpty()) {
        return umgr.callUF((FunctionDeclaration<Formula>) variables.get(operand).javaSmt,
            new ArrayList<>());
      }
      return variables.get(operand).javaSmt;
    } else if (operand.equals("false")) {
      variables.put(operand, new ParserFormula("Bool", bmgr.makeFalse()));
      return variables.get(operand).javaSmt;
    } else if (operand.equals("true")) {
      variables.put(operand, new ParserFormula("Bool", bmgr.makeTrue()));
      return variables.get(operand).javaSmt;
    } else if (!bitVec.isEmpty()) {
      BigInteger value = new BigInteger(bitVec.get(0).split("v")[1]);
      int index = Integer.parseInt(bitVec.get(1));
      return Objects.requireNonNull(bimgr).makeBitvector(index, value);
    } else {
      throw new ParserException("Operand " + operand + " is unknown.");
    }
  }

  public void getOperands(smtlibv2Parser.MultitermContext ctx,
                          List<Formula> operands) {

    for (int i = 0; i < ctx.term().size(); ++i) {
      Object operand = visit(ctx.term(i));
      // do not add multi term to list of operands
      if (operand != null) {
        operands.add((Formula) operand);
      }
    }
  }

  @Override public Object visitMultiterm(smtlibv2Parser.MultitermContext ctx) {
    //String operator = ctx.qual_identifer().getText();
    Object identifier = visit(ctx.qual_identifer());
    List<String> operators = null;
    String operator = "";
    FormulaType<?> sort = null;
    if (identifier instanceof List) {
      operators = (List<String>) identifier;
      operator = Objects.requireNonNull(operators).get(0);
    } else if (identifier instanceof Tuple2) {
      operator = (String) ((Tuple2) identifier)._1;
      sort = (FormulaType<?>) ((Tuple2) identifier)._2;
    }

    //String binary = (String) visit(ctx.b);

    Object ufOperator = null;
    if (variables.containsKey(operator)) {
      ufOperator = variables.get(operator).javaSmt;
      operator = "UF";
    }

    List<Formula> operands = new ArrayList<>();
    getOperands(ctx, operands);
    switch (operator) {
      //boolean operators
      case "and":
        try {
          List<BooleanFormula> booleanOperands =
              operands.stream().map(e -> (BooleanFormula) e).collect(Collectors.toList());
          return bmgr.and(booleanOperands);
        } catch (Exception e) {
          throw new ParserException("Operands for " + operator + " need to be of Boolean type");
        }
      case "or":
        try {
          List<BooleanFormula> booleanOperands =
              operands.stream().map(e -> (BooleanFormula) e).collect(Collectors.toList());
          return bmgr.or(booleanOperands);
        } catch (Exception e) {
          throw new ParserException("Operands for " + operator + " need to be of Boolean type");
        }
      case "xor":
        if (operands.size() != 2) {
          throw new ParserException(operator + " takes two boolean operands as input.");
        } else {
          try {
            Iterator<Formula> it = operands.iterator();
            return bmgr.xor((BooleanFormula) it.next(), (BooleanFormula) it.next());
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of Boolean type");
          }
        }
      case "not":
        if (operands.size() != 1) {
          throw new ParserException(operator + " takes one boolean operand as input.");
        } else {
          try {
            Iterator<Formula> it = operands.iterator();
            return bmgr.not((BooleanFormula) it.next());
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of Boolean type");
          }
        }
      case "=>":
        if (operands.size() != 2) {
          throw new ParserException(operator + " takes two boolean operands as input.");
        } else {
          try {
            Iterator<Formula> it = operands.iterator();
            return bmgr.implication((BooleanFormula) it.next(), (BooleanFormula) it.next());
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of Boolean type");
          }
        }
      case "ite":
        if (operands.size() != 3) {
          throw new ParserException(operator + " takes three boolean operands as input.");
        } else {
          try {
            Iterator<Formula> it = operands.iterator();
            return bmgr.ifThenElse((BooleanFormula) it.next(), (BooleanFormula) it.next(),
                (BooleanFormula) it.next());
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of Boolean type");
          }
        }
        //numeral operators
      case "+":
        if (!operands.isEmpty()) {
          try {
            if (operands.stream().anyMatch(c -> c instanceof RationalFormula)) {
              List<NumeralFormula> numeralOperands =
                  operands.stream().map(e -> (RationalFormula) e).collect(Collectors.toList());
              return Objects.requireNonNull(rmgr).sum(numeralOperands);
            } else {
              List<IntegerFormula> integerOperands =
                  operands.stream().map(e -> (IntegerFormula) e).collect(Collectors.toList());
              return Objects.requireNonNull(imgr).sum(integerOperands);
            }
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of numeral type");
          }
        } else {
          throw new ParserException(operator + " takes at least one numeral operand as input. ");
        }
      case "-":
        if (operands.size() == 2) {
          try {
            if (operands.stream().anyMatch(c -> c instanceof RationalFormula)) {
              List<NumeralFormula> numeralOperands =
                  operands.stream().map(e -> (RationalFormula) e).collect(Collectors.toList());
              return Objects.requireNonNull(rmgr).subtract(numeralOperands.get(0), numeralOperands.get(1));
            } else {
              List<IntegerFormula> integerOperands =
                  operands.stream().map(e -> (IntegerFormula) e).collect(Collectors.toList());
              return Objects.requireNonNull(imgr).subtract(integerOperands.get(0), integerOperands.get(1));
            }
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of numeral type");
          }
        } else if (operands.size() == 1) {
          try {
            if (operands.stream().anyMatch(c -> c instanceof RationalFormula)) {
              List<NumeralFormula> numeralOperands =
                  operands.stream().map(e -> (RationalFormula) e).collect(Collectors.toList());
              return Objects.requireNonNull(rmgr).negate(numeralOperands.get(0));
            } else {
              List<IntegerFormula> integerOperands =
                  operands.stream().map(e -> (IntegerFormula) e).collect(Collectors.toList());
              return Objects.requireNonNull(imgr).negate(integerOperands.get(0));
            }
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of numeral type");
          }
        } else {
          throw new ParserException(operator + " takes either one or two numeral operands as input. ");
        }
      case "div":
        if (operands.size() == 2) {
          try {
            if (operands.stream().anyMatch(c -> c instanceof RationalFormula)) {
              List<NumeralFormula> numeralOperands =
                  operands.stream().map(e -> (RationalFormula) e).collect(Collectors.toList());
              return Objects.requireNonNull(rmgr).divide(numeralOperands.get(0), numeralOperands.get(1));
            } else {
              List<IntegerFormula> integerOperands =
                  operands.stream().map(e -> (IntegerFormula) e).collect(Collectors.toList());
              return Objects.requireNonNull(imgr).divide(integerOperands.get(0), integerOperands.get(1));
            }
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of numeral type");
          }
        } else {
          throw new ParserException(operator + " takes two numeral operands as input. ");
        }
      case "mod":
        if (operands.size() == 2) {
          try {
            if (operands.stream().anyMatch(c -> c instanceof RationalFormula)) {
              throw new ParserException(operator + " is not available for Reals. ");
            } else {
              List<IntegerFormula> integerOperands =
                  operands.stream().map(e -> (IntegerFormula) e).collect(Collectors.toList());
              return Objects.requireNonNull(imgr).modulo(integerOperands.get(0), integerOperands.get(1));
            }
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of integer type");
          }
        } else {
          throw new ParserException(operator + " takes two integer operands as input. ");
        }
      case "*":
        if (operands.size() == 2) {
          try {
            if (operands.stream().anyMatch(c -> c instanceof RationalFormula)) {
              List<NumeralFormula> numeralOperands =
                  operands.stream().map(e -> (RationalFormula) e).collect(Collectors.toList());
              return Objects.requireNonNull(rmgr).multiply(numeralOperands.get(0), numeralOperands.get(1));
            } else {
              List<IntegerFormula> integerOperands =
                  operands.stream().map(e -> (IntegerFormula) e).collect(Collectors.toList());
              return Objects.requireNonNull(imgr).multiply(integerOperands.get(0), integerOperands.get(1));
            }
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of numeral type");
          }
        } else {
          throw new ParserException(operator + " takes two numeral operands as input. ");
        }
      case "distinct":
        if (!operands.isEmpty()) {
          try {
            if (operands.stream().anyMatch(c -> c instanceof RationalFormula)) {
              List<NumeralFormula> numeralOperands =
                  operands.stream().map(e -> (RationalFormula) e).collect(Collectors.toList());
              return Objects.requireNonNull(rmgr).distinct(numeralOperands);
            } else {
              List<IntegerFormula> integerOperands =
                  operands.stream().map(e -> (IntegerFormula) e).collect(Collectors.toList());
              return Objects.requireNonNull(imgr).distinct(integerOperands);
            }
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of numeral type");
          }
        } else {
          throw new ParserException(operator + " takes at least one numeral operand as input. ");
        }
      case ">":
        if (operands.size() == 2) {
          try {
            if (operands.stream().anyMatch(c -> c instanceof RationalFormula)) {
              List<NumeralFormula> numeralOperands =
                  operands.stream().map(e -> (RationalFormula) e).collect(Collectors.toList());
              return Objects.requireNonNull(rmgr)
                  .greaterOrEquals(numeralOperands.get(0), numeralOperands.get(1));
            } else {
              List<IntegerFormula> integerOperands =
                  operands.stream().map(e -> (IntegerFormula) e).collect(Collectors.toList());
              return Objects.requireNonNull(imgr)
                  .greaterThan(integerOperands.get(0), integerOperands.get(1));
            }
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of numeral type");
          }
        } else {
          throw new ParserException(operator + " takes two numeral operands as input. ");
        }
      case ">=":
        if (operands.size() == 2) {
          try {
            if (operands.stream().anyMatch(c -> c instanceof RationalFormula)) {
              List<NumeralFormula> numeralOperands =
                  operands.stream().map(e -> (RationalFormula) e).collect(Collectors.toList());
              return Objects.requireNonNull(rmgr)
                  .greaterOrEquals(numeralOperands.get(0), numeralOperands.get(1));
            } else {
              List<IntegerFormula> integerOperands =
                  operands.stream().map(e -> (IntegerFormula) e).collect(Collectors.toList());
              return Objects.requireNonNull(imgr)
                  .greaterOrEquals(integerOperands.get(0), integerOperands.get(1));
            }
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of numeral type");
          }
        } else {
          throw new ParserException(operator + " takes two numeral operands as input. ");
        }
      case "<":
        if (operands.size() == 2) {
          try {
            if (operands.stream().anyMatch(c -> c instanceof RationalFormula)) {
              List<NumeralFormula> numeralOperands =
                  operands.stream().map(e -> (RationalFormula) e).collect(Collectors.toList());
              return Objects.requireNonNull(rmgr).lessThan(numeralOperands.get(0), numeralOperands.get(1));
            } else {
              List<IntegerFormula> integerOperands =
                  operands.stream().map(e -> (IntegerFormula) e).collect(Collectors.toList());
              return Objects.requireNonNull(imgr).lessThan(integerOperands.get(0), integerOperands.get(1));
            }
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of numeral type");
          }
        } else {
          throw new ParserException(operator + " takes two numeral operands as input. ");
        }
      case "<=":
        if (operands.size() == 2) {
          try {
            if (operands.stream().anyMatch(c -> c instanceof RationalFormula)) {
              List<NumeralFormula> numeralOperands =
                  operands.stream().map(e -> (RationalFormula) e).collect(Collectors.toList());
              return Objects.requireNonNull(rmgr)
                  .lessOrEquals(numeralOperands.get(0), numeralOperands.get(1));
            } else {
              List<IntegerFormula> integerOperands =
                  operands.stream().map(e -> (IntegerFormula) e).collect(Collectors.toList());
              return Objects.requireNonNull(imgr)
                  .lessOrEquals(integerOperands.get(0), integerOperands.get(1));
            }
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of numeral type");
          }
        } else {
          throw new ParserException(operator + " takes two numeral operands as input. ");
        }

      case "to_int":
        if (operands.size() == 1) {
          try {
            List<NumeralFormula> numeralOperands =
                operands.stream().map(e -> (RationalFormula) e).collect(Collectors.toList());
            return Objects.requireNonNull(rmgr).floor(numeralOperands.get(0));
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of real type");
          }
        } else {
          throw new ParserException(operator + " takes one real operands as input. ");
        }

        //BitVec operators
      case "bvneg":
        if (operands.size() != 1) {
          throw new ParserException(operator + " takes one bitvector operand as input.");
        } else {
          try {
            return Objects.requireNonNull(bimgr).negate((BitvectorFormula) operands.get(0));
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of bitvector type");
          }
        }
      case "bvadd":
        if (operands.size() != 2) {
          throw new ParserException(operator + " takes two bitvector operand as input.");
        } else {
          try {
            return Objects.requireNonNull(bimgr).add((BitvectorFormula) operands.get(0),
                (BitvectorFormula) operands.get(1));
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + " need to be of bitvector type");
          }
        }
      case "bvsub":
        if (operands.size() != 2) {
          throw new ParserException(operator + " takes two bitvector operand as input.");
        } else {
          try {
            return Objects.requireNonNull(bimgr).subtract((BitvectorFormula) operands.get(0),
                (BitvectorFormula) operands.get(1));
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of bitvector type");
          }
        }
      case "bvsdiv":
        if (operands.size() != 2) {
          throw new ParserException(operator + " takes two bitvector operand as input.");
        } else {
          try {
            return Objects.requireNonNull(bimgr).divide((BitvectorFormula) operands.get(0),
                (BitvectorFormula) operands.get(1), true);
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of bitvector type");
          }
        }
      case "bvudiv":
        if (operands.size() != 2) {
          throw new ParserException(operator + " takes two bitvector operand as input.");
        } else {
          try {
            return Objects.requireNonNull(bimgr).divide((BitvectorFormula) operands.get(0),
                (BitvectorFormula) operands.get(1), false);
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of bitvector type");
          }
        }
      case "bvsrem":
        if (operands.size() != 2) {
          throw new ParserException(operator + " takes two bitvector operand as input.");
        } else {
          try {
            return Objects.requireNonNull(bimgr).modulo((BitvectorFormula) operands.get(0),
                (BitvectorFormula) operands.get(1), true);
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of bitvector type");
          }
        }
      case "bvurem":
        if (operands.size() != 2) {
          throw new ParserException(operator + " takes two bitvector operand as input.");
        } else {
          try {
            return Objects.requireNonNull(bimgr).modulo((BitvectorFormula) operands.get(0),
                (BitvectorFormula) operands.get(1), false);
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of bitvector type");
          }
        }
      case "bvmul":
        if (operands.size() != 2) {
          throw new ParserException(operator + " takes two bitvector operand as input.");
        } else {
          try {
            return Objects.requireNonNull(bimgr).multiply((BitvectorFormula) operands.get(0),
                (BitvectorFormula) operands.get(1));
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of bitvector type");
          }
        }
      case "bvsgt":
        if (operands.size() != 2) {
          throw new ParserException(operator + " takes two bitvector operand as input.");
        } else {
          try {
            return Objects.requireNonNull(bimgr).greaterThan((BitvectorFormula) operands.get(0),
                (BitvectorFormula) operands.get(1), true);
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of bitvector type");
          }
        }
      case "bvugt":
        if (operands.size() != 2) {
          throw new ParserException(operator + " takes two bitvector operand as input.");
        } else {
          try {
            return Objects.requireNonNull(bimgr).greaterThan((BitvectorFormula) operands.get(0),
                (BitvectorFormula) operands.get(1), false);
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of bitvector type");
          }
        }
      case "bvsge":
        if (operands.size() != 2) {
          throw new ParserException(operator + " takes two bitvector operand as input.");
        } else {
          try {
            return Objects.requireNonNull(bimgr).greaterOrEquals((BitvectorFormula) operands.get(0),
                (BitvectorFormula) operands.get(1), true);
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of bitvector type");
          }
        }
      case "bvuge":
        if (operands.size() != 2) {
          throw new ParserException(operator + " takes two bitvector operand as input.");
        } else {
          try {
            return Objects.requireNonNull(bimgr).greaterOrEquals((BitvectorFormula) operands.get(0),
                (BitvectorFormula) operands.get(1), false);
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of bitvector type");
          }
        }
      case "bvslt":
        if (operands.size() != 2) {
          throw new ParserException(operator + " takes two bitvector operand as input.");
        } else {
          try {
            return Objects.requireNonNull(bimgr).lessThan((BitvectorFormula) operands.get(0),
                (BitvectorFormula) operands.get(1), true);
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of bitvector type");
          }
        }
      case "bvult":
        if (operands.size() != 2) {
          throw new ParserException(operator + " takes two bitvector operand as input.");
        } else {
          try {
            return Objects.requireNonNull(bimgr).lessThan((BitvectorFormula) operands.get(0),
                (BitvectorFormula) operands.get(1), false);
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of bitvector type");
          }
        }
      case "bvsle":
        if (operands.size() != 2) {
          throw new ParserException(operator + " takes two bitvector operand as input.");
        } else {
          try {
            return Objects.requireNonNull(bimgr).lessOrEquals((BitvectorFormula) operands.get(0),
                (BitvectorFormula) operands.get(1), true);
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of bitvector type");
          }
        }
      case "bvule":
        if (operands.size() != 2) {
          throw new ParserException(operator + " takes two bitvector operand as input.");
        } else {
          try {
            return Objects.requireNonNull(bimgr).lessOrEquals((BitvectorFormula) operands.get(0),
                (BitvectorFormula) operands.get(1), false);
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of bitvector type");
          }
        }
      case "bvnot":
        if (operands.size() != 1) {
          throw new ParserException(operator + " takes one bitvector operand as input.");
        } else {
          try {
            return Objects.requireNonNull(bimgr).not((BitvectorFormula) operands.get(0));
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of bitvector type");
          }
        }
      case "bvand":
        if (operands.size() != 2) {
          throw new ParserException(operator + " takes two bitvector operand as input.");
        } else {
          try {
            return Objects.requireNonNull(bimgr).and((BitvectorFormula) operands.get(0),
                (BitvectorFormula) operands.get(1));
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of bitvector type");
          }
        }
      case "bvor":
        if (operands.size() != 2) {
          throw new ParserException(operator + " takes two bitvector operand as input.");
        } else {
          try {
            return Objects.requireNonNull(bimgr).or((BitvectorFormula) operands.get(0),
                (BitvectorFormula) operands.get(1));
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of bitvector type");
          }
        }
      case "bvxor":
        if (operands.size() != 2) {
          throw new ParserException(operator + " takes two bitvector operand as input.");
        } else {
          try {
            return Objects.requireNonNull(bimgr).xor((BitvectorFormula) operands.get(0),
                (BitvectorFormula) operands.get(1));
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of bitvector type");
          }
        }
      case "bvashr":
        if (operands.size() != 2) {
          throw new ParserException(operator + " takes two bitvector operand as input.");
        } else {
          try {
            return Objects.requireNonNull(bimgr).shiftRight((BitvectorFormula) operands.get(0),
                (BitvectorFormula) operands.get(1), true);
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of bitvector type");
          }
        }
      case "bvlshr":
        if (operands.size() != 2) {
          throw new ParserException(operator + " takes two bitvector operand as input.");
        } else {
          try {
            return Objects.requireNonNull(bimgr).shiftRight((BitvectorFormula) operands.get(0),
                (BitvectorFormula) operands.get(1), false);
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of bitvector type");
          }
        }
      case "bvshl":
        if (operands.size() != 2) {
          throw new ParserException(operator + " takes two bitvector operand as input.");
        } else {
          try {
            return Objects.requireNonNull(bimgr).shiftLeft((BitvectorFormula) operands.get(0),
                (BitvectorFormula) operands.get(1));
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of bitvector type");
          }
        }
      case "concat":
        if (operands.size() != 2) {
          throw new ParserException(operator + " takes two bitvector operand as input.");
        } else {
          try {
            return Objects.requireNonNull(bimgr).concat((BitvectorFormula) operands.get(0),
                (BitvectorFormula) operands.get(1));
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of bitvector type");
          }
        }
      case "extract":
        if (operands.size() == 1) {
          if (operators.size() == 3 && isInteger(operators.get(2)) && isInteger(operators.get(1))) {
            int left = Integer.parseInt(operators.get(2));
            int right = Integer.parseInt(operators.get(1));
            try {
              return Objects.requireNonNull(bimgr).extract((BitvectorFormula) operands.get(0),
                  left, right);
            } catch (Exception e) {
              throw new ParserException("Operands for " + operator + "need to be of bitvector type");
            }
          } else {
            throw new ParserException(operator + " takes one bitvector and two integers as input. ");
          }
        }
      case "zero_extend":
        if (operands.size() == 1) {
          if (operators.size() == 2 && isInteger(operators.get(1))) {
            int extension = Integer.parseInt(operators.get(1));
            try {
              return Objects.requireNonNull(bimgr).extend((BitvectorFormula) operands.get(0),
                  extension, true);
            } catch (Exception e) {
              throw new ParserException("Operands for " + operator + "need to be of bitvector type");
            }
          } else {
            throw new ParserException(operator + " takes one bitvector and one integer as input. ");
          }
        }
      case "sign_extend":
        if (operands.size() == 1) {
          if (operators.size() == 2 && isInteger(operators.get(1))) {
            int extension = Integer.parseInt(operators.get(1));
            try {
              return Objects.requireNonNull(bimgr).extend((BitvectorFormula) operands.get(0),
                  extension, false);
            } catch (Exception e) {
              throw new ParserException("Operands for " + operator + "need to be of bitvector type");
            }
          } else {
            throw new ParserException(operator + " takes one bitvector and one integer as input. ");
          }
        }
      case "bv2int":
        if (operands.size() != 1) {
          throw new ParserException(operator + " takes one bitvector operand as input.");
        } else {
          try {
            return Objects.requireNonNull(bimgr)
                .toIntegerFormula((BitvectorFormula) operands.get(0), false);
          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of bitvector type");
          }
        }
      case "int2bv":
      case "rotate_left":
      case "rotate_right":
      case "repeat":
        throw new ParserException(operator + " is not available in JavaSMT");

        //array operators
      case "select":
        if (operands.size() == 2) {
          return Objects.requireNonNull(amgr).select((ArrayFormula<Formula, Formula>) operands.get(0),
              operands.get(1));
        } else {
          throw new ParserException(operator + " takes one array and one index as input. ");
        }
      case "store":
        if (operands.size() == 3) {
          return Objects.requireNonNull(amgr)
              .store((ArrayFormula<Formula, Formula>) operands.get(0), operands.get(1), operands.get(2));
        } else {
          throw new ParserException(operator + " takes one array and one index as input. ");
        }
      case "const":
        if (isModel) {
          variables.put("temp", new ParserFormula("Array", Objects.requireNonNull(amgr).makeArray(
              "(as const (Array " + getArrayStrings(((FormulaType.ArrayFormulaType) sort).getIndexType()) + " " + getArrayStrings(((FormulaType.ArrayFormulaType) sort).getElementType()) +
                  ") " + operands.get(0) + ")", (FormulaType.ArrayFormulaType) sort)));
          return variables.get("temp").javaSmt;
        } else {
          throw  new ParserException("\"as const\" is not supported by JavaSMT");
        }
        //UF
      case "UF":
        try {
          return umgr.callUF((FunctionDeclaration<? extends Formula>) Objects.requireNonNull(
              ufOperator), operands);
        } catch (Exception e) {
          throw new ParserException(operator + " takes one array and one index as input. ");
        }

        //overloaded operators
      case "=":
        if (operands.size() == 2) {
          try {
            if (operands.stream().anyMatch(c -> c instanceof ArrayFormula)){
              return Objects.requireNonNull(amgr).equivalence((ArrayFormula<Formula, Formula>) operands.get(0),
                  (ArrayFormula<Formula, Formula>) operands.get(1));
            } else if (operands.stream().anyMatch(c -> c instanceof RationalFormula)) {
              //if (operands.stream().anyMatch(c -> variables.containsKey(c)))
                return Objects.requireNonNull(rmgr).equal((NumeralFormula) operands.get(0), (NumeralFormula) operands.get(1));
            } else if (operands.stream().anyMatch(c -> c instanceof IntegerFormula)) {
              return Objects.requireNonNull(imgr).equal((IntegerFormula) operands.get(0),
                  (IntegerFormula) operands.get(1));
            } else if (operands.stream().anyMatch(c -> c instanceof BooleanFormula)) {
              return bmgr.equivalence((BooleanFormula) operands.get(0),
                  (BooleanFormula) operands.get(1));
            } else if (operands.stream().anyMatch(c -> c instanceof BitvectorFormula)) {
              BooleanFormula result = Objects.requireNonNull(bimgr).equal((BitvectorFormula) operands.get(0),
                  (BitvectorFormula) operands.get(1));
              return result;
            }

          } catch (Exception e) {
            throw new ParserException("Operands for " + operator + "need to be of the same type" + operands);
          }
        } else {
          throw new ParserException(operator + " takes two equal types of operands as input. ");
        }
          default:
            throw new ParserException("Operator " + operator + " is not supported for operands type.");

    }
  }

  @Override public Object visitTerm_let(smtlibv2Parser.Term_letContext ctx) {
    for (int i = 0; i < ctx.var_binding().size(); i++) {
      visit(ctx.var_binding(i));
    }
    Formula formula = (Formula) visit(ctx.term());
    for (int j = 0; j < ctx.var_binding().size(); j++) {
      letVariables.remove(ctx.var_binding(j).symbol().getText());
    }
    return formula;
  }

  @Override public Object visitTerm_forall(smtlibv2Parser.Term_forallContext ctx) {
    throw new ParserException("Parser does not support Quantifiers");
  }

  @Override public Object visitTerm_exists(smtlibv2Parser.Term_existsContext ctx) {
    throw new ParserException("Parser does not support Quantifiers");
  }

  @Override public Object visitTerm_exclam(smtlibv2Parser.Term_exclamContext ctx) {
    throw new ParserException("Parser does not support Attributed Expressions");
  }

  @Override public Object visitSort_symbol_decl(smtlibv2Parser.Sort_symbol_declContext ctx) {
    throw new ParserException("Parser does not support Attributed Expressions");
  }

  @Override public Object visitTheory_sort(smtlibv2Parser.Theory_sortContext ctx) {
    throw new ParserException("Parser does not support Theory Attributes");
  }

  @Override public Object visitTheory_fun(smtlibv2Parser.Theory_funContext ctx) {
    throw new ParserException("Parser does not support Theory Attributes");
  }

  @Override public Object visitTheory_sort_descr(smtlibv2Parser.Theory_sort_descrContext ctx) {
    throw new ParserException("Parser does not support Theory Attributes");
  }

  @Override public Object visitTheory_fun_descr(smtlibv2Parser.Theory_fun_descrContext ctx) {
    throw new ParserException("Parser does not support Theory Attributes");
  }

  @Override public Object visitTheory_def(smtlibv2Parser.Theory_defContext ctx) {
    throw new ParserException("Parser does not support Theory Attributes");
  }

  @Override public Object visitTheory_val(smtlibv2Parser.Theory_valContext ctx) {
    throw new ParserException("Parser does not support Theory Attributes");
  }

  @Override public Object visitTheory_notes(smtlibv2Parser.Theory_notesContext ctx) {
    throw new ParserException("Parser does not support Theory Attributes");
  }

  @Override public Object visitTheory_attr(smtlibv2Parser.Theory_attrContext ctx) {
    throw new ParserException("Parser does not support Theory Attributes");
  }

  @Override public Object visitFunction_def(smtlibv2Parser.Function_defContext ctx) {
    String variable = ctx.symbol().getText();
    if (variable.startsWith("|")) {
      variable = variable.replaceAll("\\|", "PIPE");
    }
    List<FormulaType<?>> javaSorts;
    List<Formula> inputParams = new ArrayList<>();
    if (! ctx.sorted_var().isEmpty()) {
      for (int i = 0; i < ctx.sorted_var().size(); i++) {
        String name = ctx.sorted_var(i).symbol().getText();
        FormulaType<?> sort = (FormulaType<?>) visit(ctx.sort());
        Formula temp = mapKey(sort, name);
        variables.put("name", new ParserFormula("def-fun", temp));
        inputParams.add(temp);
      }
    }
    javaSorts = inputParams.stream().map(fmgr::getFormulaType).collect(Collectors.toList());

    String name;
    String sort;

    FormulaType<?> returnTypes = (FormulaType<?>) visit(ctx.sort());

    Formula key;

    if (! inputParams.isEmpty()) {
      ParserFormula temp = new ParserFormula("UF", umgr.declareUF(variable, returnTypes));
      temp.setReturnType(returnTypes);
      temp.setInputParams(javaSorts);
      variables.put(variable, temp);
      key = umgr.callUF((FunctionDeclaration<? extends Formula>) variables.get(variable).javaSmt, inputParams);
    } else {
      key = mapKey(returnTypes, variable);
    }

    Formula input = (Formula) visit(ctx.term());
    Formula value = input;
    variables.put(variable, new ParserFormula("def-fun", input));

    String keyString = key.toString();
    if (keyString.startsWith("PIPE")) {
      keyString = keyString.replaceAll("PIPE", "|");
    }
    String valueString = value.toString();

    Model.ValueAssignment assignment = new ValueAssignment(key, value, mapEquivalence(key, value)
        , keyString, valueString, new ArrayList<>());
    assignments.add(assignment);
    return visitChildren(ctx);
  }

  @Override public Object visitSorted_var(smtlibv2Parser.Sorted_varContext ctx) {
    return visitChildren(ctx);
  }

  @Override public Object visitCmd_assert(smtlibv2Parser.Cmd_assertContext ctx) {
    Object result = visitChildren(ctx);
    try {
      constraints.add((BooleanFormula) result);
      return result;
    } catch (Exception pE) {
      throw new ParserException("constraints need to be of Boolean type");
    }
  }

  @Override public Object visitCmd_declareConst(smtlibv2Parser.Cmd_declareConstContext ctx) {
    String variable = ctx.symbol().getText();
    FormulaType<?> sorts =  (FormulaType<?>) visit(ctx.sort());

    if (sorts.isBooleanType()) {
      variables.put(variable, new ParserFormula("Bool", bmgr.makeVariable(variable)));
    } else if (sorts.isIntegerType()) {
      variables.put((variable), new ParserFormula("Int", Objects.requireNonNull(imgr).makeVariable(variable)));
    } else if (sorts.isRationalType()) {
      variables.put((variable), new ParserFormula("Real", Objects.requireNonNull(rmgr).makeVariable(variable)));
    } else if (sorts.isBitvectorType()) {
      variables.put((variable), new ParserFormula("BitVec",
          (Objects.requireNonNull(bimgr).makeVariable(((FormulaType.BitvectorType) sorts).getSize(),
              variable))));
    } else if (sorts.isArrayType()) {
      variables.put((variable), new ParserFormula("BitVec",
          (Objects.requireNonNull(amgr).makeArray(variable,
              ((FormulaType.ArrayFormulaType<?,?>) sorts).getIndexType(),
              ((FormulaType.ArrayFormulaType<?,?>) sorts).getElementType()))));

    }
    return visitChildren(ctx);
  }


  public static String getArrayStrings(FormulaType<?> type) {

    if (type.isBooleanType()) {
      return "Bool";
    } else if (type.isIntegerType()) {
      return "Int";
    } else if (type.isRationalType()) {
      return "Real";
    } else if (type.isBitvectorType()) {
      return "(_ BitVec " + ((BitvectorType) type).getSize() + ")";
    } else if (type.isArrayType()) {
      return "(Array " + getArrayStrings(((FormulaType.ArrayFormulaType) type).getIndexType()) + " " +  getArrayStrings(((FormulaType.ArrayFormulaType) type).getElementType());
    } else {
      throw new ParserException(type + " is not a known Sort.");
    }
  }

  public Formula mapKey(FormulaType<?> sorts, String variable) {

    if (sorts.isBooleanType()) {
      return bmgr.makeVariable(variable);
    } else if (sorts.isIntegerType()) {
      return Objects.requireNonNull(imgr).makeVariable(variable);
    } else if (sorts.isRationalType()) {
      return Objects.requireNonNull(rmgr).makeVariable(variable);
    } else if (sorts.isBitvectorType()) {
      return Objects.requireNonNull(bimgr).makeVariable(((FormulaType.BitvectorType) sorts).getSize(),
              variable);
    } else if (sorts.isArrayType()) {
      return Objects.requireNonNull(amgr).makeArray(variable,
              ((FormulaType.ArrayFormulaType<?,?>) sorts).getIndexType(),
              ((FormulaType.ArrayFormulaType<?,?>) sorts).getElementType());
    } else {
      throw new ParserException(sorts + " is not of a known Sort.");
    }
  }

  public static FormulaType<?> mapSort(List<String> sorts) {
    String bvSize = "";
    String sort = "";
    String idx = "";
    String elem = "";
    if (sorts.get(0).startsWith("(_")) {
      bvSize = sorts.get(0).split("\\(_BitVec")[1];
      bvSize = bvSize.split("\\)")[0];
      sort = "BitVec";
    } else if (sorts.size() > 1) {

      if (sorts.get(0).startsWith("Array")) {
        idx = sorts.get(1);
        elem = sorts.get(2);
        sort = "Array";
      }
    } else {
      sort = sorts.get(0);
    }

    switch (sort) {
      case "Int":
        return FormulaType.IntegerType;
      case "Bool":
        return FormulaType.BooleanType;
      case "Real":
        return FormulaType.RationalType;
      case "BitVec":
        return FormulaType.BitvectorType.getBitvectorTypeWithSize(Integer.parseInt(bvSize));

      default:
        throw new ParserException("JavaSMT supports only Int, Real, BitVec and Bool for UF.");
    }
    }


  public BooleanFormula mapEquivalence(Formula key, Formula value) {
    if (key instanceof BooleanFormula) {
      return bmgr.equivalence((BooleanFormula) key, (BooleanFormula) value);
    } else if (key instanceof IntegerFormula) {
      return Objects.requireNonNull(imgr).equal((IntegerFormula) key, (IntegerFormula) value);
    } else if (key instanceof RationalFormula) {
      return Objects.requireNonNull(rmgr).equal((RationalFormula) key, (RationalFormula) value);
    } else if (key instanceof BitvectorFormula) {
      return Objects.requireNonNull(bimgr).equal((BitvectorFormula) key, (BitvectorFormula) value);
    } else if (key instanceof ArrayFormula) {
      return Objects.requireNonNull(amgr).equivalence((ArrayFormula<Formula, Formula>) key,
          (ArrayFormula<Formula, Formula>) value);
    } else {
      throw new ParserException(key + " is not a valid Sort");
    }
  }

  @Override public Object visitCmd_declareFun(smtlibv2Parser.Cmd_declareFunContext ctx) {
    String variable = ctx.symbol().getText();
    if (variable.startsWith("|")) {
      variable = variable.replaceAll("\\|", "PIPE");
    }
    List<String> declaration = new ArrayList<>();
    List<FormulaType<?>> javaSorts = new ArrayList<>();
    String returnType = ctx.getChild(ctx.getChildCount() - 1).getText();
    for (int i = 0; i < ctx.getChildCount(); i++) {
      declaration.add(ctx.getChild(i).getText());
    }

    List<String> inputParams;
    if (ctx.getChildCount() > 4 && ! ctx.getChild(3).getText().equals(")")) {
      inputParams = declaration.subList(3, ctx.getChildCount() - 2);
      javaSorts = inputParams.stream().map(e -> mapSort(Collections.singletonList(e))).collect(Collectors.toList());
    }

    ParserFormula temp = new ParserFormula("UF", umgr.declareUF(variable,
        mapSort(Collections.singletonList(returnType)),
        javaSorts));
    temp.setReturnType(mapSort(Collections.singletonList(returnType)));
    temp.setInputParams(javaSorts);
    variables.put(variable, temp);

    return visitChildren(ctx);
  }

  @Override public Object visitCmd_pop(smtlibv2Parser.Cmd_popContext ctx) {
    throw new ParserException("Parser does not support \"pop\"");
  }

  @Override public Object visitCmd_push(smtlibv2Parser.Cmd_pushContext ctx) {
    throw new ParserException("Parser does not support \"push\"");
  }

  @Override public Object visitDecl_sort(smtlibv2Parser.Decl_sortContext ctx) {
    throw new ParserException("JavaSMT does not support \"declare-sort\"");
  }

  @Override public Object visitError(smtlibv2Parser.ErrorContext ctx) {
    throw new ParserException(ctx.getText());
  }

  @Override public Object visitResp_get_model(smtlibv2Parser.Resp_get_modelContext ctx) {
    isModel = true;
    return visitChildren(ctx); }

}