package com.github.alantr7.codebots.language.runtime.functions;

import com.github.alantr7.codebots.language.runtime.BlockScope;
import lombok.Getter;

public class RuntimeFunctionContext {

    @Getter
    private BlockScope scope;

    @Getter
    private final String function;

    @Getter
    private final Object[] arguments = new Object[5];

    public RuntimeFunctionContext(BlockScope scope, String function) {
        this.scope = scope;
        this.function = function;
    }

    public void setArgument(int index, Object value) {
        arguments[index] = value;
    }

}
