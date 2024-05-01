package com.github.alantr7.codebots.language.compiler.parser.element.stmt;

import com.github.alantr7.codebots.language.compiler.parser.element.exp.Expression;

public class VariableDeclareStatement implements Statement {

    private final String name;

    private final Expression value;

    public VariableDeclareStatement(String name, Expression assignment) {
        this.name = name;
        this.value = assignment;
    }

    @Override
    public String toString() {
        return "var " + name + " = " + value;
    }

    public String getName() {
        return name;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public int getStatementType() {
        return Statement.VARIABLE_DECLARE;
    }

}
