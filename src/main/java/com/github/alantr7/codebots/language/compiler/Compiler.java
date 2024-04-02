package com.github.alantr7.codebots.language.compiler;

import com.github.alantr7.codebots.language.runtime.RuntimeCodeBlock;

import java.util.Arrays;
import java.util.LinkedList;

public class Compiler {

    public static RuntimeCodeBlock compile(String[] input) {
        // try and compile...
        var tokens = tokenize(input);
        Arrays.stream(tokens).forEach(line -> System.out.println(Arrays.toString(Arrays.stream(line).map(token -> "'" + token + "'").toArray(String[]::new))));



        return null;
    }

    private static String[][] tokenize(String[] lines) {
        var tokensPerLines = new LinkedList<String[]>();
        for (var raw : lines) {
            var line = raw.trim();
            if (line.isBlank())
                continue;

            var tokenized = tokenizeLine(line);
            tokensPerLines.add(tokenized);
        }

        return tokensPerLines.toArray(String[][]::new);
    }

    private static String[] tokenizeLine(String line) {
        var tokens = new LinkedList<String>();
        String currentToken = "";
        int start = 0;
        for (int i = 1; i < line.length(); i++) {
            var character = line.charAt(i);
            if (isSymbol(character)) {
                var token = line.substring(start, i);
                if (!token.isBlank())
                    tokens.add(token);

                start = i + 1;

                if (character != ' ')
                    tokens.add(String.valueOf(character));

                continue;
            }
        }

        tokens.add(line.substring(start));
        return tokens.toArray(String[]::new);
    }

    private static char[] PATTERN_BREAK = {
            ' ', '"', '(', ')', '{', '}', '.', '\n'
    };

    private static boolean isSymbol(char ch) {
        for (int i = 0; i < PATTERN_BREAK.length; i++) {
            if (PATTERN_BREAK[i] == ch)
                return true;
        }
        return false;
    }

}