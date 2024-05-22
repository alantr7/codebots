package com.github.alantr7.codebots.language.runtime;

import com.github.alantr7.codebots.language.runtime.functions.FunctionCall;
import com.github.alantr7.codebots.language.runtime.functions.RuntimeNativeFunction;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class RuntimeEnvironment {

    @Getter
    private final Program program;

    @Getter
    private boolean isInterrupted;

    @Getter @Setter
    private boolean isHalted;

    @Getter
    private String[] stackTrace;

    @Getter
    private Exception exception;

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

    public RuntimeVariable REGISTRY_RETURN_VALUE = new RuntimeVariable(DataType.ANY);

    public RuntimeVariable REGISTRY_CURRENT_VALUE = new RuntimeVariable(DataType.ANY);

    public RuntimeVariable REGISTRY_CURRENT_SCOPE = new RuntimeVariable(DataType.ANY);

    public RuntimeVariable REGISTRY_EXPRESSION_1 = new RuntimeVariable(DataType.ANY);

    public RuntimeVariable REGISTRY_EXPRESSION_2 = new RuntimeVariable(DataType.ANY);

    public RuntimeVariable REGISTRY_EXPRESSION_3 = new RuntimeVariable(DataType.ANY);

    public RuntimeVariable REGISTRY_LINE_NUMBER = new RuntimeVariable(DataType.INT);

    { REGISTRY_LINE_NUMBER.setValue(1); }

    public RuntimeVariable getRegistry(String name) {
        return switch (name) {
            case "cv" -> REGISTRY_CURRENT_VALUE;
            case "cs" -> REGISTRY_CURRENT_SCOPE;
            case "rv" -> REGISTRY_RETURN_VALUE;
            case "line" -> REGISTRY_LINE_NUMBER;
            case "exp1" -> REGISTRY_EXPRESSION_1;
            case "exp2" -> REGISTRY_EXPRESSION_2;
            case "exp3" -> REGISTRY_EXPRESSION_3;
            default -> null;
        };
    }

    public void interrupt() {
        interrupt(null);
    }

    public void interrupt(Exception e) {
        this.isInterrupted = true;
        this.stackTrace = generateStackTrace();
        this.exception = e;
    }

    public String[] generateStackTrace() {
        var stack = program.getEnvironment().getBlockStack();
        var trace = new String[stack.size()];

        int i = 0;

        for (Iterator<BlockStackEntry> it = stack.descendingIterator(); it.hasNext(); i++) {
            var entry = it.next();
            RuntimeInstruction lastInstruction;

            if (entry.block() instanceof RuntimeNativeFunction func) {
                trace[i] = func.getFunctionName() + "()";
                continue;
            }

            if (entry.block() == null) {
                trace[i] = "null block (?)";
                continue;
            }

            if (i == 0) {
                lastInstruction = entry.block().getBlock()[entry.context().getLineIndex()];
            } else {
                lastInstruction = entry.block().getBlock()[entry.context().getLineIndex() - 1];
            }

            if (entry.block().isFunction()) {
                trace[i] = entry.block().getFunctionName() + "(): " + lastInstruction.toString();
            } else {
                trace[i] = entry.block().toString() + ": " + lastInstruction.toString();
            }
        }

        return trace;
    }

}
