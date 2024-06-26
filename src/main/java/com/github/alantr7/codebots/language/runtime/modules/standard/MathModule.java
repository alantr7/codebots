package com.github.alantr7.codebots.language.runtime.modules.standard;

import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.functions.RuntimeNativeFunction;
import com.github.alantr7.codebots.language.runtime.modules.NativeModule;

public class MathModule extends NativeModule {

    public MathModule(Program program) {
        super(program);

        this.getRootScope().setFunction("random", new RuntimeNativeFunction(program, "random", args -> {
            return Math.round(Math.random() * (int) args[0]);
        }));
    }

}
