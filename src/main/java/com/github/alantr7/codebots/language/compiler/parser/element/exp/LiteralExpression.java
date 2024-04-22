package com.github.alantr7.codebots.language.compiler.parser.element.exp;

public class LiteralExpression extends Expression {

    private final int type;

    public static final int BOOL = 0;

    public static final int STRING = 1;

    public static final int INT = 2;

    public static final int FLOAT = 3;

    public static final int OPERATOR = 10;

    public static final LiteralExpression OPERATOR_ADD = new LiteralExpression("+", OPERATOR);

    public static final LiteralExpression OPERATOR_SUB = new LiteralExpression("-", OPERATOR);

    public static final LiteralExpression OPERATOR_MUL = new LiteralExpression("*", OPERATOR);

    public static final LiteralExpression OPERATOR_DIV = new LiteralExpression("/", OPERATOR);

    public LiteralExpression(Object value, int type) {
        super(value);
        this.type = type;
    }

    @Override
    public String getType() {
        return "literal";
    }

    public int getLiteralType() {
        return type;
    }

    @Override
    protected String toString(int indent) {
        return " ".repeat(indent) + "literal: " + getValue();
    }

}
