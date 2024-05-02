package com.github.alantr7.codebots.language.runtime.modules.standard;

import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.RuntimeCodeBlock;
import com.github.alantr7.codebots.language.runtime.errors.Assertions;
import com.github.alantr7.codebots.language.runtime.errors.exceptions.ExecutionException;
import com.github.alantr7.codebots.language.runtime.modules.MemoryModule;
import com.github.alantr7.codebots.language.runtime.modules.NativeModule;

import java.lang.reflect.Array;
import java.util.LinkedHashMap;

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
            else return 0;
        });
    }

}
