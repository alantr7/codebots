package com.github.alantr7.codebots.language.runtime.modules.standard;

import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.RuntimeCodeBlock;
import com.github.alantr7.codebots.language.runtime.errors.exceptions.ExecutionException;
import com.github.alantr7.codebots.language.runtime.modules.MemoryModule;
import com.github.alantr7.codebots.language.runtime.modules.NativeModule;

import java.lang.reflect.Array;

public class LangModule extends NativeModule {

    public LangModule(Program program) {
        super(program);

        registerFunction("array", args -> {
            int capacity = args[0] == null ? 8 : (int) args[0];
            if (capacity > 20) {
                throw new ExecutionException("Arrays can not hold more than 20 elements!");
            }
            return new Object[capacity];
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
