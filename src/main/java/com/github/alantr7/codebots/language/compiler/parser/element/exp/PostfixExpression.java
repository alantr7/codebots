package com.github.alantr7.codebots.language.compiler.parser.element.exp;

import java.util.Arrays;

public class PostfixExpression extends Expression {

    public PostfixExpression(Expression[] value) {
        super(value);
    }

    public Expression[] getValue() {
        return (Expression[]) super.getValue();
    }

    @Override
    public String toString() {
        return toString(0);
    }

    @Override
    protected String toString(int indent) {
        return " ".repeat(indent) + Arrays.toString(getValue());
    }

}
