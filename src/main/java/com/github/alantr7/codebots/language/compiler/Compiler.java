package com.github.alantr7.codebots.language.compiler;

import com.github.alantr7.codebots.language.compiler.bnf.Grammar;
import com.github.alantr7.codebots.language.compiler.bnf.ResultNode;
import com.github.alantr7.codebots.language.runtime.RuntimeCodeBlock;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

public class Compiler {

    public static RuntimeCodeBlock compile(Grammar grammar, String input) {
        System.out.println("Parsing " + input + " with grammar: " + grammar);
        var tree = grammar.test(grammar.getRule("program"), input);

        System.out.println("Tree:");
        System.out.println(tree.tree());

        var code = new StringBuilder();
        compile(tree, code, 0);

        System.out.println("Compiled:");
        System.out.println(code);

        return null;
    }

    private static void compile(ResultNode node, StringBuilder code, int indent) {
        switch (node.getName()) {
            case "program" -> {
                compileChildren(node, code, indent + 2);
            }
            case "define_var" -> {
                indent(code, indent);
                code.append("define_var ");
                code.append(node.getChild("name").getMatched());
                code.append(" int");
                code.append("\n");
            }
            case "define_func" -> {
                indent(code, indent);
                code.append("define_func ");
                code.append(node.getChild("name").getMatched());
                code.append("\n");

                indent(code, indent);
                code.append("begin");
                code.append("\n");

                var parameters = node.getChild("parameters");
                var index = new AtomicInteger(0);
                parameters.getChildren().forEach(param -> {
                    if (!param.getName().equals("name"))
                        return;

                    indent(code, indent + 2);
                    code.append("define_var arg").append(index.get()).append("\n");

                    indent(code, indent + 2);
                    code.append("unload_arg 0 &arg").append(index.get()).append("\n");

                    index.incrementAndGet();
                });

                var body = node.getChild("function_block");
                if (body != null) {
                    compileChildren(body, code, indent);
                }

                indent(code, indent);
                code.append("end");
            }

            case "function_block" -> {
                compileChildren(node, code, indent - 2);
            }

            case "call_function" -> {
                indent(code, indent);
                code.append("push_func ").append(node.getChild("name")).append("\n");

                var arguments = node.getChild("call_args");
                int index = 0;
                for (var argument : arguments.getChildren()) {
                    indent(code, indent + 2);
                    code.append("set_arg ").append(index).append(" ").append("i").append(argument.getMatched()).append("\n");
                }

                indent(code, indent + 2);
                code.append("call\n");

                indent(code, indent);
                code.append("pop_func").append("\n");
            }
        }
    }

    private static void compileChildren(ResultNode node, StringBuilder code, int indent) {
        node.getChildren().forEach(child -> compile(child, code, indent + 2));
    }

    private static void indent(StringBuilder code, int indent) {
        code.append(" ".repeat(indent));
    }

}