package org.vip.Parser;

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
}
