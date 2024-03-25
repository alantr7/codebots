package com.github.alantr7.codebots.language.runtime.functions;

import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.RuntimeCodeBlock;
import com.github.alantr7.codebots.language.runtime.RuntimeInstruction;

import java.util.function.Function;

public class RuntimeNativeFunction extends RuntimeCodeBlock {

    private boolean isExecuted = false;

    private final Function<Object[], Object> handler;

    public RuntimeNativeFunction(Program program, String label, Function<Object[], Object> handler) {
        super(program, label, program.getRootScope(), new RuntimeInstruction[0]);
        this.handler = handler;
    }

    @Override
    public boolean hasNext() {
        return !isExecuted;
    }

    @Override
    public void next() {
        var function = environment.getFunctionStack().getLast();
        handler.apply(function.getArguments());

        isExecuted = true;
    }

    @Override
    public void reset() {
        super.reset();
        isExecuted = false;
    }

}
