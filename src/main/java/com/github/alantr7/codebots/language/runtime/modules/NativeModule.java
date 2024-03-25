package com.github.alantr7.codebots.language.runtime.modules;

import com.github.alantr7.codebots.language.runtime.BlockScope;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.RuntimeCodeBlock;
import com.github.alantr7.codebots.language.runtime.RuntimeInstruction;

public class NativeModule extends Module {

    public NativeModule(Program program) {
        this(program, BlockScope.nestIn(program.getRootScope()));
    }

    private NativeModule(Program program, BlockScope scope) {
        super(program, scope, new RuntimeCodeBlock(program, "__main__", scope, new RuntimeInstruction[0]));
    }

}
