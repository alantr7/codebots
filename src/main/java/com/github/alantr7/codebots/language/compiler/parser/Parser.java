package com.github.alantr7.codebots.language.compiler.parser;

import com.github.alantr7.codebots.language.compiler.TokenQueue;
import com.github.alantr7.codebots.language.compiler.parser.element.Module;
import com.github.alantr7.codebots.language.compiler.parser.element.exp.*;
import com.github.alantr7.codebots.language.compiler.parser.element.stmt.*;
import com.github.alantr7.codebots.language.compiler.parser.error.ParserException;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.alantr7.codebots.language.compiler.parser.ParserHelper.error;
import static com.github.alantr7.codebots.language.compiler.parser.ParserHelper.expect;

public class Parser {

    private TokenQueue queue;

    public Module parse(TokenQueue tokens) throws ParserException {
        this.queue = tokens;
        try {
            return parseModule();
        } catch (ParserException exception) {
            System.err.println("Encountered an exception while parsing line " + queue.getLine());
            throw exception;
        }
    }

    private Module parseModule() throws ParserException {
        List<ImportStatement> imports = new LinkedList<>();
        List<Function> functions = new LinkedList<>();
        Map<String, RecordDefinition> records = new LinkedHashMap<>();
        Map<String, VariableDeclareStatement> variables = new LinkedHashMap<>();

        while (!queue.isEmpty()) {
            var keyword = queue.peek();
            if (keyword.equals("function")) {
                var func = nextFunction();
                functions.add(func);
            } else if (keyword.equals("import")) {
                var module = nextImport();
                if (module == null)
                    break;

                imports.add(module);
            } else if (keyword.equals("record")) {
                var record = nextRecord();
                records.put(record.getName(), record);
            } else if (keyword.equals("var") || keyword.equals("const")) {
                var var = (VariableDeclareStatement) nextVariableDeclare();
                variables.put(var.getName(), var);
            } else {
                throw new ParserException("Unexpected token: '" + keyword + "'. Expected 'import', 'function' or 'record'.");
            }
        }

        return new Module(imports.toArray(new ImportStatement[0]), variables, functions.toArray(new Function[0]), records);
    }

    private ImportStatement nextImport() {
        queue.next();
        var name = nextIdentifier();

        if (name == null)
            return null;

        if (!queue.peek().equals("as")) {
            return new ImportStatement(name, name);
        }

        queue.advance();
        var alias = nextIdentifier();

        return new ImportStatement(name, alias);
    }

    private Function nextFunction() throws ParserException {
        queue.next();
        var name = nextIdentifier();

        expect(queue.peek(), "(");
        queue.advance();

        var parameters = new LinkedList<String>();
        while (true) {
            var parameter = nextIdentifier();
            if (parameter == null) break;

            parameters.add(parameter);
            if (queue.peek().equals(",")) {
                queue.advance();
                continue;
            }

            break;
        }

        expect(queue.peek(), ")");
        queue.advance();

        expect(queue.peek(), "{");
        queue.advance();

        var statements = new LinkedList<Statement>();
        while (true) {
            var stmt = nextStatement();
            if (stmt == null)
                break;

            statements.add(stmt);
        }

        expect(queue.peek(), "}");
        queue.advance();

        return new Function(name, parameters.toArray(new String[0]), statements.toArray(new Statement[0]));
    }

    private RecordDefinition nextRecord() throws ParserException {
        queue.next();
        var name = nextIdentifier();

        expect(queue.peek(), "(");
        queue.advance();

        var fields = new LinkedList<String>();
        while (true) {
            var field = nextIdentifier();
            if (field == null) break;

            fields.add(field);
            if (queue.peek().equals(",")) {
                queue.advance();
                continue;
            }

            break;
        }

        expect(queue.peek(), ")");
        queue.advance();

        return new RecordDefinition(name, fields.toArray(String[]::new));
    }

    private Statement nextStatement() throws ParserException {
        return switch (queue.peek()) {
            case "var", "const" -> nextVariableDeclare();
            case "return" -> nextReturnStatement();
            case "if" -> nextIfStatement();
            case "while" -> nextWhileLoop();
            case "do" -> nextDoWhileLoop();
            case "for" -> nextForLoop();
            default -> {
                var stmt = nextVariableAssign();
                if (stmt != null)
                    yield stmt;

                stmt = (Statement) nextFunctionCall();
                if (stmt != null)
                    yield stmt;

                yield null;
            }
        };
    }

    private Statement nextVariableDeclare() throws ParserException {
        if (!queue.peek().equals("var") && !queue.peek().equals("const"))
            return null;

        boolean isConstant = queue.next().equals("const");
        var name = queue.next();

        if (!queue.peek().equals("=")) {
            if (isConstant)
                throw new ParserException("Constants must be initialized!");

            return new VariableDeclareStatement(name, null, false);
        }

        queue.advance();

        var value = nextExpression();
        if (value == null) {
            throw new ParserException("Invalid expression for variable assignment!");
        }

        return new VariableDeclareStatement(name, value, isConstant);
    }

    private Statement nextVariableAssign() throws ParserException {
        var next = nextIdentifier();
        if (next == null) {
            return null;
        }

        VariableAccess target = null;
        var indices = new LinkedList<Expression>();

        // Accessing a variable
        while (queue.peek().equals("[")) {
            queue.advance();
            var index = nextExpression();

            expect(queue.peek(), "]");
            queue.advance();

            indices.add(index);
        }

        target = new VariableAccess(new MemberAccess("this", null), next, indices.toArray(Expression[]::new));

        if (!queue.peek().equals("=")) {
            queue.rollback();
            return null;
        }

        queue.advance();
        var value = nextExpression();

        if (value == null) {
            throw new ParserException("Invalid expression for variable assignment!");
        }

        return new VariableAssignStatement(target, value);

    }

    private IfStatement nextIfStatement() throws ParserException {
        if (!queue.peek().equals("if"))
            return null;

        queue.advance();

        expect(queue.peek(), "(");
        queue.advance();

        var condition = nextExpression();
        if (condition == null) {
            error("Invalid expression for if statement!");
        }

        expect(queue.peek(), ")");
        queue.advance();

        expect(queue.peek(), "{");
        queue.advance();

        var statements = new LinkedList<Statement>();
        while (true) {
            var stmt = nextStatement();
            if (stmt == null)
                break;

            statements.add(stmt);
        }

        expect(queue.peek(), "}");
        queue.advance();

        var elseStatements = new LinkedList<Statement>();
        IfStatement nextIf = null;

        // Check if there's an ELSE keyword
        if (queue.peek().equals("else")) {
            queue.advance();

            // Check if it's an else-if, or just an else
            if (queue.peek().equals("{")) {
                queue.advance();

                while (true) {
                    var stmt = nextStatement();
                    if (stmt == null)
                        break;

                    elseStatements.add(stmt);
                }

                queue.advance(); // "}"
            } else if (queue.peek().equals("if")) {
                nextIf = nextIfStatement();
            }
        }

        return new IfStatement(condition, statements.toArray(new Statement[0]), nextIf, elseStatements.toArray(new Statement[0]));
    }

    private Statement nextReturnStatement() throws ParserException {
        if (!queue.peek().equals("return"))
            return null;

        queue.advance();

        var value = nextExpression();
        if (value == null) {
            error("Invalid expression for return statement.");
        }

        return new ReturnStatement(value);
    }

    private WhileLoopStatement nextWhileLoop() throws ParserException {
        if (!queue.peek().equals("while"))
            return null;

        queue.advance();

        expect(queue.peek(), "(");
        queue.advance();

        var condition = nextExpression();
        if (condition == null) {
            error("Invalid condition for while loop!");
        }

        expect(queue.peek(), ")");
        queue.advance();

        expect(queue.peek(), "{");
        queue.advance();

        var statements = new LinkedList<Statement>();
        while (true) {
            var stmt = nextStatement();
            if (stmt == null)
                break;

            statements.add(stmt);
        }

        expect(queue.peek(), "}");
        queue.advance();

        return new WhileLoopStatement(condition, statements.toArray(new Statement[0]));
    }

    private DoWhileLoopStatement nextDoWhileLoop() throws ParserException {
        if (!queue.peek().equals("do"))
            return null;

        queue.advance();

        expect(queue.peek(), "{");
        queue.advance();

        var statements = new LinkedList<Statement>();
        while (true) {
            var stmt = nextStatement();
            if (stmt == null)
                break;

            statements.add(stmt);
        }

        expect(queue.peek(), "}");
        queue.advance();

        expect(queue.peek(), "while");
        queue.advance();

        expect(queue.peek(), "(");
        queue.advance();

        var condition = nextExpression();
        if (condition == null) {
            error("Invalid condition for do-while loop!");
            return null;
        }

        expect(queue.peek(), ")");
        queue.advance();

        return new DoWhileLoopStatement(condition, statements.toArray(new Statement[0]));
    }

    private ForLoopStatement nextForLoop() throws ParserException {
        if (!queue.peek().equals("for"))
            return null;

        queue.advance();

        expect(queue.peek(), "(");
        queue.advance();

        var init = nextStatement();
        if (init == null) {
            error("Invalid loop init statement!");
        }

        expect(queue.peek(), ";");
        queue.advance();

        var condition = nextExpression();
        if (condition == null) {
            error("Invalid loop condition!");
        }

        expect(queue.peek(), ";");
        queue.advance();

        var update = nextStatement();
        if (update == null) {
            error("Invalid loop update statement!");
        }

        expect(queue.peek(), ")");
        queue.advance();

        expect(queue.peek(), "{");
        queue.advance();

        var statements = new LinkedList<Statement>();
        while (true) {
            var stmt = nextStatement();
            if (stmt == null)
                break;

            statements.add(stmt);
        }

        expect(queue.peek(), "}");
        queue.advance();

        return new ForLoopStatement(init, condition, update, statements.toArray(new Statement[0]));
    }

    private Expression nextExpression() throws ParserException {
        int j = 0;
        var stack = new Stack<String>();

        var postfix = new LinkedList<Expression>();

        stack.push("#");

        boolean expectsOperator = false;
        int parenthesisOpen = 0;

        while (!queue.isEmpty()) {
            var next = queue.peek();

            queue.advance();

            if (expectsOperator && !ParserHelper.isOperator(next)) {
                queue.rollback();
                break;
            }

            if (next.equals(")") && parenthesisOpen == 0) {
                queue.rollback();
                break;
            }

            if (next.equals(";")) {
                queue.rollback();
                break;
            }

            // TODO: Used !isOperator before, it must support parenthesis!
            if (ParserHelper.isNumber(next)) {
                postfix.add(new LiteralExpression(Integer.parseInt(next), LiteralExpression.INT));
                j++;

                expectsOperator = true;
            } else if (ParserHelper.isBoolean(next)) {
                postfix.add(new LiteralExpression(Boolean.parseBoolean(next), LiteralExpression.BOOL));
                j++;

                expectsOperator = true;
            } else if (ParserHelper.isOperator(next)) {
                if (next.equals("(")) {
                    stack.push(next);
                    parenthesisOpen++;
//                }
                } else {
                    if (next.equals(")")) {
                        if (stack.isEmpty())
                            return null;

                        while (!stack.peek().equals("(")) {
                            var popInParenthesis = stack.pop();
                            postfix.add(new LiteralExpression(popInParenthesis, LiteralExpression.INT));
                            j++;
                        }

                        parenthesisOpen--;
                        stack.pop(); // pop out '('
                    } else {

                        if (ParserHelper.getPrecedence(next) > ParserHelper.getPrecedence(stack.peek())) {
                            stack.push(next);
                        } else {
                            while (ParserHelper.getPrecedence(next) <= ParserHelper.getPrecedence(stack.peek())) {
                                postfix.add(new LiteralExpression(stack.pop(), LiteralExpression.OPERATOR));
                                j++;
                            }

                            stack.push(next);
                        }

                        expectsOperator = false;
//                    }
//                }
                    }
                }
            } else {

                // Check if it's a record instantiation
                if (next.equals("new")) {
                    var recordInstantiate = nextRecordInstantiate();
                    if (recordInstantiate == null)
                        break;

                    expectsOperator = true;
                    postfix.add(recordInstantiate);
                    continue;
                }

                if (next.startsWith("\"") && next.endsWith("\"")) {
                    postfix.add(new LiteralExpression(next.substring(1, next.length() - 1), LiteralExpression.STRING));
                } else {
                    queue.rollback();

                    var memberAccess = nextMemberAccessOrArrayOrCall();
                    if (memberAccess == null) {
                        break;
                    } else {
                        System.out.println("Member access: " + memberAccess);
                        postfix.add(memberAccess);
                    }
                }

                expectsOperator = true;

            }

        }

        while (!stack.peek().equals("#")) {
            var pop = stack.pop();
            postfix.add(new LiteralExpression(pop, LiteralExpression.OPERATOR));
            j++;
        }

        return new PostfixExpression(postfix.toArray(Expression[]::new));
    }

    private String nextIdentifier() {
        return queue.peek().matches("[a-zA-Z_]+") ? queue.next() : null;
    }

    private Expression nextRecordInstantiate() throws ParserException {
        var recordName = nextIdentifier();
        if (recordName == null) {
            queue.rollback();
            return null;
        }

        expect(queue.peek(), "(");
        queue.advance();

        var arguments = new LinkedList<Expression>();
        while (true) {
            var argument = nextExpression();
            if (argument == null) {
                break;
            }

            arguments.add(argument);
            if (queue.peek().equals(",")) {
                queue.advance();
                continue;
            }

            break;
        }

        expect(queue.peek(), ")");
        queue.advance();

        return new RecordInstantiation(new VariableAccess(new MemberAccess("this", null), recordName, new Expression[0]), arguments.toArray(new Expression[0]));
    }

    private Expression nextFunctionCall() throws ParserException {
        String identifier = nextIdentifier();
        if (identifier == null) {
            return null;
        }

        var access = new LinkedList<String>();
        access.add(identifier);

        while (queue.peek().equals(".")) {
            queue.advance();
            var nextIdentifier = nextIdentifier();
            if (nextIdentifier == null) {
                return null;
            }

            access.add(nextIdentifier);
        }

        String name = access.getLast();
        MemberAccess target = null;

        access.removeLast();

        if (access.isEmpty()) {
            target = new MemberAccess("this", null);
        } else {
            var iterator = access.descendingIterator();
            while (iterator.hasNext()) {
                target = new MemberAccess(iterator.next(), target);
            }
        }

        if (!queue.peek().equals("(")) {
            queue.rollback();
            return null;
        }

        queue.advance();

        List<Expression> arguments = new LinkedList<>();
        if (!queue.peek().equals(")")) {
            while (true) {
                var argument = nextExpression();
                if (argument == null) break;

                arguments.add(argument);
                if (queue.peek().equals(",")) {
                    queue.advance();
                    continue;
                }

                break;
            }
        }

        if (!queue.peek().equals(")"))
            return null;

        queue.advance();
        return new FunctionCall(new VariableAccess(target, name, new Expression[0]), arguments.toArray(new Expression[0]));
    }

    private Expression nextMemberAccessOrArrayOrCall() throws ParserException {
        String identifier = nextIdentifier();
        if (identifier == null) {
            return null;
        }

        if (identifier.equals("if")) {
            queue.rollback();
            return null;
        }

        var access = new LinkedList<String>();
        access.add(identifier);

        while (queue.peek().equals(".")) {
            queue.advance();
            var nextIdentifier = nextIdentifier();
            if (nextIdentifier == null) {
                return null;
            }

            access.add(nextIdentifier);
        }

        String name = access.getLast();
        MemberAccess target = null;

        access.removeLast();

        if (access.isEmpty()) {
            target = new MemberAccess("this", null);
        } else {
            var iterator = access.descendingIterator();
            while (iterator.hasNext()) {
                target = new MemberAccess(iterator.next(), target);
            }
        }

        var indices = new LinkedList<Expression>();

        if (queue.peek().equals("[")) {
            while (queue.peek().equals("[")) {
                queue.advance();
                var index = (PostfixExpression) nextExpression();
                if (index == null) {
                    return null;
                }

                if (!queue.peek().equals("]")) {
                    return null;
                }

                indices.add(index);
                queue.advance();
            }
        }

        if (!queue.peek().equals("(")) {
            return new VariableAccess(target, name, indices.toArray(new Expression[0]));
        }

        queue.advance();

        List<Expression> arguments = new LinkedList<>();
        if (!queue.peek().equals(")")) {
            while (true) {
                var argument = nextExpression();
                if (argument == null) break;

                arguments.add(argument);
                if (queue.peek().equals(",")) {
                    queue.advance();
                    continue;
                }

                break;
            }
        }

        if (!queue.peek().equals(")"))
            return null;

        queue.advance();
        return new FunctionCall(new VariableAccess(target, name, new Expression[0]), arguments.toArray(new Expression[0]));
    }

}
