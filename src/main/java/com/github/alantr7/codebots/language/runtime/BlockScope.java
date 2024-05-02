package com.github.alantr7.codebots.language.runtime;

import com.github.alantr7.codebots.language.runtime.modules.Module;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

public class BlockScope {

    @Getter
    private BlockScope parent;

    private Module module;

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

    public RuntimeCodeBlock[] getFunctions() {
        return functions.values().toArray(RuntimeCodeBlock[]::new);
    }

    public void setFunction(String name, RuntimeCodeBlock block) {
        functions.put(name, block);
    }

    public void setParent(BlockScope parent) {
        this.parent = parent;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public Module getModule() {
        return module != null ? module : (parent != null ? parent.getModule() : null);
    }

    public static BlockScope nestIn(BlockScope parent) {
        var scope = new BlockScope();
        scope.setParent(parent);

        return scope;
    }

}
