package com.github.alantr7.codebots.language.compiler.parser.element.stmt;

import com.github.alantr7.codebots.language.compiler.parser.element.exp.Expression;
import lombok.Getter;

public class VariableDeclareStatement implements Statement {

    @Getter
    private final String name;

    @Getter
    private final Expression value;

    private final boolean isConstant;

    public VariableDeclareStatement(String name, Expression assignment, boolean isConstant) {
        this.name = name;
        this.value = assignment;
        this.isConstant = isConstant;
    }

    @Override
    public String toString() {
        return (isConstant ? "const " : "var ") + name + " = " + value;
    }

    public boolean isConstant() {
        return isConstant;
    }

    @Override
    public int getStatementType() {
        return Statement.VARIABLE_DECLARE;
    }

}
