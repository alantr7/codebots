package com.github.alantr7.codebots.language.runtime.modules.outline;

import com.github.alantr7.codebots.language.runtime.ValueType;
import lombok.Getter;

@Getter
public class OutlineEntry {

    public enum Type {
        BLOCK, VARIABLE
    }

    private final ValueType valueType;

    private final Type type;

    private final String name;

    private final OutlineEntry[] children;

    public OutlineEntry(Type type, String name, ValueType valueType, OutlineEntry[] children) {
        this.type = type;
        this.name = name;
        this.valueType = valueType;
        this.children = children;
    }

}
