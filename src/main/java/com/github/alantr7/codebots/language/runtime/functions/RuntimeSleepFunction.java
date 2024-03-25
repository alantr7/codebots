package com.github.alantr7.codebots.language.runtime.functions;

import com.github.alantr7.codebots.language.runtime.Program;

public class RuntimeSleepFunction extends RuntimeNativeFunction {

    private int ticks = 0;

    private final int duration;

    public RuntimeSleepFunction(Program program, int duration) {
        super(program, null, null);
        this.duration = duration;
    }

    @Override
    public boolean hasNext() {
        return ticks < duration;
    }

    @Override
    public void next() {
        ticks++;
    }

    @Override
    public void reset() {
        ticks = 0;
    }

}
