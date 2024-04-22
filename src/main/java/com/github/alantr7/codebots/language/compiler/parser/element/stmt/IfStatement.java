package com.github.alantr7.codebots.language.compiler.parser.element.stmt;

import com.github.alantr7.codebots.language.compiler.parser.element.exp.Expression;

import java.util.Arrays;
import java.util.stream.Collectors;

public class IfStatement implements Statement {

    private final Expression condition;

    private final Statement[] body;

    public IfStatement(Expression condition, Statement[] body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public String toString() {
        return "if (" + condition + ") {\n" + String.join("\n", Arrays.stream(body).map(b -> b.toString()).collect(Collectors.toList())) + "\n" + "}";
    }
}
