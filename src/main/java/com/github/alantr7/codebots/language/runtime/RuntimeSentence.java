package com.github.alantr7.codebots.language.runtime;

public class RuntimeSentence implements RuntimeInstruction {

    private final String[] tokens;

    public RuntimeSentence(String[] tokens) {
        this.tokens = tokens;
    }

    public String getInstruction() {
        return tokens[0];
    }

    public String[] getTokens() {
        return tokens;
    }

    @Override
    public String toString() {
        return String.join(" ", tokens);
    }
}
