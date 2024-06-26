package com.github.alantr7.codebots.language.runtime.functions;

import com.github.alantr7.codebots.language.runtime.*;
import com.github.alantr7.codebots.language.runtime.errors.exceptions.ExecutionException;

public class RuntimeNativeFunction extends RuntimeCodeBlock {

    private final Handler handler;

    public RuntimeNativeFunction(Program program, String label, Handler handler) {
        super(program, label, BlockType.FUNCTION, new RuntimeInstruction[0]);
        this.handler = handler;
        this.setFunctionName(label);
    }

    @Override
    public boolean hasNext(BlockContext context) {
        return context.getLineIndex() == 0;
    }

    @Override
    public void next(BlockContext context) {
        var function = environment.getCallStack().getLast();
        try {
            var result = handler.execute(function.getArguments());
            environment.REGISTRY_RETURN_VALUE.setValue(result);
        } catch (Exception e) {
            environment.interrupt(e);
        }

        context.advance();
    }

    @FunctionalInterface
    public interface Handler {

        Object execute(Object[] arguments) throws ExecutionException;

    }

}
