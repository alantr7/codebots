package com.github.alantr7.codebots.language.compiler.bnf;

import lombok.Getter;

import java.util.Map;

public class BnfNonTerminalToken extends Token {

    @Getter
    private final String name;

    private final Map<String, BnfRule> rules;

    public BnfNonTerminalToken(String name, Map<String, BnfRule> rules) {
        this.name = name;
        this.rules = rules;
    }

    @Override
    public String toString() {
        return count == TokenSpecial.ONE ? "<" + name + ">" : ("<" + name + ">" + count.getSymbol());
    }

}
