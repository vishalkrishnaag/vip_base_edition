package org.vip.Parser;

import org.vip.Codegen.Instruction;
import org.vip.Lexer.TokenType;

public class ConversionHelper {
    public ConversionHelper() {
    }

    public String convertOperator(TokenType type) {
        switch (type) {
            case PLUS -> {
                return "add";
            }
            case MINUS -> {

                return "sub";
            }
            case STAR -> {

                return "mul";
            }
            case SLASH -> {

                return "div";
            }
            case GREATER_THAN -> {

                return "gt";
            }
            case GREATER_THAN_EQ -> {

                return "gte";
            }
            case LESS_THAN -> {

                return "lt";
            }
            case LESS_THAN_EQ -> {

                return "lte";
            }
            case AND -> {

                return "and";
            }
            case OR -> {

                return "or";
            }
            case NOT -> {

                return "not";
            }
            case NOT_EQUAL -> {

                return "neq";
            }
            case EQUALS -> {

                return "cmp";
            }
            default -> {

                return "halt";
            }

        }
    }

    public Instruction convertToInstruction(String token) {
        switch (token) {
            case "+" -> {
                return Instruction.add;
            }
            case "-" -> {

                return Instruction.sub;
            }
            case "*" -> {

                return Instruction.mul;
            }
            case "/" -> {

                return Instruction.div;
            }
            case ">" -> {

                return Instruction.greater_than;
            }
            case ">=" -> {

                return Instruction.greater_than_or_equal;
            }
            case "<" -> {

                return Instruction.less_than;
            }
            case "<=" -> {

                return Instruction.less_than_or_equal;
            }
            case "and" -> {

                return Instruction.and;
            }
            case "or" -> {

                return Instruction.or;
            }
            case "not" -> {

                return Instruction.not;
            }
            case "not equal" -> {

                return Instruction.not_equal;
            }
            case "equal" -> {

                return Instruction.boolean_compare;
            }
            default -> {

                return Instruction.halt;
            }

        }
    }
}
