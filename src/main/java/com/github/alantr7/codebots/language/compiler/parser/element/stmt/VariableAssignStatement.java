package com.github.alantr7.codebots.language.compiler.parser.element.stmt;

import com.github.alantr7.codebots.language.compiler.parser.element.exp.Expression;
import com.github.alantr7.codebots.language.compiler.parser.element.exp.VariableAccess;
import lombok.Getter;

@Getter
public class VariableAssignStatement implements Statement {

    private final VariableAccess target;

    private final Expression value;

    public VariableAssignStatement(VariableAccess target, Expression value) {
        this.target = target;
        this.value = value;
    }

    @Override
    public int getStatementType() {
        return Statement.VARIABLE_ASSIGN;
    }

}
