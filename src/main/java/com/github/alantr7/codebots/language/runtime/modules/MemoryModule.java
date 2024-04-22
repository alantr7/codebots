package com.github.alantr7.codebots.language.runtime.modules;

import com.github.alantr7.codebots.language.runtime.BlockScope;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.RuntimeCodeBlock;

public class MemoryModule extends Module {

    public MemoryModule(Program program, RuntimeCodeBlock block) {
        super(program, new BlockScope(), block);
    }

}
