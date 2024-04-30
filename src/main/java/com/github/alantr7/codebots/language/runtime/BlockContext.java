package com.github.alantr7.codebots.language.runtime;

import lombok.Getter;
import lombok.Setter;

public class BlockContext {

    @Getter @Setter
    private int lineIndex = 0;

    private int flags = 0;

    @Getter
    private final BlockScope scope;

    public BlockContext(BlockScope scope) {
        this.scope = scope;
    }

    public int advance() {
        return lineIndex++;
    }

    public int advanceAndGet() {
        return ++lineIndex;
    }

    public boolean getFlag(int flag) {
        return (flags & flag) == flag;
    }

    public void setFlag(int flag, boolean value) {
        this.flags = value ? flags | flag : flags & ~flag;
    }

    public static final int FLAG_ELSE = 1;

    public static final int FLAG_ELSE_SATISFIED = 2;

}
