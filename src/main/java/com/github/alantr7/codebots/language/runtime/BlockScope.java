package com.github.alantr7.codebots.language.runtime;

import java.util.LinkedHashMap;
import java.util.Map;

public class BlockScope {

    private BlockScope parent;

    private final Map<String, RuntimeVariable> variables = new LinkedHashMap<>();

    private final Map<String, RuntimeCodeBlock> functions = new LinkedHashMap<>();

    public RuntimeVariable getVariable(String name) {
        return variables.getOrDefault(name, parent != null ? parent.getVariable(name) : null);
    }

    public void setVariable(String name, RuntimeVariable variable) {
        variables.put(name, variable);
    }

    public RuntimeCodeBlock getFunction(String name) {
        return functions.getOrDefault(name, parent != null ? parent.getFunction(name) : null);
    }

    public void setFunction(String name, RuntimeCodeBlock block) {
        functions.put(name, block);
    }

    public void setParent(BlockScope parent) {
        this.parent = parent;
    }

    public static BlockScope nestIn(BlockScope parent) {
        var scope = new BlockScope();
        scope.setParent(parent);

        return scope;
    }

}
