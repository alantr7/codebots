package com.github.alantr7.codebots.language.compiler.parser.element.stmt;

import com.github.alantr7.codebots.language.compiler.parser.element.exp.Expression;
import lombok.Getter;

@Getter
public class ForLoopStatement implements Statement {

    private final Statement statement1;

    private final Expression condition;

    private final Statement statement2;

    private final Statement[] body;

    public ForLoopStatement(Statement stmt1, Expression condition, Statement stmt2, Statement[] body) {
        this.statement1 = stmt1;
        this.condition = condition;
        this.statement2 = stmt2;
        this.body = body;
    }

}
