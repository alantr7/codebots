package com.github.alantr7.codebots.language.runtime;

import com.github.alantr7.codebots.language.runtime.functions.RuntimeNativeFunction;
import com.github.alantr7.codebots.language.runtime.modules.Module;

public class DataType<T> {

    public static final DataType<Integer> INT = new DataType<>("INT");

    public static final DataType<Boolean> BOOLEAN = new DataType<>("BOOLEAN");

    public static final DataType<Float> FLOAT = new DataType<>("FLOAT");

    public static final DataType<String> STRING = new DataType<>("STRING");

    public static final DataType<Module> MODULE = new DataType<>("MODULE");

    public static final DataType<Dictionary> DICTIONARY = new DataType<>("DICTIONARY");

    public static final DataType<?> ANY = new DataType<>("ANY");

    public static final DataType<?> NULL = new DataType<>("NULL");

    private final String name;

    private DataType(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public boolean isCompatibleWith(DataType<?> type) {
        return type == this || this == ANY || type == NULL;
    }

    public static DataType<?> of(Object object) {
        if (object == null)
            return NULL;

        return switch (object.getClass().getName()) {
            case "java.lang.String" -> STRING;
            case "java.lang.Integer" -> INT;
            case "java.lang.Float" -> FLOAT;
            case "java.lang.Boolean" -> BOOLEAN;
            case "com.github.alantr7.codebots.language.runtime.Dictionary" -> DICTIONARY;
            default -> NULL;
        };
    }

    public static DataType<?> fromString(String name) {
        return switch (name) {
            case "int" -> INT;
            case "boolean" -> BOOLEAN;
            case "float" -> FLOAT;
            case "string" -> STRING;
            default -> null;
        };
    }

}
