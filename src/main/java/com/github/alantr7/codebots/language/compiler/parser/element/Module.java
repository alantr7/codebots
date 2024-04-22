package com.github.alantr7.codebots.language.compiler.parser.element;

import com.github.alantr7.codebots.language.compiler.parser.element.stmt.Function;
import com.github.alantr7.codebots.language.compiler.parser.element.stmt.ImportStatement;
import lombok.Getter;

@Getter
public class Module {

    private final ImportStatement[] imports;

    private final Function[] functions;

    public Module(ImportStatement[] imports, Function[] functions) {
        this.imports = imports;
        this.functions = functions;
    }

}
