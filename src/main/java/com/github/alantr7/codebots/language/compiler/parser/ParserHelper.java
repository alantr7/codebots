package com.github.alantr7.codebots.language.compiler.parser;

import com.github.alantr7.codebots.language.compiler.parser.error.ParserException;

public class ParserHelper {

    public static boolean isOperator(String input) {
        if (input.length() > 2 || input.isEmpty())
            return false;

        return input.equals("+") || input.equals("-") || input.equals("*") || input.equals("/") || input.equals("==") || input.equals("!=") ||
                input.equals(">") || input.equals("<") || input.equals(">=") || input.equals("<=") || input.equals("(") || input.equals(")");
    }

    public static boolean isNumber(String input) {
        return input.matches("\\d+");
    }

    public static boolean isBoolean(String input) {
        return input.equals("true") || input.equals("false");
    }

    public static int getPrecedence(String input) {
        return switch (input) {
            case "+", "-" -> 3;
            case "*", "/" -> 4;
            case "<", ">", "==", "!=", "<=", ">=" -> 2;
            case "(", ")", "#" -> 1;
            default -> 0;
        };
    }

    public static void expect(String token, String expected) throws ParserException {
        if (!token.equals(expected)) {
            throw new ParserException("Unexpected token: \"" + token + "\". Was expecting \"" + expected + "\".");
        }
    }

    public static void error(String message) throws ParserException {
        throw new ParserException(message);
    }

}
