package com.github.alantr7.codebots.language.compiler.parser.element.stmt;

import lombok.Getter;

public class RecordDefinition {

    @Getter
    private final String name;

    @Getter
    private final String[] fields;

    public RecordDefinition(String name, String[] fields) {
        this.name = name;
        this.fields = fields;
    }

}
