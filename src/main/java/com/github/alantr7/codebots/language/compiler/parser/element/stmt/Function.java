package com.github.alantr7.codebots.language.compiler.parser.element.stmt;

import lombok.Getter;

import java.util.Arrays;

@Getter
public class Function {

    private final String name;

    private final String[] parameters;

    private final Statement[] statements;

    public Function(String name, String[] parameters, Statement[] statements) {
        this.name = name;
        this.parameters = parameters;
        this.statements = statements;
    }

    @Override
    public String toString() {
        return "function " + name + "(" + Arrays.toString(parameters) + ") {" + Arrays.toString(statements) + "}";
    }

}
