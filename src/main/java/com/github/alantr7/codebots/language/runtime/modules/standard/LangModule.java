package com.github.alantr7.codebots.language.runtime.modules.standard;

import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.RuntimeCodeBlock;
import com.github.alantr7.codebots.language.runtime.errors.Assertions;
import com.github.alantr7.codebots.language.runtime.errors.exceptions.ExecutionException;
import com.github.alantr7.codebots.language.runtime.functions.RuntimeNativeFunction;
import com.github.alantr7.codebots.language.runtime.functions.SleepFunction;
import com.github.alantr7.codebots.language.runtime.modules.MemoryModule;
import com.github.alantr7.codebots.language.runtime.modules.NativeModule;

import java.lang.reflect.Array;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class LangModule extends NativeModule {

    public LangModule(Program program) {
        super(program);

        registerFunction("array", args -> {
            int capacity = args.length == 0 ? 8 : (int) args[0];
            if (capacity > 20) {
                throw new ExecutionException("Arrays can not hold more than 20 elements!");
            }
            return new Object[capacity];
        });

        registerFunction("dict", args -> new LinkedHashMap<String, Object>());
        registerFunction("dict_set", args -> {
            Assertions.assertEquals(args.length, 3, "dict_set requires 3 arguments");
            Assertions.assertEquals(args[0] instanceof LinkedHashMap<?,?>, true, "dict_set requires a dictionary as the first argument");

            @SuppressWarnings("unchecked")
            var dict = (LinkedHashMap<String, Object>) args[0];
            dict.put((String) args[1], args[2]);

            return null;
        });
        registerFunction("dict_unset", args -> {
            Assertions.assertEquals(args.length, 2, "dict_unset requires 2 arguments");
            Assertions.assertEquals(args[0] instanceof LinkedHashMap<?,?>, true, "dict_unset requires a dictionary as the first argument");

            @SuppressWarnings("unchecked")
            var dict = (LinkedHashMap<String, Object>) args[0];
            dict.remove((String) args[1]);

            return null;
        });

        registerFunction("length", args -> {
            var object = args[0];
            if (object instanceof String text) {
                return text.length();
            }
            else if (object.getClass().isArray()) {
                return Array.getLength(object);
            }
            else if (object instanceof Map<?, ?> map) {
                return map.size();
            }
            else return 0;
        });

        registerFunction("to_string", args -> {
            Assertions.assertEquals(args.length, 1, "to_string requires 1 argument, but found " + args.length);
            Assertions.assertNotNull(args[0], "value must not be null");

            return stringify(args[0]);
        });

        registerFunction("to_int", args -> {
            Assertions.assertEquals(args.length, 1, "to_int requires 1 argument, but found " + args.length);
            Assertions.assertNotNull(args[0], "value must not be null");

            if (args[0] instanceof String text) {
                Assertions.assertEquals(text.matches("-?\\d+"), true, "provided string is not a valid number");
                return Integer.parseInt(text);
            }
            else if (args[0] instanceof Number number) {
                return number.intValue();
            }

            throw new ExecutionException("to_int only accepts string or number values");
        });

        getRootScope().setFunction("sleep", new SleepFunction(program));
    }

    private static final Set<String> classesWithDefaultToString = Set.of(
            "java.lang.String",
            "java.lang.Integer",
            "java.lang.Double",
            "java.lang.Float",
            "java.lang.Long",
            "java.lang.Short",
            "java.lang.Byte",
            "java.lang.Character",
            "java.lang.Boolean"
    );

    public static String stringify(Object object) {
        if (classesWithDefaultToString.contains(object.getClass().getName()) || object.getClass().isPrimitive())
            return object.toString();

        if (object.getClass().isArray()) {
            var builder = new StringBuilder();
            builder.append("[");
            for (int i = 0; i < Array.getLength(object); i++) {
                if (i > 0) builder.append(", ");
                builder.append(stringify(Array.get(object, i)));
            }
            builder.append("]");
            return builder.toString();
        }

        return "object";
    }

}
