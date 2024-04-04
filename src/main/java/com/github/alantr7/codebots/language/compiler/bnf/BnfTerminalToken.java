package com.github.alantr7.codebots.language.compiler.bnf;

import lombok.Getter;

public class BnfTerminalToken extends Token {

    @Getter
    private final String value;

    public BnfTerminalToken(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return count == TokenSpecial.ONE ? value : (value + count.getSymbol());
    }

    // TODO: Implement testing logic here
    @Override
    GrammarRule.TestResult test(TestContext context, String input) {
        return input.startsWith(value)
                ? new GrammarRule.TestResult(true, input.substring(1), context.getRule())
                : new GrammarRule.TestResult(false, input, null);
    }

}
