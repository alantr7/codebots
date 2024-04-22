package com.github.alantr7.codebots.language.compiler.parser.element.exp;

public class MemberAccess extends Expression {

    private final MemberAccess right;

    public MemberAccess(Object left, MemberAccess right) {
        super(left);
        this.right = right;
    }

    public MemberAccess getRight() {
        return right;
    }

    @Override
    public String toString() {
        return this.getValue() + (right != null ? "." + right : "");
    }

}
