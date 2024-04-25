package com.github.alantr7.codebots.language.runtime.functions;

import com.github.alantr7.codebots.language.runtime.BlockContext;
import com.github.alantr7.codebots.language.runtime.Program;

public class RuntimeSleepFunction extends RuntimeNativeFunction {

    private final int duration;

    public RuntimeSleepFunction(Program program, int duration) {
        super(program, null, null);
        this.duration = duration;
    }

    @Override
    public boolean hasNext(BlockContext context) {
        return context.getLineIndex() < duration;
    }

    @Override
    public void next(BlockContext context) {
        context.advance();
    }

}
