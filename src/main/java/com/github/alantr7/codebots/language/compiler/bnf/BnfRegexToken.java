package com.github.alantr7.codebots.language.compiler.bnf;

import lombok.Getter;

public class BnfRegexToken extends Token {

    @Getter
    private final String regex;

    public BnfRegexToken(String regex) {
        this.regex = regex;
    }

    @Override
    public String toString() {
        return "/" + regex + "/";
    }

}
