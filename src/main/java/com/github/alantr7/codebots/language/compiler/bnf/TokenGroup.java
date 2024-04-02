package com.github.alantr7.codebots.language.compiler.bnf;

import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedList;

public class TokenGroup extends Token {

    @Getter
    private final Token[][] branches;

    public TokenGroup(Token[] tokens) {
        var group = new LinkedList<Token>();
        var branches = new LinkedList<Token[]>();
        for (var token : tokens) {
            if (token == TokenSpecial.OR) {
                branches.add(group.toArray(Token[]::new));
                group.clear();
            } else {
                group.add(token);
            }
        }

        if (!group.isEmpty()) {
            branches.add(group.toArray(Token[]::new));
        }

        this.branches = branches.toArray(Token[][]::new);
    }

    @Override
    public String toString() {
        return "group(" + Arrays.toString(Arrays.stream(branches).map(branch -> "branch(" + Arrays.toString(branch) + ")").toArray(String[]::new)) + ")";
    }

}
