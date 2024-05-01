package com.github.alantr7.codebots.language.compiler.parser.element.stmt;

import com.github.alantr7.codebots.language.compiler.parser.element.exp.Expression;
import lombok.Getter;

@Getter
public class DoWhileLoopStatement implements Statement {

    private final Expression expression;

    private final Statement[] body;

    public DoWhileLoopStatement(Expression expression, Statement[] body) {
        this.expression = expression;
        this.body = body;
    }

    @Override
    public int getStatementType() {
        return Statement.DO_WHILE_LOOP;
    }

}
