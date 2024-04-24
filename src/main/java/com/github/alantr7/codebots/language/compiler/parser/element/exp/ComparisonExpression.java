package com.github.alantr7.codebots.language.compiler.parser.element.exp;

import lombok.Getter;

public class ComparisonExpression extends Expression {

    @Getter
    private final String operator;

    @Getter
    private final Expression right;

    public ComparisonExpression(Expression left, String operator, Expression right) {
        super(left);
        this.operator = operator;
        this.right = right;
    }

}
