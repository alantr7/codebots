package com.github.alantr7.codebots.language.compiler.bnf;

import java.util.HashMap;
import java.util.Map;

public class BnfGrammar {

    private final Map<String, BnfRule> rules;

    public BnfGrammar(Map<String, BnfRule> rules) {
        this.rules = rules;
    }

    public boolean test(BnfRule rule, String input) {
        return rule.test(input);
    }

    public BnfRule getRule(String name) {
        return rules.get(name);
    }

}
