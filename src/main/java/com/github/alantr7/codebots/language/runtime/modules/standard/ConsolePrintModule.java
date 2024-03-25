package com.github.alantr7.codebots.language.runtime.modules.standard;

import com.github.alantr7.codebots.language.runtime.BlockScope;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.functions.RuntimeNativeFunction;
import com.github.alantr7.codebots.language.runtime.modules.NativeModule;

public class ConsolePrintModule extends NativeModule {

    public ConsolePrintModule(Program program) {
        super(program);

        this.getRootScope().setFunction("print", new RuntimeNativeFunction(program, "print", args -> {
            System.out.println(args[0]);
            return null;
        }));
    }

}
