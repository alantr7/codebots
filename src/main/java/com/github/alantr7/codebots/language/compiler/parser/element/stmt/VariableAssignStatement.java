package com.github.alantr7.codebots.language.compiler.parser.element.stmt;

import com.github.alantr7.codebots.language.compiler.parser.element.exp.Expression;
import com.github.alantr7.codebots.language.compiler.parser.element.exp.MemberAccess;
import lombok.Getter;

@Getter
public class VariableAssignStatement implements Statement {

    private final MemberAccess target;

    private final String name;

    private final Expression value;

    public VariableAssignStatement(MemberAccess target, String name, Expression value) {
        this.target = target;
        this.name = name;
        this.value = value;
    }

}
