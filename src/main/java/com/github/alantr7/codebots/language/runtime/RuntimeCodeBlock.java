package com.github.alantr7.codebots.language.runtime;

import com.github.alantr7.codebots.language.compiler.parser.ParserHelper;
import com.github.alantr7.codebots.language.runtime.errors.Assertions;
import com.github.alantr7.codebots.language.runtime.errors.exceptions.ExecutionException;
import com.github.alantr7.codebots.language.runtime.functions.FunctionCall;
import com.github.alantr7.codebots.language.runtime.functions.RuntimeSleepFunction;
import com.github.alantr7.codebots.language.runtime.modules.Module;
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
    private final BlockType blockType;

    private boolean isFunction = false;

    @Getter
    @Setter
    private String functionName;

    @Getter
    private final String label;

    private final Map<String, Integer> labelPositions = new LinkedHashMap<>();

    public RuntimeCodeBlock(Program program, String label, BlockType blockType, RuntimeInstruction[] block) {
        this.program = program;
        this.environment = program.getEnvironment();
        this.label = label;
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

        var scope = context.getScope();
        if (sentence instanceof RuntimeCodeBlock block1) {
            environment.getBlockStack().add(new BlockStackEntry(block1, new BlockContext(BlockScope.nestIn(scope))));
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

                var holder = new RuntimeVariable(ValueType.MODULE);
                holder.setValue(module);
                setValue(context, tokens[2], holder);

                environment.getBlockStack().add(new BlockStackEntry(module.getBlock(), new BlockContext(BlockScope.nestIn(program.getRootScope()))));
            }

            case "define_var" -> {
//                var type = ValueType.fromString(tokens[2]);
//                Assertions.assertBool(type != null && type != ValueType.NULL, "Invalid variable type.");
                scope.setVariable(tokens[1], new RuntimeVariable(ValueType.ANY));
            }
            case "add" -> {
                mathOperation(context, tokens, 0);
            }
            case "sub" -> {
                mathOperation(context, tokens, 1);
            }
            case "mul" -> {
                mathOperation(context, tokens, 2);
            }
            case "div" -> {
                mathOperation(context, tokens, 3);
            }
            case "mod" -> {
                scope.getVariable(tokens[1])
                        .setValue((int) scope.getVariable(tokens[1]).getValue() % (int) getValue(context, tokens[2]));
            }
            case "inc" -> {
                System.out.println("I: " + scope.getVariable("i"));
                scope.getVariable(tokens[1]).setValue(((int) scope.getVariable(tokens[1]).getValue()) + 1);
            }
            case "concat" -> {
                Assertions.assertType(environment.REGISTRY_CURRENT_VALUE, ValueType.STRING, "Type mismatch.");
                setValue(context, tokens[1], ((String) scope.getVariable(tokens[1]).getValue()) + environment.REGISTRY_CURRENT_VALUE.getValue());
            }
            case "sleep" -> environment.getBlockStack().add(new BlockStackEntry(
                    new RuntimeSleepFunction(program, Integer.parseInt(tokens[1])),
                    new BlockContext(BlockScope.nestIn(program.getRootScope())))
            );

            case "push_stack" -> environment.getTokenStack().push(new LinkedList<>());
            case "pop_stack" -> environment.getTokenStack().pop();
            case "push" -> {
                environment.getTokenStack().peek().push(String.valueOf(getValue(context, tokens[1])));
            }

            case "eval" -> {
                var expression = Arrays.copyOfRange(tokens, 2, tokens.length);
                evaluateExpression(context, tokens[1], expression);
            }

            case "push_func" -> {
                // TODO: Function calls
                var object = environment.REGISTRY_CURRENT_SCOPE.getValue();
                functionStack.add(new FunctionCall(((Module) object).getRootScope(), tokens[1]));
            }
            case "use_arg" -> {
                var function = functionStack.getLast();
                environment.REGISTRY_CURRENT_VALUE.setValue(function.getArguments()[Integer.parseInt(tokens[1])]);
            }
            case "unload_arg" -> {
                var function = functionStack.getLast();
                setValue(context, tokens[2], function.getArguments()[Integer.parseInt(tokens[1])]);
            }
            case "set_arg" -> {
                functionStack.getLast().setArgument(Integer.parseInt(tokens[1]), getValue(context, tokens[2]));
            }
            case "pop_func" -> {
                functionStack.removeLast();
                environment.REGISTRY_RETURN_VALUE.setValue(null);
            }
            case "call" -> {
                var function = functionStack.getLast();
                var functionBlock = function.getScope().getFunction(function.getFunction());

                environment.getBlockStack().add(new BlockStackEntry(functionBlock, new BlockContext(BlockScope.nestIn(function.getScope()))));
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
                setValue(context, "$rv", getValue(context, tokens[1]));

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
                setValue(context, tokens[1], environment.REGISTRY_RETURN_VALUE.getValue());
            }

            case "set" -> {
                setValue(context, tokens[1], getValue(context, tokens[2]));
            }
            case "reset" -> {
                // TODO: Use 'set $cv NULL'
                environment.REGISTRY_CURRENT_VALUE.setValue(null);
            }

            case "if" -> {
                if (testIfStatement(context, tokens)) {
                    var nextBlock = ((RuntimeCodeBlock) block[context.advanceAndGet()]);
                    environment.getBlockStack().add(new BlockStackEntry(nextBlock, new BlockContext(BlockScope.nestIn(scope))));
                    return;
                } else {
                    context.advance(); // Skip next!
                    context.setFlag(BlockContext.FLAG_ELSE, true);

                    return;
                }
            }

            case "else" -> {
                if (!context.getFlag(BlockContext.FLAG_ELSE)) {
                    context.advance();
                    return;
                }

                context.setFlag(BlockContext.FLAG_ELSE_SATISFIED, true);
            }

            case "goto" -> {
                var label = tokens[1];
                // Now find block with that label....

                var iterator = environment.getBlockStack().descendingIterator();
                while (iterator.hasNext()) {
                    var entry = iterator.next();
                    var position = entry.block().labelPositions.get(label);

                    if (position != null) {
                        var ctx = new BlockContext(entry.context().getScope());
                        ctx.setLineIndex(position);

                        iterator.remove();
                        environment.getBlockStack().add(new BlockStackEntry(entry.block(), ctx));
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

            case "exit_func" -> {
                var iterator = environment.getBlockStack().descendingIterator();
                while (iterator.hasNext()) {
                    var block = iterator.next();
                    iterator.remove();
                    if (block.block().isFunction) {
                        break;
                    }
                }
            }

            default -> {
                System.err.println("Unknown instruction: " + instruction);
                ;
            }
        }

        if (context.getFlag(BlockContext.FLAG_ELSE) && !context.getFlag(BlockContext.FLAG_ELSE_SATISFIED)) {
            context.setFlag(BlockContext.FLAG_ELSE, false);
        }
    }

    private void mathOperation(BlockContext context, String[] tokens, int operationIndex) throws Exception {
        var operation = Calculator.operations()[operationIndex];
        var num1 = getValue(context, tokens[2]);
        var num2 = getValue(context, tokens[3]);

        var result = operation.perform(num1, num2);
        setValue(context, tokens[1], result);
    }

    private void evaluateExpression(BlockContext context, String registry, String[] expressions) throws Exception {
        var stack = new Stack<>();
        var tokenStack = environment.getTokenStack().peek();

        Object operand2, operand1;

        for (var literal : expressions) {
            if (literal.matches("\\d+")) {
                stack.push(Integer.parseInt(literal));
            } else if (ParserHelper.isBoolean(literal)) {
                stack.push(Boolean.parseBoolean(literal));
            } else if (literal.equals("pop")) {
                var pop = tokenStack.removeLast();

                // TODO: Improve this
                if (ParserHelper.isNumber(pop)) {
                    stack.push(Integer.parseInt(pop));
                } else if (ParserHelper.isBoolean(pop)) {
                    stack.push(Boolean.parseBoolean(pop));
                } else if (pop.equals("null")) {
                    stack.push(null);
                } else {
                    stack.push(pop);
                }
            } else {
                // It's an operator
                operand2 = stack.pop();
                operand1 = stack.pop();

                if (operand1 instanceof String || operand2 instanceof String) {
                    switch (literal) {
                        case "+" -> {
                            stack.push(String.valueOf(operand1) + operand2);
                        }
                        case "==" -> stack.push(Objects.equals(operand1, operand2));
                        case "!=" -> stack.push(!Objects.equals(operand1, operand2));
                    }
                } else {
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
        }

        setValue(context, registry, stack.peek());
//        System.out.println("Evaluated expression. Result: " + stack.peek());
    }

    private Object getValue(BlockContext context, String raw) {
        var scope = context.getScope();
        char firstCharacter = raw.charAt(0);
        return switch (firstCharacter) {
            case '$' -> {
                // Get value from the registry
                var name = raw.substring(1);
                yield environment.getRegistry(name).getValue(); // Added getValue()
            }
            case '&' -> scope.getVariable(raw.substring(1));
            case '*' -> scope.getVariable(raw.substring(1)).getValue();
            case '#' -> scope.getModule();
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

    protected void setValue(BlockContext context, String key, Object value) throws ExecutionException {
        var scope = context.getScope();
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

    private boolean testIfStatement(BlockContext context, String[] tokens) throws ExecutionException {
        var value1 = getValue(context, tokens[1]);
        var value2 = getValue(context, tokens[2]);

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
    public String toString() {
        return "BLOCK " + (label != null ? label : functionName);
    }

}
