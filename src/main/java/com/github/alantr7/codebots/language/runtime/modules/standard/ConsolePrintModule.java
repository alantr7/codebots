package com.github.alantr7.codebots.language.runtime.modules.standard;

import com.github.alantr7.codebots.language.runtime.BlockScope;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.RuntimeCodeBlock;
import com.github.alantr7.codebots.language.runtime.errors.Assertions;
import com.github.alantr7.codebots.language.runtime.functions.RuntimeNativeFunction;
import com.github.alantr7.codebots.language.runtime.modules.NativeModule;

public class ConsolePrintModule extends NativeModule {

    public ConsolePrintModule(Program program) {
        super(program);

        this.getRootScope().setFunction("print", new RuntimeNativeFunction(program, "print", args -> {
            Assertions.assertEquals(args.length, 1, "print requires 1 argument");
            Assertions.assertEquals(args[0] instanceof String, true, "print requires a string as the first argument");

            System.out.println(args[0]);
            return null;
        }));
    }

}
