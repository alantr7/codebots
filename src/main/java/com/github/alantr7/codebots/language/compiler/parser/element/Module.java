package com.github.alantr7.codebots.language.compiler.parser.element;

import com.github.alantr7.codebots.language.compiler.parser.element.stmt.Function;
import com.github.alantr7.codebots.language.compiler.parser.element.stmt.ImportStatement;
import com.github.alantr7.codebots.language.compiler.parser.element.stmt.RecordDefinition;
import lombok.Getter;

import java.util.Map;

@Getter
public class Module {

    private final ImportStatement[] imports;

    private final Function[] functions;

    private final Map<String, RecordDefinition> records;

    public Module(ImportStatement[] imports, Function[] functions, Map<String, RecordDefinition> records) {
        this.imports = imports;
        this.functions = functions;
        this.records = records;
    }

}
