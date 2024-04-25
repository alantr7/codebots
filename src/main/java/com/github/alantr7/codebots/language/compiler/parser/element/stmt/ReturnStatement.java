package com.github.alantr7.codebots.language.compiler.parser.element.stmt;

import com.github.alantr7.codebots.language.compiler.parser.element.exp.Expression;
import lombok.Getter;

public class ReturnStatement implements Statement {

    @Getter
    private final Expression value;

    public ReturnStatement(Expression value) {
        this.value = value;
    }

}
