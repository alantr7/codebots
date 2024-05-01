package com.github.alantr7.codebots.language.runtime;

import com.github.alantr7.codebots.language.runtime.functions.FunctionCall;
import com.github.alantr7.codebots.language.runtime.functions.RuntimeNativeFunction;
import lombok.Getter;

import java.util.*;

public class RuntimeEnvironment {

    @Getter
    private final Program program;

    public RuntimeEnvironment(Program program) {
        this.program = program;
    }

    @Getter
    private final Deque<FunctionCall> callStack = new LinkedList<>();

    @Getter
    private final Deque<BlockStackEntry> blockStack = new LinkedList<>();

    @Getter
    private final Stack<LinkedList<Object>> tokenStack = new Stack<>();

    @Getter
    private final Map<String, RuntimeNativeFunction> nativeFunctions = new LinkedHashMap<>();

    public RuntimeVariable REGISTRY_RETURN_VALUE = new RuntimeVariable(ValueType.ANY);

    public RuntimeVariable REGISTRY_CURRENT_VALUE = new RuntimeVariable(ValueType.ANY);

    public RuntimeVariable REGISTRY_CURRENT_SCOPE = new RuntimeVariable(ValueType.ANY);

    public RuntimeVariable REGISTRY_EXPRESSION_1 = new RuntimeVariable(ValueType.ANY);

    public RuntimeVariable REGISTRY_EXPRESSION_2 = new RuntimeVariable(ValueType.ANY);

    public RuntimeVariable REGISTRY_EXPRESSION_3 = new RuntimeVariable(ValueType.ANY);

    public RuntimeVariable getRegistry(String name) {
        return switch (name) {
            case "cv" -> REGISTRY_CURRENT_VALUE;
            case "cs" -> REGISTRY_CURRENT_SCOPE;
            case "rv" -> REGISTRY_RETURN_VALUE;
            case "exp1" -> REGISTRY_EXPRESSION_1;
            case "exp2" -> REGISTRY_EXPRESSION_2;
            case "exp3" -> REGISTRY_EXPRESSION_3;
            default -> null;
        };
    }

}
