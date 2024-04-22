package com.github.alantr7.codebots.language.compiler.parser.element.stmt;

import lombok.Getter;

public class ImportStatement {

    @Getter
    private final String name;

    @Getter
    private final String alias;

    public ImportStatement(String name, String alias) {
        this.name = name;
        this.alias = alias;
    }

    @Override
    public String toString() {
        return "import " + name + " as " + alias;
    }

}
