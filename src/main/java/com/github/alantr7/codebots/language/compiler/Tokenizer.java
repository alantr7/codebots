package com.github.alantr7.codebots.language.compiler;

import com.github.alantr7.codebots.language.compiler.parser.ParserHelper;

import java.util.LinkedList;
import java.util.List;

public class Tokenizer {

    public static TokenQueue tokenize(String[] input) {
        List<String[]> lines = new LinkedList<>();
        List<Integer> lineNumbers = new LinkedList<>();

        for (int i = 0; i < input.length; i++) {
            var line = input[i];
            var tokenized = tokenizeLine(line);
            if (tokenized.length == 0)
                continue;

            lines.add(tokenized);
            lineNumbers.add(i + 1);
        }

        return new TokenQueue(lines.toArray(String[][]::new), lineNumbers.toArray(Integer[]::new));
    }

    public static TokenQueue tokenize(String input) {
        return tokenize(new String[]{input});
    }

    private static final String SYMBOLS = " ()[]{}<>=!,.\n+-?*/;";

    private static boolean isSymbol(char ch) {
        return SYMBOLS.contains(String.valueOf(ch));
    }

    private static String[] tokenizeLine(String line) {
        var tokens = new LinkedList<String>();
        boolean quotes = false;
        int start = 0;

        for (int i = 1; i < line.length(); i++) {
            var character = line.charAt(i);
            if (character == '"') {
                if (quotes) {
                    tokens.add(line.substring(start, i + 1));
                    start = i + 1;
                }
                quotes = !quotes;
                continue;
            }
            if (quotes)
                continue;

            if (isSymbol(character)) {
                String token = null;

                if (character == '=' && tokens.peekLast() != null) {
                    String multicharSymbols = "<>=!";
                    if (multicharSymbols.contains(tokens.getLast())) {
                        token = tokens.removeLast() + character;
                    }
                }

                if (token == null) {
                    token = line.substring(start, i);
                }

                if (!token.isBlank()) {
                    // Check if it's a negative number
                    if (token.matches("\\d+")) {
                        String previous = tokens.get(tokens.size() - 1);
                        String previous2 = tokens.get(tokens.size() - 2);

                        if (previous.equals("-") && previous2.length() == 1 && isSymbol(previous2.charAt(0))) {
                            tokens.removeLast();
                            tokens.add("0");
                            tokens.add("-");
                        }
                    }
                    tokens.add(token);
                }

                start = i + 1;

                if (character != ' ' && !ParserHelper.isOperator(token))
                    tokens.add(String.valueOf(character));
            }
        }

        var last = line.substring(start);
        if (!last.isBlank())
            tokens.add(last);
        return tokens.toArray(String[]::new);
    }

}
