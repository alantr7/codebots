package com.github.alantr7.codebots.language.compiler.parser.element.stmt;

import com.github.alantr7.codebots.language.compiler.parser.element.exp.Expression;
import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

public class IfStatement implements Statement {

    @Getter
    private final Expression condition;

    @Getter
    private final Statement[] body;

    @Getter
    private final IfStatement elseIf;

    @Getter
    private final Statement[] elseBody;

    public IfStatement(Expression condition, Statement[] body, IfStatement elseIf, Statement[] elseBody) {
        this.condition = condition;
        this.body = body;
        this.elseIf = elseIf;
        this.elseBody = elseBody;
    }

    @Override
    public String toString() {
        return "if (" + condition + ") {\n" + String.join("\n", Arrays.stream(body).map(b -> b.toString()).collect(Collectors.toList())) + "\n" + "}";
    }
}
