package com.github.alantr7.codebots.language.compiler.bnf;

import java.util.LinkedList;
import java.util.Map;

public class BnfParser {

    public static Grammar parse(String[] input) {
        var tokens = tokenize(input);
        var grammar = new Grammar();

        for (var rule : tokens) {
            var name = rule[0].substring(1, rule[0].length() - 1);
            var assignSymbol = rule[1];

            if (!assignSymbol.equals("::=")) {
                throw new RuntimeException("Invalid syntax!: '" + assignSymbol + "'");
            }

            var group = (TokenGroup) compileTokenGroup(grammar, rule, 2)[0];
            System.out.println(group.toString());

            var actualRule = new GrammarRule(grammar, name, group);
            grammar.registerRule(name, actualRule);
        }

        return grammar;
    }

    private static Object[] compileTokenGroup(Grammar grammar, String[] rule, int i) {
        var rules = grammar.getRules();
        var subTokens = new LinkedList<Token>();
        for (; i < rule.length; i++) {
            var rawToken = rule[i];
            Token token;

            switch (rawToken.charAt(0)) {
                case '<' -> token = parseNonTerminalToken(rules, rawToken);
                case '"' -> token = parseTerminalToken(rawToken);
                case '[' -> token = parseIntervalToken(rawToken);
                case '(' -> {
                    var result = compileTokenGroup(grammar, rule, i + 1);
                    token = (Token) result[0];

                    subTokens.add(token);
                    i = (int) result[1];
                    continue;
                }
                case ')' -> {
                    var group = new TokenGroup(subTokens.toArray(Token[]::new));
                    return new Object[]{
                            group,
                            i
                    };
                }
                case '|' -> token = TokenSpecial.OR;
                case '?' -> token = TokenSpecial.ZERO_OR_ONE;
                case '*' -> token = TokenSpecial.ZERO_OR_MORE;
                case '+' -> token = TokenSpecial.ONE_OR_MORE;
                default ->
                        throw new RuntimeException("Invalid token. It's neither terminal or non-terminal: '%s'".formatted(rawToken));
            }

            if (subTokens.peekLast() != null) {
                if (token != TokenSpecial.OR && token instanceof TokenSpecial operator) {
                    subTokens.getLast().setCount(operator);
                    continue;
                }
            }

            subTokens.add(token);
        }

        // Find ORs, and create branches
        var group = new TokenGroup(subTokens.toArray(Token[]::new));
        return new Object[]{group, 0};
    }

    private static Token parseNonTerminalToken(Map<String, GrammarRule> rules, String input) {
        String name = input.substring(1, input.indexOf('>'));
        return new BnfNonTerminalToken(name, rules);
    }

    private static Token parseTerminalToken(String input) {
        String name = input.substring(1, input.lastIndexOf('"')).replace("\\n", "\r\n");
        return new BnfTerminalToken(name);
    }

    private static Token parseIntervalToken(String input) {
        return new BnfRegexToken(input);
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
                var token = line.substring(start, i);
                if (!token.isBlank())
                    tokens.add(token);

                start = i + 1;

                if (character != ' ')
                    tokens.add(String.valueOf(character));
            }
        }

        var last = line.substring(start);
        if (!last.isBlank())
            tokens.add(last);
        return tokens.toArray(String[]::new);
    }

    private static final String SYMBOLS = " (){}.\n+?*";

    private static boolean isSymbol(char ch) {
        return SYMBOLS.contains(String.valueOf(ch));
    }

}
