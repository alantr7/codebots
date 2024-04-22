package com.github.alantr7.codebots.language.compiler.parser;

public class ParserHelper {

    public static boolean isOperator(String input) {
        if (input.length() != 1)
            return false;

        return input.equals("+") || input.equals("-") || input.equals("*") || input.equals("/");
    }

    public static boolean isNumber(String input) {
        return input.matches("\\d+");
    }

    public static int getPrecedence(String input) {
        return switch (input.charAt(0)) {
            case '+', '-' -> 2;
            case '*', '/' -> 3;
            case '(', ')', '#' -> 1;
            default -> 0;
        };
    }

}
