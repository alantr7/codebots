package com.github.alantr7.codebots.language.runtime.modules;

import com.github.alantr7.codebots.language.runtime.BlockScope;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.RuntimeCodeBlock;
import com.github.alantr7.codebots.language.runtime.RuntimeEnvironment;
import com.github.alantr7.codebots.language.runtime.modules.outline.OutlineEntry;
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
        if (block.hasNext()) {
            block.next();
        } else {
            environment.getBlockStack().removeLast();
        }
    }

}
