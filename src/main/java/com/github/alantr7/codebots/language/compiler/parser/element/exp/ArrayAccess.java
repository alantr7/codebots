package com.github.alantr7.codebots.language.compiler.parser.element.exp;

import lombok.Getter;

import java.util.Arrays;

public class ArrayAccess extends VariableAccess {

    @Getter
    private final Expression[] indices;

    public ArrayAccess(MemberAccess target, String name, Expression[] indices) {
        super(target, name);
        this.indices = indices;
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
        return super.toString(indent) + Arrays.toString(indices);
    }

}
