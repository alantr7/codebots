package com.github.alantr7.codebots.language.runtime;

import com.github.alantr7.codebots.api.bot.CodeBot;

public enum ValueType {

    INT, BOOLEAN, FLOAT, OBJECT, STRING, FUNCTION, CLASS, CODE_BLOCK, ANY, NULL;

    public boolean isCompatibleWith(ValueType type) {
        return type == this || this == ANY || type == NULL;
    }

    public static ValueType of(Object object) {
        if (object == null)
            return NULL;

        return switch (object.getClass().getName()) {
            case "java.lang.String" -> STRING;
            case "java.lang.Integer" -> INT;
            case "java.lang.Float" -> FLOAT;
            case "java.lang.Boolean" -> BOOLEAN;
            case "com.github.alantr7.codebots.language.runtime.RuntimeCodeBlock" -> CODE_BLOCK;
            default -> NULL;
        };
    }

    public static ValueType fromString(String name) {
        return switch (name) {
            case "int" -> INT;
            case "bool" -> BOOLEAN;
            case "block" -> CODE_BLOCK;
            case "float" -> FLOAT;
            case "object" -> OBJECT;
            case "string" -> STRING;
            case "function" -> FUNCTION;
            default -> null;
        };
    }

}
