package com.github.alantr7.codebots.language.runtime;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

public class BlockContext {

    @Getter @Setter
    private int lineIndex = 0;

    private int flags = 0;

    @Getter
    private final BlockScope scope;

    private final Map<String, Object> extra = new LinkedHashMap<>();

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

    public Object getExtra(String key) {
        return extra.get(key);
    }

    public void setExtra(String key, Object value) {
        this.extra.put(key, value);
    }

    public static final int FLAG_ELSE               = 0b001;
    public static final int FLAG_ELSE_SATISFIED     = 0b010;

    public static final int FLAG_COMPLETED          = 0b100;

}
