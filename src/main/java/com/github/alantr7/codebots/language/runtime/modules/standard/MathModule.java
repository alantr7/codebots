package com.github.alantr7.codebots.language.runtime.modules.standard;

import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.functions.RuntimeNativeFunction;
import com.github.alantr7.codebots.language.runtime.modules.NativeModule;

import java.util.Arrays;
import java.util.Random;

public class MathModule extends NativeModule {

    public MathModule(Program program) {
        super(program);

        this.getRootScope().setFunction("random", new RuntimeNativeFunction(program, "random", args -> {
            var rand = Math.round(Math.random() * (int) args[0]);
//            System.out.println("Random bounds: " + args[0]);
            System.out.println("Arguments: " + Arrays.toString(args));
            System.out.println("Random number generated: " + rand);

            return rand;
        }));
    }

}
