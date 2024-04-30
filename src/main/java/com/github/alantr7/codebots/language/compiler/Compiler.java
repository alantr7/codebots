package com.github.alantr7.codebots.language.compiler;

import com.github.alantr7.codebots.language.compiler.parser.element.Module;
import com.github.alantr7.codebots.language.compiler.parser.element.exp.*;
import com.github.alantr7.codebots.language.compiler.parser.element.stmt.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;

public class Compiler {

    StringBuilder code = new StringBuilder();

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

            compileExpression(ass, stmt.getName());
        } else if (statement instanceof VariableAssignStatement stmt) {
            compileExpression((PostfixExpression) stmt.getValue(), stmt.getName());
        } else if (statement instanceof FunctionCall stmt) {
            compileFunctionCall(stmt, false);
        } else if (statement instanceof IfStatement stmt) {
            compileIfStatement(stmt);
        } else if (statement instanceof ReturnStatement stmt) {
            compileReturnStatement(stmt);
        }
    }

    private void compileVariableAccess(VariableAccess var) {
        code.append("  set $cs #cs\n");
        if (!var.getTarget().getValue().equals("this")) {
            MemberAccess current = var.getTarget();
            while (current != null) {
                code.append("  set $cs ").append(current.getValue()).append("\n");
                current = current.getRight();
            }
        }

        code.append("  push *").append(var.getName()).append("\n");
    }

    private void compileFunctionCall(FunctionCall call, boolean push) {
        if (!call.getTarget().getValue().equals("this")) {
            MemberAccess current = call.getTarget();
            while (current != null) {
                code.append("  set $cs *").append(current.getValue()).append("\n");
                current = current.getRight();
            }
        } else {
            code.append("  set $cs #this_module\n");
        }
        code.append("  push_func ").append(call.getValue()).append("\n");
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

    // TODO: Problem with using $rv is that if there's no return value on a function,
    //       $rv will have a value of a function that last returned it
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
            } else if (element instanceof LiteralExpression literal && literal.getValue() instanceof String text && text.contains(" ")) {
                code.append("  push \"").append(text).append("\"\n");
                tokens.push("pop");
            } else {
                tokens.push(element.getValue().toString());
            }
        }

        code.append("  eval $exp1 ").append(String.join(" ", tokens.toArray(String[]::new))).append("\n");
        code.append("  pop_stack\n");
        code.append("  set ").append(registry).append(" $exp1\n");
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

}
