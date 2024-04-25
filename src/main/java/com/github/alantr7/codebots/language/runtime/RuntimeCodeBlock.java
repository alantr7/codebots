package com.github.alantr7.codebots.language.runtime;

import com.github.alantr7.codebots.language.compiler.parser.ParserHelper;
import com.github.alantr7.codebots.language.runtime.errors.Assertions;
import com.github.alantr7.codebots.language.runtime.errors.exceptions.ExecutionException;
import com.github.alantr7.codebots.language.runtime.functions.FunctionCall;
import com.github.alantr7.codebots.language.runtime.functions.RuntimeSleepFunction;
import com.github.alantr7.codebots.language.runtime.utils.Calculator;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class RuntimeCodeBlock extends RuntimeObject {

    private final Program program;

    protected RuntimeEnvironment environment;

    @Getter
    private final RuntimeInstruction[] block;

    @Getter
    private final BlockScope scope;

    @Getter
    private final BlockType blockType;

    private boolean isFunction = false;

    @Getter @Setter
    private String functionName;

    @Getter
    private final String label;

    private final Map<String, Integer> labelPositions = new LinkedHashMap<>();

    public RuntimeCodeBlock(Program program, String label, BlockScope scope, BlockType blockType, RuntimeInstruction[] block) {
        this.program = program;
        this.environment = program.getEnvironment();
        this.label = label;
        this.scope = scope;
        this.blockType = blockType;
        this.block = block;

        for (int j = 0; j < block.length; j++) {
            var instruction = block[j];
            if (instruction instanceof RuntimeCodeBlock block1 && block1.label != null)
                labelPositions.put(block1.label, j);
        }

        environment.REGISTRY_CURRENT_SCOPE.setValue(this);
    }

    public boolean hasNext(BlockContext context) {
        return context.getLineIndex() < block.length;
    }

    public void next(BlockContext context) {
        int i = context.getLineIndex();
        try {
            _execute(context);
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
        context.advance();
    }

    private void _execute(BlockContext context) throws Exception {
        var functionStack = environment.getCallStack();

        final int i = context.getLineIndex();
        var sentence = block[i];
        if (sentence instanceof RuntimeCodeBlock block1) {
            environment.getBlockStack().add(new BlockStackEntry(block1, new BlockContext()));
            environment.REGISTRY_CURRENT_SCOPE.setValue(block1);
            return;
        }

        var tokens = ((RuntimeSentence) sentence).getTokens();
        var instruction = tokens[0];

        switch (instruction) {
            case "import" -> {
                var relative = tokens[1];
                var module = program.getOrLoadModule(relative);
                Assertions.assertNotNull(module, "Module not found.");

                var holder = new RuntimeVariable(ValueType.CODE_BLOCK);
                holder.setValue(module.getBlock());
                setValue(tokens[2], holder);

                environment.getBlockStack().add(new BlockStackEntry(module.getBlock(), new BlockContext()));
            }

            case "define_var" -> {
//                var type = ValueType.fromString(tokens[2]);
//                Assertions.assertBool(type != null && type != ValueType.NULL, "Invalid variable type.");
                scope.setVariable(tokens[1], new RuntimeVariable(ValueType.ANY));
            }
            case "add" -> {
                mathOperation(tokens, 0);
            }
            case "sub" -> {
                mathOperation(tokens, 1);
            }
            case "mul" -> {
                mathOperation(tokens, 2);
            }
            case "div" -> {
                mathOperation(tokens, 3);
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
            case "sleep" -> environment.getBlockStack().add(new BlockStackEntry(
                    new RuntimeSleepFunction(program, Integer.parseInt(tokens[1])),
                    new BlockContext())
            );

            case "push_stack" -> environment.getTokenStack().push(new Stack<>());
            case "pop_stack" -> environment.getTokenStack().pop();
            case "push" -> {
                System.out.println("Pushing: " + String.valueOf(getValue(tokens[1])));
                environment.getTokenStack().peek().push(String.valueOf(getValue(tokens[1])));
            }

            case "eval" -> {
                var expression = Arrays.copyOfRange(tokens, 2, tokens.length);
                evaluateExpression(tokens[1], expression);
            }

            case "push_func" -> {
                var object = (RuntimeCodeBlock) environment.REGISTRY_CURRENT_SCOPE.getValue();
                functionStack.add(new FunctionCall(object.scope, tokens[1]));
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
                System.out.println("Calling function : " + function.getFunction());

                environment.getBlockStack().add(new BlockStackEntry(functionBlock, new BlockContext()));
            }
            case "define_func" -> {
                var name = tokens[1];
                var functionBody = (RuntimeCodeBlock) block[i + 1];
                functionBody.isFunction = true;
                functionBody.functionName = name;

                scope.setFunction(name, functionBody);
                // Skip the next part

                context.advance();
            }
            case "return" -> {
                // So, here, it should remove all code blocks from stack until it reaches a function
                setValue("$rv", getValue(tokens[1]));

                var iterator = environment.getBlockStack().descendingIterator();
                while (iterator.hasNext()) {
                    var block = iterator.next();
                    iterator.remove();

                    if (block.block().isFunction) {
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
                    var nextBlock = ((RuntimeCodeBlock) block[context.advanceAndGet()]);

                    environment.getBlockStack().add(new BlockStackEntry(nextBlock, new BlockContext()));
                    return;
                } else {
                    System.out.println("NOT TRUE: " + getValue(tokens[1]) + " != " + getValue(tokens[2]));
                    context.advance(); // Skip next!
                    context.setFlag(BlockContext.FLAG_ELSE, true);

                    return;
                }
            }

            case "else" -> {
                if (!context.getFlag(BlockContext.FLAG_ELSE)) {
                    System.out.println("SKIPPING ELSE: " + this.block[context.getLineIndex() + 1]);
                    context.advance();
                    return;
                } else {
                    System.out.println("ELSE FLAG IS TRUE!");
                }

                context.setFlag(BlockContext.FLAG_ELSE_SATISFIED, true);
            }

            case "goto" -> {
                var label = tokens[1];
                // Now find block with that label....

                var iterator = environment.getBlockStack().descendingIterator();
                while (iterator.hasNext()) {
                    var block = iterator.next().block();
                    var position = block.labelPositions.get(label);

                    if (position != null) {
                        /*
                        block.i = position;
                        ((RuntimeCodeBlock) block.block[position]).reset();
                         */
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
                    var position = block.block().labelPositions.get(label);

                    if (position != null) {
                        break;
                    } else {
                        iterator.remove();
                    }
                }
            }

            default -> {
                System.err.println("Unknown instruction: " + instruction);;
            }
        }

        if (context.getFlag(BlockContext.FLAG_ELSE) && !context.getFlag(BlockContext.FLAG_ELSE_SATISFIED)) {
            context.setFlag(BlockContext.FLAG_ELSE, false);
        }
    }

    private void mathOperation(String[] tokens, int operationIndex) throws Exception {
        var operation = Calculator.operations()[operationIndex];
        var num1 = getValue(tokens[2]);
        var num2 = getValue(tokens[3]);

        var result = operation.perform(num1, num2);
        setValue(tokens[1], result);
    }

    private void evaluateExpression(String registry, String[] expressions) throws Exception {
        System.out.println("Evaluating expression: " + Arrays.toString(expressions));
        var stack = new Stack<>();
        var tokenStack = environment.getTokenStack().peek();

        Object operand1, operand2;

        for (var literal : expressions) {
            if (literal.matches("\\d+")) {
                stack.push(Integer.parseInt(literal));
            } else if (ParserHelper.isBoolean(literal)) {
                stack.push(Boolean.parseBoolean(literal));
            } else if (literal.equals("pop")) {
                var pop = tokenStack.pop();
                System.out.println("Popped: " + pop);

                // TODO: Improve this
                if (pop.matches("\\d+")) {
                    stack.push(Integer.parseInt(pop));
                } else {
                    stack.push(Boolean.parseBoolean(pop));
                }
            } else {
                // It's an operator
                operand2 = stack.pop();
                operand1 = stack.pop();

                switch (literal) {
                    case "+" -> stack.push((int) operand1 + (int) operand2);
                    case "-" -> stack.push((int) operand1 - (int) operand2);
                    case "*" -> stack.push((int) operand1 * (int) operand2);
                    case "/" -> stack.push((int) operand1 / (int) operand2);
                    case "==" -> stack.push(operand1 == operand2);
                    case "!=" -> stack.push(operand1 != operand2);
                    case "<" -> stack.push((int) operand1 < (int) operand2);
                    case ">" -> stack.push((int) operand1 > (int) operand2);
                    case "<=" -> stack.push((int) operand1 <= (int) operand2);
                    case ">=" -> stack.push((int) operand1 >= (int) operand2);
                }
            }
        }

        setValue(registry, stack.peek());
        System.out.println("Evaluated expression. Result: " + stack.peek());
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

    protected RuntimeVariable findVariableInScope(String raw) {
        char firstCharacter = raw.charAt(0);
        return switch (firstCharacter) {
            case '$' -> {
                // Get value from the registry
                var name = raw.substring(1);
                yield environment.getRegistry(name); // Added getValue()
            }
            case '&' -> scope.getVariable(raw.substring(1));
            default -> scope.getVariable(raw);
        };
    }

    protected void setValue(String key, Object value) throws ExecutionException {
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
        var value2 = getValue(tokens[2]);

        return Objects.equals(value1, value2);
    }

    private String[] generateStackTrace() {
        var stack = program.getEnvironment().getBlockStack();
        var trace = new String[stack.size()];

        int i = 0;

        for (Iterator<BlockStackEntry> it = stack.descendingIterator(); it.hasNext(); i++) {
            var entry = it.next();
            RuntimeInstruction lastInstruction;

            if (i == 0) {
                lastInstruction = entry.block().block[entry.context().getLineIndex()];
            } else {
                lastInstruction = entry.block().block[entry.context().getLineIndex() - 1];
            }

            if (entry.block().isFunction) {
                trace[i] = entry.block().functionName + "(): " + lastInstruction.toString();
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
        return "BLOCK " + (label != null ? label : functionName);
    }

}
