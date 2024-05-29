package com.github.alantr7.codebots.language.runtime;

import com.github.alantr7.codebots.language.compiler.parser.ParserHelper;
import com.github.alantr7.codebots.language.runtime.errors.Assertions;
import com.github.alantr7.codebots.language.runtime.errors.exceptions.ExecutionException;
import com.github.alantr7.codebots.language.runtime.functions.FunctionCall;
import com.github.alantr7.codebots.language.runtime.modules.Module;
import com.github.alantr7.codebots.language.runtime.modules.standard.LangModule;
import com.github.alantr7.codebots.language.runtime.utils.Calculator;
import com.github.alantr7.codebots.plugin.config.Config;
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

    @Getter
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

//        environment.REGISTRY_CURRENT_SCOPE.setValue(this);
    }

    public boolean hasNext(BlockContext context) {
        return context.getLineIndex() < block.length;
    }

    public void next(BlockContext context) {
        int i = context.getLineIndex();
        try {
            _execute(context);
        } catch (ExecutionException e) {
            environment.interrupt(e);
        } catch (Exception e) {
            e.printStackTrace();
            environment.interrupt(e);
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
            return;
        }

        var tokens = ((RuntimeSentence) sentence).getTokens();
        var instruction = tokens[0];

        switch (instruction) {
            case "import" -> {
                var relative = tokens[1];
                var module = program.getOrLoadModule(relative);
                Assertions.assertNotNull(module, "Module not found.");

                var holder = new RuntimeVariable(DataType.MODULE);
                holder.setValue(module);
                setValue(context, tokens[2], holder);

                environment.getBlockStack().add(new BlockStackEntry(module.getBlock(), new BlockContext(BlockScope.nestIn(program.getRootScope()))));
            }

            case "define_var" -> {
                int variables = scope.getVariableCountRecursive();
                if (variables == Config.SCRIPTS_MAX_VARIABLES_COUNT) {
                    throw new ExecutionException("Cannot declare any more variables due to the variables count limit!");
                }

                scope.setVariable(tokens[1], new RuntimeVariable(DataType.ANY));
            }
            case "define_const" -> {
                var variable = new RuntimeVariable(DataType.ANY);
                variable.setConstant(true);

                scope.setVariable(tokens[1], variable);
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
                Assertions.assertType(environment.REGISTRY_CURRENT_VALUE, DataType.STRING, "Type mismatch.");
                setValue(context, tokens[1], ((String) scope.getVariable(tokens[1]).getValue()) + environment.REGISTRY_CURRENT_VALUE.getValue());
            }

            case "push_stack" -> environment.getTokenStack().push(new LinkedList<>());
            case "pop_stack" -> environment.getTokenStack().pop();
            case "push" -> {
                var toPush = getValue(context, tokens[1]);
                Object value;
                if (toPush instanceof Long l) {
                    value = (int) (long) l;
                } else {
                    value = toPush;
                }

                environment.getTokenStack().peek().push(value);

            }

            case "eval" -> {
                var expression = Arrays.copyOfRange(tokens, 2, tokens.length);
                evaluateExpression(context, tokens[1], expression);
            }

            case "halt" -> environment.setHalted(true);

            case "push_func" -> {
                if (functionStack.size() == Config.SCRIPTS_MAX_FUNCTION_CALL_STACK_SIZE)
                    throw new ExecutionException("Call stack overflow!");

                var object = environment.REGISTRY_CURRENT_SCOPE.getValue();
                functionStack.add(new FunctionCall(((Module) object).getRootScope(), tokens[1], Integer.parseInt(tokens[2])));
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

                if (functionBlock == null)
                    throw new ExecutionException("Function '" + function.getFunction() + "' not found.");

                environment.getBlockStack().add(new BlockStackEntry(functionBlock, new BlockContext(BlockScope.nestIn(function.getScope()))));
            }
            case "define_func" -> {
                /*
                int functions = scope.getFunctionsCountRecursive();
                if (functions == Config.SCRIPTS_MAX_FUNCTIONS_COUNT) {
                    System.err.println(functions + " / " + Config.SCRIPTS_MAX_FUNCTIONS_COUNT);
                    throw new ExecutionException("Cannot define any more functions due to the functions count limit!");
                }*/

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

            // Takes a value from an array, and stores it into a variable/registry
            // Or a dictionary!
            case "array_get" -> {
                var array = getValue(context, tokens[1]);
                var key = getValue(context, tokens[2]);

                System.out.println("Finding array: " + tokens[1]);
                System.out.println("Index: " + key);

                var element = array instanceof String text
                        ? String.valueOf(text.charAt((int) key))
                        : key instanceof String dictKey ? ((Map<?, ?>) array).get(dictKey) : ((Object[]) array)[(int) key];

                setValue(context, tokens[3], element);
            }

            // Takes a value from a variable/registry, and stores it into an array
            case "array_set" -> {
                var array = getValue(context, tokens[1]);
                var key = getValue(context, tokens[2]);
                var value = getValue(context, tokens[3]);

                if (key instanceof String dictKey) {
                    var map = (Dictionary) array;
                    if (map.isLocked()) {
                        throw new ExecutionException("Record can not be modified.");
                    }

                    map.put(dictKey, value);
                } else if (key instanceof Integer in) {
                    ((Object[]) array)[in] = value;
                }
            }

            // Lock an array. Used by records
            case "dict_lock" -> {
                var array = (Dictionary) getValue(context, tokens[1]);
                array.lock();
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

            default -> System.err.println("Unknown instruction: " + instruction);
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
            } else if (ParserHelper.isNull(literal)) {
                stack.push(null);
            } else if (literal.equals("pop")) {
                var pop = tokenStack.removeLast();

                // TODO: Improve this
                if (pop instanceof String) {
                    if (ParserHelper.isNumber((String) pop)) {
                        stack.push(Integer.parseInt((String) pop));
                    } else if (ParserHelper.isBoolean((String) pop)) {
                        stack.push(Boolean.parseBoolean((String) pop));
                    } else if (pop.equals("null")) {
                        stack.push(null);
                    } else {
                        stack.push(pop);
                    }
                } else {
                    stack.push(pop);
                }
            } else {
                // It's an operator
                operand2 = stack.pop();
                operand1 = stack.pop();

                if (operand1 instanceof String || operand2 instanceof String) {
                    if (operand1 != null && operand1.getClass().isArray()) {
                        // Stringify the array
                        operand1 = LangModule.stringify(operand1);
                    }
                    if (operand2 != null && operand2.getClass().isArray()) {
                        operand2 = LangModule.stringify(operand2);
                    }
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
    }

    private Object getValue(BlockContext context, String raw) {
        var scope = context.getScope();
        if (raw.isEmpty())
            return "";

        char firstCharacter = raw.charAt(0);
        return switch (firstCharacter) {
            case '$' -> {
                // Get value from the registry
                var name = raw.substring(1);
                yield environment.getRegistry(name).getValue(); // Added getValue()
            }
            case '&' -> scope.getVariable(raw.substring(1));
            case '*' -> {
                var variable = scope.getVariable(raw.substring(1));
                if (variable == null) {
                    System.err.println("Trying to access a variable that does not exist!: " + raw);
                    yield null;
                }

                yield variable.getValue();
            }
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

        if (registry.isConstant() && registry.isInitialized()) {
            throw new ExecutionException("Cannot reassign a constant");
        }

        Assertions.assertType(value, registry.getAcceptedType(), "Incompatible types (\"%s\" and \"%s\")".formatted(registry.getAcceptedType(), DataType.of(value)));

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

    @Override
    public String toString() {
        return "BLOCK " + (label != null ? label : functionName);
    }

}
