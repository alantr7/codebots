package com.github.alantr7.codebots.language.runtime;

import com.github.alantr7.codebots.language.runtime.functions.RuntimeFunctionContext;
import com.github.alantr7.codebots.language.runtime.functions.RuntimeNativeFunction;
import lombok.Getter;

import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class RuntimeEnvironment {

    @Getter
    private final Program program;

    public RuntimeEnvironment(Program program) {
        this.program = program;
    }

    @Getter
    private final Deque<RuntimeFunctionContext> functionStack = new LinkedList<>();

    @Getter
    private final Deque<RuntimeCodeBlock> blockStack = new LinkedList<>();

    @Getter
    private final Map<String, RuntimeNativeFunction> nativeFunctions = new LinkedHashMap<>();

    public RuntimeVariable REGISTRY_RETURN_VALUE = new RuntimeVariable(ValueType.ANY);

    public RuntimeVariable REGISTRY_CURRENT_VALUE = new RuntimeVariable(ValueType.ANY);

    public RuntimeVariable REGISTRY_CURRENT_SCOPE = new RuntimeVariable(ValueType.ANY);

    public RuntimeVariable getRegistry(String name) {
        return switch (name) {
            case "cv" -> REGISTRY_CURRENT_VALUE;
            case "cs" -> REGISTRY_CURRENT_SCOPE;
            case "rv" -> REGISTRY_RETURN_VALUE;
            default -> null;
        };
    }

}
