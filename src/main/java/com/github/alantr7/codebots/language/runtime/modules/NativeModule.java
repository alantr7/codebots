package com.github.alantr7.codebots.language.runtime.modules;

import com.github.alantr7.codebots.language.runtime.*;
import com.github.alantr7.codebots.language.runtime.functions.RuntimeNativeFunction;

public class NativeModule extends Module {

    public NativeModule(Program program) {
        this(program, BlockScope.nestIn(program.getRootScope()));
    }

    private NativeModule(Program program, BlockScope scope) {
        super(program, scope, new RuntimeCodeBlock(program, "__main__", BlockType.MAIN, new RuntimeInstruction[0]));
    }

    protected void registerFunction(String name, RuntimeNativeFunction.Handler handler) {
        var function = new RuntimeNativeFunction(this.program, name, handler);
        getRootScope().setFunction(name, function);
    }

}
