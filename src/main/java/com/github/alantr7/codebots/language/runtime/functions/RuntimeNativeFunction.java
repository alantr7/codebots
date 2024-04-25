package com.github.alantr7.codebots.language.runtime.functions;

import com.github.alantr7.codebots.language.runtime.*;

import java.util.function.Function;

public class RuntimeNativeFunction extends RuntimeCodeBlock {

    private final Function<Object[], Object> handler;

    public RuntimeNativeFunction(Program program, String label, Function<Object[], Object> handler) {
        super(program, label, program.getRootScope(), BlockType.FUNCTION, new RuntimeInstruction[0]);
        this.handler = handler;
    }

    @Override
    public boolean hasNext(BlockContext context) {
        return context.getLineIndex() == 0;
    }

    @Override
    public void next(BlockContext context) {
        var function = environment.getCallStack().getLast();
        var result = handler.apply(function.getArguments());

        environment.REGISTRY_RETURN_VALUE.setValue(result);
        context.advance();
    }

}
