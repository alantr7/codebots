package com.github.alantr7.codebots.language.runtime;

import com.github.alantr7.codebots.language.runtime.errors.Assertions;
import com.github.alantr7.codebots.language.runtime.errors.exceptions.ExecutionException;
import com.github.alantr7.codebots.language.runtime.functions.RuntimeFunctionContext;
import com.github.alantr7.codebots.language.runtime.functions.RuntimeSleepFunction;

import java.util.*;

public class RuntimeCodeBlock extends RuntimeObject {

    private final Program program;

    protected RuntimeEnvironment environment;

    private final RuntimeInstruction[] block;

    private final BlockScope scope;

    private int i;

    private boolean flagElse = false;

    private boolean flagElseSatisfied = false;

    private boolean isFunction = false;

    private String functionName;

    private final String label;

    private final Map<String, Integer> labelPositions = new LinkedHashMap<>();

    public RuntimeCodeBlock(Program program, String label, BlockScope scope, RuntimeInstruction[] block) {
        this.program = program;
        this.environment = program.getEnvironment();
        this.label = label;
        this.scope = scope;
        this.block = block;

        for (int j = 0; j < block.length; j++) {
            var instruction = block[j];
            if (instruction instanceof RuntimeCodeBlock block1 && block1.label != null)
                labelPositions.put(block1.label, j);
        }

        environment.REGISTRY_CURRENT_SCOPE.setValue(this);
    }

    public boolean hasNext() {
        return i < block.length;
    }

    public void next() {
        try {
            _execute();
        } catch (ExecutionException e) {
            var stackTrace = generateStackTrace();
            System.err.println("Error while executing \"" + block[i] + "\": " + e.getMessage());
            System.err.println("Stack trace:");
            for (var entry : stackTrace) {
                System.err.println("  at " + entry.toString());
            }
            environment.getBlockStack().clear();
        } catch (Exception e) {
            e.printStackTrace();
            environment.getBlockStack().clear();
        } finally {
        }
        i++;
    }

    public void reset() {
        i = 0;
    }

    private void _execute() throws Exception {
        var functionStack = environment.getFunctionStack();

        var sentence = block[i];
        if (sentence instanceof RuntimeCodeBlock block1) {
            block1.reset();
            environment.getBlockStack().add(block1);

            environment.REGISTRY_CURRENT_SCOPE.setValue(block1);
            return;
        }

        var tokens = ((RuntimeSentence) sentence).getTokens();
        var instruction = tokens[0];

        System.out.println(environment.getProgram().getMainModule() + ": " + Arrays.toString(tokens));

        switch (instruction) {
            case "import" -> {
                var relative = tokens[1];
                var module = program.getOrLoadModule(relative);
                Assertions.assertNotNull(module, "Module not found.");

                var holder = new RuntimeVariable(ValueType.CODE_BLOCK);
                holder.setValue(module.getBlock());
                setValue(tokens[2], holder);

                environment.getBlockStack().add(module.getBlock());
            }

            case "define_var" -> {
                var type = ValueType.fromString(tokens[2]);
                Assertions.assertBool(type != null && type != ValueType.NULL, "Invalid variable type.");
                scope.setVariable(tokens[1], new RuntimeVariable(type));
            }
            case "add" -> {

            }
            case "sub" -> {

            }
            case "mul" -> {

            }
            case "div" -> {

            }
            case "mod" -> {
                scope.getVariable(tokens[1])
                        .setValue((int) scope.getVariable(tokens[1]).getValue() % (int) getValue(tokens[2]));
            }
            case "inc" -> {
                System.out.println("I: " + getVariable("i"));
                scope.getVariable(tokens[1]).setValue(((int) scope.getVariable(tokens[1]).getValue()) + 1);
            }
            case "concat" -> {
                Assertions.assertType(environment.REGISTRY_CURRENT_VALUE, ValueType.STRING, "Type mismatch.");
                setValue(tokens[1], ((String) scope.getVariable(tokens[1]).getValue()) + environment.REGISTRY_CURRENT_VALUE.getValue());
            }
            case "sleep" ->
                    environment.getBlockStack().add(new RuntimeSleepFunction(program, Integer.parseInt(tokens[1])));
            case "push_func" -> {
                var object = (RuntimeCodeBlock) environment.REGISTRY_CURRENT_SCOPE.getValue();
                functionStack.add(new RuntimeFunctionContext(object.scope, tokens[1]));
            }
            case "use_arg" -> {
                var function = functionStack.getLast();
                environment.REGISTRY_CURRENT_VALUE.setValue(function.getArguments()[Integer.parseInt(tokens[1])]);
            }
            case "unload_arg" -> {
                var function = functionStack.getLast();
                setValue(tokens[2], function.getArguments()[Integer.parseInt(tokens[1])]);
            }
            case "set_arg" -> {
                functionStack.getLast().setArgument(Integer.parseInt(tokens[1]), getValue(tokens[2]));
            }
            case "pop_func" -> {
                functionStack.removeLast();
            }
            case "call" -> {
                var function = functionStack.getLast();
                var functionBlock = function.getScope().getFunction(function.getFunction());
                functionBlock.reset();

                environment.getBlockStack().add(functionBlock);
            }
            case "define_func" -> {
                var name = tokens[1];
                var functionBody = (RuntimeCodeBlock) block[i + 1];
                functionBody.isFunction = true;
                functionBody.functionName = name;

                scope.setFunction(name, functionBody);
                // Skip the next part
                i++;
            }
            case "return" -> {
                // So, here, it should remove all code blocks from stack until it reaches a function
                setValue("$rv", getValue(tokens[1]));

                var iterator = environment.getBlockStack().descendingIterator();
                while (iterator.hasNext()) {
                    var block = iterator.next();
                    iterator.remove();

                    if (block.isFunction) {
                        break;
                    }
                }
            }
            case "unload_rv" -> {
                setValue(tokens[1], environment.REGISTRY_RETURN_VALUE.getValue());
            }

            case "set" -> {
                setValue(tokens[1], getValue(tokens[2]));
            }
            case "reset" -> {
                // TODO: Use 'set $cv NULL'
                environment.REGISTRY_CURRENT_VALUE.setValue(null);
            }

            case "if" -> {
                if (testIfStatement(tokens)) {
                    System.out.println("TRUE!");
                    var nextBlock = ((RuntimeCodeBlock) block[++i]);
                    nextBlock.reset();

                    environment.getBlockStack().add(nextBlock);
                    return;
                } else {
                    System.out.println("NOT TRUE: " + getValue(tokens[1]) + " != " + getValue(tokens[3]));
                    i++; // Skip next!
                    flagElse = true;

                    return;
                }
            }

            case "else" -> {
                if (!flagElse) {
                    i++;
                    return;
                }

                flagElseSatisfied = true;
            }

            case "goto" -> {
                var label = tokens[1];
                // Now find block with that label....

                var iterator = environment.getBlockStack().descendingIterator();
                while (iterator.hasNext()) {
                    var block = iterator.next();
                    var position = block.labelPositions.get(label);

                    if (position != null) {
                        block.i = position;
                        ((RuntimeCodeBlock) block.block[position]).reset();
                        break;
                    } else {
                        iterator.remove();
                    }
                }

                return;
            }

            case "exit" -> {
                var label = tokens[1];
                // Now find block with that label....

                var iterator = environment.getBlockStack().descendingIterator();
                while (iterator.hasNext()) {
                    var block = iterator.next();
                    var position = block.labelPositions.get(label);

                    if (position != null) {
                        break;
                    } else {
                        iterator.remove();
                    }
                }
            }
        }

        if (flagElse && !flagElseSatisfied) {
            flagElse = false;
        }

        int a = 5;
        a++;
    }

    private Object getValue(String raw) {
        char firstCharacter = raw.charAt(0);
        return switch (firstCharacter) {
            case '$' -> {
                // Get value from the registry
                var name = raw.substring(1);
                yield environment.getRegistry(name).getValue(); // Added getValue()
            }
            case '&' -> scope.getVariable(raw.substring(1));
            case '*' -> scope.getVariable(raw.substring(1)).getValue();
            case '#' -> this;
            default -> switch (raw) {
                case "true" -> true;
                case "false" -> false;
                default -> {
                    if (raw.matches("\\d+"))
                        yield Integer.parseInt(raw);

                    yield raw;
                }
            };
        };
    }

    private void setValue(String key, Object value) throws ExecutionException {
        boolean isReference = value instanceof RuntimeVariable;
        RuntimeVariable registry;

        if (key.charAt(0) == '$') {
            // Get value from the registry
            var name = key.substring(1);
            registry = environment.getRegistry(name);
        } else {
            registry = scope.getVariable(key);
        }

        if (registry == environment.REGISTRY_CURRENT_SCOPE) {
            environment.REGISTRY_CURRENT_SCOPE.setValue(value);
            return;
        }

        Assertions.assertType(value, registry.getAcceptedType(), "Incompatible types (\"%s\" and \"%s\")".formatted(registry.getAcceptedType(), ValueType.of(value)));

        if (isReference) {
            registry.setPointer((RuntimeVariable) value);
        } else {
            registry.setValue(value);
        }
    }

    private boolean testIfStatement(String[] tokens) throws ExecutionException {
        var value1 = getValue(tokens[1]);
        var value2 = getValue(tokens[3]);

        return switch (tokens[2]) {
            case ">" -> {
                Assertions.assertType(value1, ValueType.INT, "Cannot use '>' operator on \"%s\"".formatted(ValueType.of(value1)));
                Assertions.assertType(value2, ValueType.INT, "Cannot use '>' operator on \"%s\"".formatted(ValueType.of(value2)));
                yield (int) value1 > (int) value2;
            }
            case "<" -> {
                Assertions.assertType(value1, ValueType.INT, "Cannot use '>' operator on \"%s\"".formatted(ValueType.of(value1)));
                Assertions.assertType(value2, ValueType.INT, "Cannot use '>' operator on \"%s\"".formatted(ValueType.of(value2)));
                yield (int) value1 < (int) value2;
            }
            case "=" -> Objects.equals(value1, value2);
            case "!=" -> !Objects.equals(value1, value2);
            case ">=" -> (int) value1 >= (int) value2;
            case "<=" -> (int) value1 <= (int) value2;
            default -> false;
        };
    }

    private String[] generateStackTrace() {
        var stack = program.getEnvironment().getBlockStack();
        var trace = new String[stack.size()];

        int i = 0;

        for (Iterator<RuntimeCodeBlock> it = stack.descendingIterator(); it.hasNext(); i++) {
            var entry = it.next();
            RuntimeInstruction lastInstruction;

            if (i == 0) {
                lastInstruction = entry.block[i];
            } else {
                lastInstruction = entry.block[entry.i - 1];
            }

            if (entry.isFunction) {
                trace[i] = entry.functionName + "(): " + lastInstruction.toString();
            } else {
                trace[i] = entry.toString() + ": " + lastInstruction.toString();
            }
        }

        return trace;
    }

    @Override
    public Object getVariable(String name) {
        return scope.getVariable(name);
    }

    @Override
    public RuntimeCodeBlock getFunction(String function) {
        return scope.getFunction(function);
    }

    @Override
    public String toString() {
        return label;
    }

}
