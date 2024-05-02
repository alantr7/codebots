package com.github.alantr7.codebots.language.runtime.functions;

import com.github.alantr7.codebots.language.runtime.BlockScope;
import lombok.Getter;

public class FunctionCall {

    @Getter
    private BlockScope scope;

    @Getter
    private final String function;

    @Getter
    private final Object[] arguments;

    public FunctionCall(BlockScope scope, String function, int argumentsLength) {
        this.scope = scope;
        this.function = function;
        this.arguments = new Object[argumentsLength];
    }

    public void setArgument(int index, Object value) {
        arguments[index] = value;
    }

}
