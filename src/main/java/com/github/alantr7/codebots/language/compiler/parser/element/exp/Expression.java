package com.github.alantr7.codebots.language.compiler.parser.element.exp;

public class Expression {

    private final Object value;

    public Expression(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    protected String toString(int indent) {
        return " ".repeat(indent) + "value: " + value;
    }

    public String getType() {
        return "expression";
    }

    public boolean isLiteral() {
        return getType().equals("literal");
    }

}
