package com.github.alantr7.codebots.language.compiler.bnf;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class ResultNode {

    private final List<ResultNode> children = new LinkedList<>();

    private Token token;

    private Object name;

    private String matched;

    public ResultNode() {
    }

    public ResultNode(Object name) {
        this(name, null);
    }

    public ResultNode(Object name, String matched) {
        this.name = name;
        this.matched = matched;
    }

    @Override
    public String toString() {
        return String.valueOf(name);
    }

    public String tree() {
        return toString(0);
    }

    public String getMatched() {
        if (matched != null)
            return matched;

        return children.size() == 1 ? children.get(0).getMatched() : null;
    }

    private String toStringGetId() {
        var matched = getMatched();
        return matched != null ? ("'" + name + "' = '" + matched + "'") : ("'" + name + "'");
    }

    private String toString(int indent) {
        return children.isEmpty() ? (" ".repeat(indent) + toStringGetId()) :
                (" ".repeat(indent) + toStringGetId() + ":\n" + String.join("\n", children.stream().map(child -> child.toString(indent + 2)).toArray(String[]::new)));
    }

    public void merge(ResultNode node) {
        children.addAll(node.children);
    }

}
