package com.github.alantr7.codebots.language.runtime.modules;

import com.github.alantr7.codebots.language.runtime.*;
import lombok.Getter;
import lombok.Setter;

public abstract class Module {

    protected final Program program;

    @Getter
    private final BlockScope rootScope;

    private final RuntimeEnvironment environment;

    @Getter
    protected final RuntimeCodeBlock block;

    public Module(Program program, BlockScope scope, RuntimeCodeBlock block) {
        this.program = program;
        this.environment = program.getEnvironment();
        this.block = block;
        this.rootScope = scope;
        this.rootScope.setParent(program.getRootScope());
    }

    public boolean hasNext() {
        return !environment.getBlockStack().isEmpty();
    }

    public void next() {
        var block = environment.getBlockStack().getLast();
        if (block.block().hasNext(block.context())) {
            block.block().next(block.context());
        } else {
            environment.getBlockStack().removeLast();
        }
    }

}
