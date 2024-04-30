package com.github.alantr7.codebots.language.runtime.modules;

import com.github.alantr7.codebots.language.runtime.*;

public class NativeModule extends Module {

    public NativeModule(Program program) {
        this(program, BlockScope.nestIn(program.getRootScope()));
    }

    private NativeModule(Program program, BlockScope scope) {
        super(program, scope, new RuntimeCodeBlock(program, "__main__", BlockType.MAIN, new RuntimeInstruction[0]));
    }

}
