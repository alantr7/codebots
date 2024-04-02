package com.github.alantr7.codebots.language.compiler.bnf;

import lombok.Getter;

public class TokenSpecial extends Token {

    public static final TokenSpecial ONE = new TokenSpecial(null);

    public static final TokenSpecial OR = new TokenSpecial('|');

    public static final TokenSpecial ZERO_OR_MORE = new TokenSpecial('*');

    public static final TokenSpecial ZERO_OR_ONE = new TokenSpecial('?');

    public static final TokenSpecial ONE_OR_MORE = new TokenSpecial('+');

    @Getter
    private final Character symbol;

    private TokenSpecial(Character symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return String.valueOf(symbol);
    }

}
