package com.github.alantr7.codebots.language.runtime.functions;

import com.github.alantr7.codebots.language.runtime.BlockContext;
import com.github.alantr7.codebots.language.runtime.Program;

public class SleepFunction extends RuntimeNativeFunction {

    public SleepFunction(Program program) {
        super(program, "sleep", null);
    }

    @Override
    public boolean hasNext(BlockContext context) {
        var call = environment.getCallStack().getLast();
        int duration = (int) call.getArguments()[0];

        return context.getLineIndex() < duration;
    }

    @Override
    public void next(BlockContext context) {
        context.advance();
        environment.setHalted(true);
    }

}
