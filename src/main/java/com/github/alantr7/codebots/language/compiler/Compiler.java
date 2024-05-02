package com.github.alantr7.codebots.language.compiler;

import com.github.alantr7.codebots.language.compiler.parser.Parser;
import com.github.alantr7.codebots.language.compiler.parser.element.Module;
import com.github.alantr7.codebots.language.compiler.parser.element.exp.*;
import com.github.alantr7.codebots.language.compiler.parser.element.stmt.*;
import com.github.alantr7.codebots.language.compiler.parser.error.ParserException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;

public class Compiler {

    final StringBuilder code = new StringBuilder();

    final CompileContext context = new CompileContext();

    public String compile(Module module) {
        for (var importS : module.getImports()) {
            code.append("define_var ")
                    .append(importS.getAlias())
                    .append("\n")
                    .append("import ").append(importS.getName())
                    .append(" ").append(importS.getAlias())
                    .append("\n");
        }

        code.append("\n");

        for (var function : module.getFunctions()) {
            compileFunction(function);
        }

        return code.toString();
    }

    private void compileFunction(Function function) {
        code.append("define_func ")
                .append(function.getName())
                .append("\n")
                .append("begin\n");

        var parameters = function.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            var parameter = parameters[i];
            code.append("  define_var ")
                    .append(parameter)
                    .append("\n");
            code.append("  unload_arg ").append(i).append(" ").append(parameter).append("\n");
        }

        for (var statement : function.getStatements()) {
            compileStatement(statement);
        }

        code.append("end\n");
    }

    private void compileStatement(Statement statement) {
        if (statement instanceof VariableDeclareStatement stmt) {
            code.append("  define_var ").append(stmt.getName()).append("\n");
            var ass = (PostfixExpression) stmt.getValue();

            if (ass != null)
                compileExpression(ass, stmt.getName());
        } else if (statement instanceof VariableAssignStatement stmt) {
            code.append("\n");
            code.append("  set $cs #this_module\n");

            compileExpression((PostfixExpression) stmt.getValue(), "$exp3");

            code.append("  set $exp1 *").append(stmt.getTarget().getName()).append("\n");

            var iterator = Arrays.stream(stmt.getTarget().getIndices()).iterator();
            while (iterator.hasNext()) {
                var index = iterator.next();
                compileExpression((PostfixExpression) index, "$exp2");

                if (iterator.hasNext())
                    code.append("  array_get $exp1 $exp2 $exp1\n");
            }

            code.append("  array_set $exp1 $exp2 $exp3\n");
            code.append("\n");
        } else if (statement instanceof FunctionCall stmt) {
            compileFunctionCall(stmt, false);
        } else if (statement instanceof IfStatement stmt) {
            compileIfStatement(stmt);
        } else if (statement instanceof ReturnStatement stmt) {
            compileReturnStatement(stmt);
        } else if (statement instanceof WhileLoopStatement stmt) {
            compileWhileLoop(stmt);
        } else if (statement instanceof DoWhileLoopStatement stmt) {
            compileDoWhileLoop(stmt);
        } else if (statement instanceof ForLoopStatement stmt) {
            compileForLoop(stmt);
        }
    }

    private void compileVariableAccess(VariableAccess var) {
        code.append("  set $cs #this_module\n");
        if (!var.getTarget().getValue().equals("this")) {
            MemberAccess current = var.getTarget();
            while (current != null && !current.getValue().equals("this")) {
                code.append("  set $cs ").append(current.getValue()).append("\n");
                current = current.getRight();
            }
        }

        if (var.getIndices().length != 0) {
            code.append("\n");
            code.append("  set $cs #this_module\n");
            code.append("  set $exp1 *").append(var.getName()).append("\n");

            var iterator = Arrays.stream(var.getIndices()).iterator();
            while (iterator.hasNext()) {
                var index = iterator.next();
                compileExpression((PostfixExpression) index, "$exp2");

                if (iterator.hasNext())
                    code.append("  array_get $exp1 $exp2 $exp1\n");
            }

            code.append("  array_get $exp1 $exp2 $exp1\n");
            code.append("  push $exp1\n");
            code.append("\n");
        } else {
            code.append("  push *").append(var.getName()).append("\n");
        }
    }

    private void compileFunctionCall(FunctionCall call, boolean push) {
        if (!call.getTarget().getValue().equals("this")) {
            var current = call.getTarget().getTarget();
            while (current != null && !current.getValue().equals("this")) {
                code.append("  set $cs *").append(current.getValue()).append("\n");
                current = current.getRight();
            }
        } else {
            code.append("  set $cs #this_module\n");
        }
        code.append("  push_func ").append(call.getValue()).append(" ").append(call.getArguments().length).append("\n");
        Expression[] arguments = call.getArguments();
        for (int i = 0; i < arguments.length; i++) {
            var argument = arguments[i];
            compileExpression((PostfixExpression) argument, "$exp1");
            code.append("  set_arg ").append(i).append(" $exp1\n");
        }

        code.append("  call\n");
        if (push) {
            code.append("  push $rv\n");
        }
        code.append("  pop_func\n");
    }

    private void compileWhileLoop(WhileLoopStatement loop) {
        var name = context.nextLoopName();
        code.append("  begin ").append(name).append("\n");
        compileExpression((PostfixExpression) loop.getExpression(), "$exp1");
        code.append("  if $exp1 false\n");
        code.append("  begin\n");
        code.append("  exit ").append(name).append("\n");
        code.append("  end\n");

        for (var stmt : loop.getBody()) {
            compileStatement(stmt);
        }

        code.append("  goto ").append(name).append("\n");
        code.append("  end\n");
    }

    private void compileDoWhileLoop(DoWhileLoopStatement loop) {
        var name = context.nextLoopName();
        code.append("  begin ").append(name).append("\n");
        for (var stmt : loop.getBody()) {
            compileStatement(stmt);
        }

        compileExpression((PostfixExpression) loop.getExpression(), "$exp1");
        code.append("  if $exp1 false\n");
        code.append("  begin\n");
        code.append("  exit ").append(name).append("\n");
        code.append("  end\n");

        code.append("  goto ").append(name).append("\n");
        code.append("  end\n");
    }

    private void compileForLoop(ForLoopStatement loop) {
        var entry = context.nextLoopEntryName();
        var name = context.nextLoopName();
        code.append("  begin ").append(entry).append("\n");
        compileStatement(loop.getStatement1());

        code.append("  begin ").append(name).append("\n");
        compileExpression((PostfixExpression) loop.getCondition(), "$exp1");

        code.append("  if $exp1 false\n");
        code.append("  begin\n");
        code.append("  exit ").append(entry).append("\n");
        code.append("  end\n");

        for (var stmt : loop.getBody()) {
            compileStatement(stmt);
        }

        compileStatement(loop.getStatement2());
        code.append("  goto ").append(name).append("\n");
        code.append("  end\n");
        code.append("  end\n");
    }

    private void compileExpression(PostfixExpression expression, String registry) {
        if (expression.getValue().length == 1 && expression.getValue()[0].isLiteral()) {
            String value = expression.getValue()[0].getValue() instanceof String text ?
                    ("\"" + text + "\"")
                    : String.valueOf(expression.getValue()[0].getValue());

            code.append("  set ").append(registry).append(" ").append(value).append("\n");
            return;
        }

        code.append("  push_stack\n");

        var tokens = new Stack<String>();

        for (var element : expression.getValue()) {
            if (element instanceof FunctionCall call) {
                compileFunctionCall(call, true);
                tokens.push("pop");
            } else if (element instanceof VariableAccess member) {
                compileVariableAccess(member);
                tokens.push("pop");
            } else if (element instanceof LiteralExpression literal && literal.getLiteralType() == LiteralExpression.STRING) {
                code.append("  push \"").append(literal.getValue()).append("\"\n");
                tokens.push("pop");
            } else {
                tokens.push(element.getValue().toString());
            }
        }

        code.append("  eval ").append(registry).append(" ").append(String.join(" ", tokens.toArray(String[]::new))).append("\n");
        code.append("  pop_stack\n");
    }

    private void compileIfStatement(IfStatement statement) {
        compileExpression((PostfixExpression) statement.getCondition(), "$exp1");
        code.append("  if $exp1 true\n");
        code.append("  begin\n");
        for (var stmt : statement.getBody()) {
            compileStatement(stmt);
        }
        code.append("  end\n");

        if (statement.getElseBody().length > 0 || statement.getElseIf() != null) {
            code.append("  else\n");
            code.append("  begin\n");
            if (statement.getElseIf() != null) {
                compileIfStatement(statement.getElseIf());
            } else {
                for (var stmt : statement.getElseBody()) {
                    compileStatement(stmt);
                }
            }
            code.append("  end\n");
        }
    }

    private void compileReturnStatement(ReturnStatement stmt) {
        compileExpression((PostfixExpression) stmt.getValue(), "$rv");
        code.append("  exit_func\n");
    }

    public static String compileModule(String code) throws ParserException {
        var tokens = Tokenizer.tokenize(code.split("\n"));
        var parser = new Parser();

        var parsed = parser.parse(tokens);
        return compileModule(parsed);
    }

    public static String compileModule(Module module) {
        return new Compiler().compile(module);
    }

}
