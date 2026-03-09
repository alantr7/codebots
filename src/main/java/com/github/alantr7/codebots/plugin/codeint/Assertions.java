package com.github.alantr7.codebots.plugin.codeint;

import com.github.alantr7.codebots.cbslang.exceptions.ExecutionException;

public class Assertions {

    public static void assertBool(boolean value, String error) throws ExecutionException {
        if (!value) throw new ExecutionException(error);
    }

}
