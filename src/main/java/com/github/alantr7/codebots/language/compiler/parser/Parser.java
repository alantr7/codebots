package com.github.alantr7.codebots.language.compiler.parser;

import com.github.alantr7.codebots.language.compiler.TokenQueue;
import com.github.alantr7.codebots.language.compiler.parser.element.Module;
import com.github.alantr7.codebots.language.compiler.parser.element.exp.*;
import com.github.alantr7.codebots.language.compiler.parser.element.stmt.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class Parser {

    private TokenQueue queue;

    public Module parse(TokenQueue tokens) {
        this.queue = tokens;
        return parseModule();
    }

    private Module parseModule() {
        List<ImportStatement> imports = new LinkedList<>();
        List<Function> functions = new LinkedList<>();

        while (!queue.isEmpty()) {
            var keyword = queue.peek();
            if (keyword.equals("function")) {
                var func = nextFunction();
                if (func == null)
                    break;

                functions.add(func);
            } else if (keyword.equals("import")) {
                var module = nextImport();
                if (module == null)
                    break;

                imports.add(module);
            } else {
                System.err.println("Unexpected token: '" + keyword + "'. Expected 'import' or 'function'.");
            }
        }

        return new Module(imports.toArray(new ImportStatement[0]), functions.toArray(new Function[0]));
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

    private Function nextFunction() {
        queue.next();
        var name = nextIdentifier();

        if (!queue.peek().equals("(")) {
            System.err.println("Unexpected symbol: " + queue.peek() + ". Was expecting '('.");
            return null;
        }
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

        if (!queue.peek().equals(")")) {
            System.err.println("Unexpected symbol: " + queue.peek() + ". Was expecting ')'.");
            return null;
        }
        queue.advance();

        if (!queue.peek().equals("{")) {
            System.err.println("Unexpected symbol: " + queue.peek() + ". Was expecting '{'.");
            return null;
        }
        queue.advance();

        var statements = new LinkedList<Statement>();
        while (true) {
            var stmt = nextStatement();
            if (stmt == null)
                break;

            statements.add(stmt);
        }

        if (!queue.peek().equals("}")) {
            System.err.println("Unexpected symbol: " + queue.peek() + ". Was expecting '}'.");
            return null;
        }
        queue.advance();

        return new Function(name, parameters.toArray(new String[0]), statements.toArray(new Statement[0]));
    }

    // TODO: Parse statement by peeking at the next identifier
    private Statement nextStatement() {
        Statement stmt;

        stmt = nextVariableDeclare();
        if (stmt != null)
            return stmt;

        stmt = nextReturnStatement();
        if (stmt != null)
            return stmt;

        stmt = nextVariableAssign();
        if (stmt != null)
            return stmt;

        stmt = nextIfStatement();
        if (stmt != null)
            return stmt;

        stmt = nextWhileLoop();
        if (stmt != null)
            return stmt;

        stmt = nextDoWhileLoop();
        if (stmt != null)
            return stmt;

        stmt = nextForLoop();
        if (stmt != null)
            return stmt;

        stmt = (Statement) nextFunctionCall();
        if (stmt == null)
            return null;

        return stmt;
    }

    private Statement nextVariableDeclare() {
        if (!queue.peek().equals("var"))
            return null;

        queue.advance();
        var name = queue.next();

        if (!queue.peek().equals("="))
            return new VariableDeclareStatement(name, null);

        queue.advance();

        var value = nextExpression();
        if (value == null) {
            System.err.println("Invalid syntax!");
            return null;
        }

        return new VariableDeclareStatement(name, value);
    }

    private Statement nextVariableAssign() {
        var next = nextIdentifier();
        if (next == null) {
            return null;
        }

        System.out.println("Assigning " + next);
        System.out.println("Next: " + queue.peek());

        if (!queue.peek().equals("=")) {
            queue.rollback();
            return null;
        }

        queue.advance();
        var value = nextExpression();

        if (value == null) {
            System.err.println("Invalid expression for variable assignment!");
            return null;
        }

        return new VariableAssignStatement(new MemberAccess("this", null), next, value);
    }

    private IfStatement nextIfStatement() {
        if (!queue.peek().equals("if"))
            return null;

        queue.advance();
        if (!queue.peek().equals("(")) {
            System.err.println("Unexpected symbol: " + queue.peek() + ". Was expecting '('.");
            return null;
        }
        queue.advance();

        var condition = nextExpression();
        if (condition == null) {
            System.err.println("Invalid syntax!");
            return null;
        }

        if (!queue.peek().equals(")")) {
            System.err.println("Unexpected symbol: " + queue.peek() + ". Was expecting ')'.");
            return null;
        }
        queue.advance();

        if (!queue.peek().equals("{")) {
            System.err.println("Unexpected symbol: " + queue.peek() + ". Was expecting '{'.");
            return null;
        }
        queue.advance();

        var statements = new LinkedList<Statement>();
        while (true) {
            var stmt = nextStatement();
            if (stmt == null)
                break;

            statements.add(stmt);
        }

        if (!queue.peek().equals("}")) {
            System.err.println("Unexpected symbol: " + queue.peek() + ". Was expecting '}'.");
            return null;
        }
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

    private Statement nextReturnStatement() {
        if (!queue.peek().equals("return"))
            return null;

        queue.advance();

        var value = nextExpression();
        if (value == null) {
            System.err.println("Invalid expression for return statement.");
            return null;
        }

        return new ReturnStatement(value);
    }

    private WhileLoopStatement nextWhileLoop() {
        if (!queue.peek().equals("while"))
            return null;

        queue.advance();
        if (!queue.peek().equals("(")) {
            System.err.println("Unexpected symbol: " + queue.peek() + ". Was expecting '('.");
            return null;
        }
        queue.advance();

        var condition = nextExpression();
        if (condition == null) {
            System.err.println("Invalid syntax!");
            return null;
        }

        if (!queue.peek().equals(")")) {
            System.err.println("Unexpected symbol: " + queue.peek() + ". Was expecting ')'.");
            return null;
        }
        queue.advance();

        if (!queue.peek().equals("{")) {
            System.err.println("Unexpected symbol: " + queue.peek() + ". Was expecting '{'.");
            return null;
        }
        queue.advance();

        var statements = new LinkedList<Statement>();
        while (true) {
            var stmt = nextStatement();
            if (stmt == null)
                break;

            statements.add(stmt);
        }

        if (!queue.peek().equals("}")) {
            System.err.println("Unexpected symbol: " + queue.peek() + ". Was expecting '}'.");
            return null;
        }
        queue.advance();

        return new WhileLoopStatement(condition, statements.toArray(new Statement[0]));
    }

    private DoWhileLoopStatement nextDoWhileLoop() {
        if (!queue.peek().equals("do"))
            return null;

        queue.advance();

        if (!queue.peek().equals("{")) {
            System.err.println("Unexpected symbol: " + queue.peek() + ". Was expecting '{'.");
            return null;
        }
        queue.advance();

        var statements = new LinkedList<Statement>();
        while (true) {
            var stmt = nextStatement();
            if (stmt == null)
                break;

            statements.add(stmt);
        }

        if (!queue.peek().equals("}")) {
            System.err.println("Unexpected symbol: " + queue.peek() + ". Was expecting '}'.");
            return null;
        }
        queue.advance();

        if (!queue.peek().equals("while")) {
            System.err.println("Unexpected symbol: " + queue.peek() + ". Was expecting 'while'.");
            return null;
        }
        queue.advance();

        if (!queue.peek().equals("(")) {
            System.err.println("Unexpected symbol: " + queue.peek() + ". Was expecting '('.");
            return null;
        }
        queue.advance();

        var condition = nextExpression();
        if (condition == null) {
            System.err.println("Invalid syntax!");
            return null;
        }

        if (!queue.peek().equals(")")) {
            System.err.println("Unexpected symbol: " + queue.peek() + ". Was expecting ')'.");
            return null;
        }
        queue.advance();

        return new DoWhileLoopStatement(condition, statements.toArray(new Statement[0]));
    }

    private ForLoopStatement nextForLoop() {
        if (!queue.peek().equals("for"))
            return null;

        queue.advance();
        if (!queue.peek().equals("(")) {
            System.err.println("Unexpected symbol: " + queue.peek() + ". Was expecting '('.");
            return null;
        }
        queue.advance();

        var init = nextStatement();
        if (init == null) {
            System.err.println("Invalid loop init statement!");
            return null;
        }

        if (!queue.peek().equals(";")) {
            System.err.println("Unexpected symbol: " + queue.peek() + ". Was expecting ';'.");
            return null;
        }
        queue.advance();

        var condition = nextExpression();
        if (condition == null) {
            System.err.println("Invalid loop condition!");
            return null;
        }

        if (!queue.peek().equals(";")) {
            System.err.println("Unexpected symbol: " + queue.peek() + ". Was expecting ';'.");
            return null;
        }
        queue.advance();

        var update = nextStatement();
        if (update == null) {
            System.err.println("Invalid loop update statement!");
            return null;
        }

        if (!queue.peek().equals(")")) {
            System.err.println("Unexpected symbol: " + queue.peek() + ". Was expecting ')'.");
            return null;
        }
        queue.advance();

        if (!queue.peek().equals("{")) {
            System.err.println("Unexpected symbol: " + queue.peek() + ". Was expecting '{'.");
            return null;
        }
        queue.advance();

        var statements = new LinkedList<Statement>();
        while (true) {
            var stmt = nextStatement();
            if (stmt == null)
                break;

            statements.add(stmt);
        }

        if (!queue.peek().equals("}")) {
            System.err.println("Unexpected symbol: " + queue.peek() + ". Was expecting '}'.");
            return null;
        }
        queue.advance();

        return new ForLoopStatement(init, condition, update, statements.toArray(new Statement[0]));
    }

    private Expression nextExpression() {
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
                            System.out.println("Pop in parenthesis: " + popInParenthesis);
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
            } else  {

                if (next.startsWith("\"") && next.endsWith("\"")) {
                    postfix.add(new LiteralExpression(next.substring(1, next.length() - 1), LiteralExpression.STRING));
                } else {
                    queue.rollback();
                    var memberAccess = nextMemberAccessOrCall();
                    if (memberAccess == null) {
                        break;
                    } else {
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

    private Integer nextNumber() {
        var token = queue.peek();
        var number = token.matches("\\d+") ? Integer.parseInt(token) : null;

        if (number == null)
            return null;

        queue.advance();
        return number;
    }

    private String nextIdentifier() {
        return queue.peek().matches("[a-zA-Z]+") ? queue.next() : null;
    }

    private Expression nextFunctionCall() {
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
        return new FunctionCall(target, name, arguments.toArray(new Expression[0]));
    }

    private Expression nextMemberAccessOrCall() {
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

        if (!queue.peek().equals("(")) {
            return new VariableAccess(target, name);
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
        return new FunctionCall(target, name, arguments.toArray(new Expression[0]));
    }

}
