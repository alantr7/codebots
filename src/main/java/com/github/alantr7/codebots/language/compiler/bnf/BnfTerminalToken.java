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

}
