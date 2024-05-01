package com.github.alantr7.codebots.language.compiler.parser.element.exp;

import lombok.Getter;

public class ArrayAccess extends VariableAccess {

    @Getter
    private final Expression index;

    public ArrayAccess(MemberAccess target, String name, Expression index) {
        super(target, name);
        this.index = index;
    }

    @Override
    public String getType() {
        return "array_access";
    }

    @Override
    public String toString() {
        return toString(0);
    }

    @Override
    protected String toString(int indent) {
        return super.toString(indent) + "[" + index + "]";
    }

}
