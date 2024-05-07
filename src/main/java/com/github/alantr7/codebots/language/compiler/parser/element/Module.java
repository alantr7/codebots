package com.github.alantr7.codebots.language.compiler.parser.element;

import com.github.alantr7.codebots.language.compiler.parser.element.stmt.Function;
import com.github.alantr7.codebots.language.compiler.parser.element.stmt.ImportStatement;
import com.github.alantr7.codebots.language.compiler.parser.element.stmt.RecordDefinition;
import com.github.alantr7.codebots.language.compiler.parser.element.stmt.VariableDeclareStatement;
import lombok.Getter;

import java.util.Map;

@Getter
public class Module {

    private final ImportStatement[] imports;

    private final Map<String, VariableDeclareStatement> variables;

    private final Function[] functions;

    private final Map<String, RecordDefinition> records;

    public Module(ImportStatement[] imports, Map<String, VariableDeclareStatement> variables, Function[] functions, Map<String, RecordDefinition> records) {
        this.imports = imports;
        this.variables = variables;
        this.functions = functions;
        this.records = records;
    }

}
