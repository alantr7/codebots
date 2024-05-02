package com.github.alantr7.codebots.language.compiler.parser.element.exp;

import lombok.Getter;

public class VariableAccess extends Expression {

    @Getter
    private final MemberAccess target;

    @Getter
    private final Expression[] indices;

    public VariableAccess(MemberAccess target, String name, Expression[] indices) {
        super(name);
        this.target = target;
        this.indices = indices;
    }

    public String getName() {
        return (String) getValue();
    }

    @Override
    public String getType() {
        return "member_access";
    }

}
