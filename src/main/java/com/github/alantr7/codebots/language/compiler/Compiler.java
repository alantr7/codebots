package com.github.alantr7.codebots.language.compiler;

import com.github.alantr7.codebots.language.compiler.parser.element.Module;
import com.github.alantr7.codebots.language.compiler.parser.element.exp.*;
import com.github.alantr7.codebots.language.compiler.parser.element.stmt.Function;
import com.github.alantr7.codebots.language.compiler.parser.element.stmt.VariableDeclareStatement;

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
                    .append(" $").append(importS.getAlias())
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

        for (var parameter : function.getParameters()) {
            code.append("define_var ")
                    .append(parameter)
                    .append("\n");
        }

        for (var statement : function.getStatements()) {
            if (statement instanceof VariableDeclareStatement stmt) {
                code.append("  define_var ").append(stmt.getName()).append("\n");
                var ass = (PostfixExpression) stmt.getValue();

                compileExpression(ass, stmt.getName());
            } else if (statement instanceof FunctionCall stmt) {
                compileFunctionCall(stmt);
            }
        }

        code.append("end\n");
    }

    private void compileVariableAccess(VariableAccess var) {
        if (!var.getTarget().getValue().equals("this")) {
            MemberAccess current = var.getTarget();
            while (current != null) {
                code.append("  set $cs ").append(current.getValue()).append("\n");
                current = current.getRight();
            }
        }

        code.append("  push *").append(var.getName()).append("\n");
    }

    private void compileFunctionCall(FunctionCall call) {
        if (!call.getTarget().getValue().equals("this")) {
            MemberAccess current = call.getTarget();
            while (current != null) {
                code.append("  set $cs ").append(current.getValue()).append("\n");
                current = current.getRight();
            }
        }
        code.append("  push_func ").append(call.getValue()).append("\n");
        Expression[] arguments = call.getArguments();
        for (int i = 0; i < arguments.length; i++) {
            var argument = arguments[i];
            compileExpression((PostfixExpression) argument, "$exp1");
            code.append("  set_arg ").append(i).append(" $exp1\n");
        }

        code.append("  call\n  pop_func\n");
    }

    private void compileExpression(PostfixExpression expression, String registry) {
        if (expression.getValue().length == 1 && expression.getValue()[0].isLiteral()) {
            code.append("  set ").append(registry).append(" ").append(expression.getValue()[0].getValue()).append("\n");
            return;
        }

        code.append("  push_stack\n");

        var tokens = new Stack<String>();

        for (var element : expression.getValue()) {
            if (element instanceof FunctionCall call) {
                compileFunctionCall(call);
                code.append("  push $rv\n");
                tokens.push("pop");
            } else if (element instanceof VariableAccess member) {
                compileVariableAccess(member);
                tokens.push("pop");
            } else {
                tokens.push(element.getValue().toString());
            }
        }

        code.append("  eval $exp1 ").append(String.join(" ", tokens.toArray(String[]::new))).append("\n");
        code.append("  pop_stack\n");
    }
}
