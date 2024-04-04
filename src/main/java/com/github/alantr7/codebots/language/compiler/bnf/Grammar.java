package com.github.alantr7.codebots.language.compiler.bnf;

import java.util.LinkedHashMap;
import java.util.Map;

public class Grammar {

    private final Map<String, GrammarRule> rules = new LinkedHashMap<>();

    public ResultNode test(GrammarRule rule, String input) {
        return rule.compile(input);
    }

    public GrammarRule getRule(String name) {
        return rules.get(name);
    }

    void registerRule(String name, GrammarRule rule) {
        rules.put(name, rule);
    }

    Map<String, GrammarRule> getRules() {
        return rules;
    }

}
