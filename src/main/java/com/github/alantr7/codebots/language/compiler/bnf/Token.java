package com.github.alantr7.codebots.language.compiler.bnf;

import lombok.Getter;
import lombok.Setter;

public abstract class Token {

    @Getter @Setter
    TokenSpecial count = TokenSpecial.ONE;

}
