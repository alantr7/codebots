package com.github.alantr7.codebots.language.runtime.utils;

import com.github.alantr7.codebots.language.runtime.errors.Assertions;

public class Calculator {

    public static final Operation<Integer, Integer, Integer> ADD = Integer::sum;

    public static final Operation<Integer, Integer, Integer> SUB = (a, b) -> a - b;

    public static final Operation<Integer, Integer, Integer> MUL = (a, b) -> a * b;

    public static final Operation<Integer, Integer, Integer> DIV = (a, b) -> {
        Assertions.assertBool(b != 0, "Can not divide by 0.");
        return a / b;
    };

    public static final Operation<Integer, Integer, Integer> MOD = (a, b) -> a % b;

    private static final Operation<Object, Object, Object>[] operations = new Operation[] {
            ADD, SUB, MUL, DIV, MOD
    };

    public static Operation<Object, Object, Object>[] operations() {
        return operations;
    }

    @FunctionalInterface
    public interface Operation<A, B, C> {

        C perform(A a, B b) throws Exception;

    }

}
