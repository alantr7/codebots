package com.github.alantr7.codebots.language.runtime.errors;

import com.github.alantr7.codebots.language.runtime.RuntimeVariable;
import com.github.alantr7.codebots.language.runtime.DataType;
import com.github.alantr7.codebots.language.runtime.errors.exceptions.ExecutionException;

import java.util.Objects;

public class Assertions {

    public static void assertType(RuntimeVariable var1, DataType<?> type, String message) throws ExecutionException {
        if (!var1.getType().isCompatibleWith(type))
            throw new ExecutionException(message);
    }

    public static void assertType(Object value, DataType<?> type, String message) throws ExecutionException {
        if (!type.isCompatibleWith(DataType.of(value)))
            throw new ExecutionException(message);
    }

    public static void assertEqualTypes(RuntimeVariable var1, RuntimeVariable var2, String message) throws ExecutionException {
        if (var1.getType() != var2.getType())
            throw new ExecutionException(message);
    }

    public static void assertNotNull(Object object, String message) throws ExecutionException {
        if (object == null)
            throw new ExecutionException(message);
    }

    public static void assertEquals(Object object1, Object object2, String message) throws ExecutionException {
        if (!Objects.equals(object1, object2))
            throw new ExecutionException(message);
    }

    public static void assertBool(boolean b, String message) throws ExecutionException {
        if (!b)
            throw new ExecutionException(message);
    }

    public static void expectArguments(Object[] args, Class<?>... types) throws ExecutionException {
        if (args.length < types.length)
            throw new ExecutionException("Argument and parameter count mismatch!");

        for (int i = 0; i < args.length; i++) {
            if (!types[i].isInstance(args[i]))
                throw new ExecutionException("Expected " + types[i].getName() + ", but got " + args[i].getClass().getName());
        }
    }

}
