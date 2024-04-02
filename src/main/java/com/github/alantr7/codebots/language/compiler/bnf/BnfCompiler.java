package com.github.alantr7.codebots.language.compiler.bnf;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class BnfCompiler {

    public static BnfGrammar compile(String[] input) {
        var tokens = tokenize(input);
        var rules = new HashMap<String, BnfRule>();

        for (var rule : tokens) {
            var name = rule[0].substring(1, rule[0].length() - 1);
            var assignSymbol = rule[1];

            if (!assignSymbol.equals("::=")) {
                throw new RuntimeException("Invalid syntax!: '" + assignSymbol + "'");
            }

            var group = (TokenGroup) compileTokenGroup(rules, rule, 2)[0];
            System.out.println(group.toString());

            var actualRule = new BnfRule(name, group, rules);
            rules.put(name, actualRule);
        }

        return new BnfGrammar(rules);
    }

    private static Object[] compileTokenGroup(Map<String, BnfRule> rules, String[] rule, int i) {
        var subTokens = new LinkedList<Token>();
        for (; i < rule.length; i++) {
            var token = rule[i];
            Token actualToken;

            // NON TERMINAL TOKEN
            if (token.charAt(0) == '<') {
                actualToken = parseNonTerminalToken(rules, token);
            } else if (token.charAt(0) == '"') {
                actualToken = parseTerminalToken(token);
            } else if (token.charAt(0) == '[') {
                actualToken = parseIntervalToken(token);
            } else if (token.charAt(0) == '(') {
                var result = compileTokenGroup(rules, rule, i + 1);
                actualToken = (Token) result[0];

                subTokens.add(actualToken);
                i = (int) result[1];
                continue;
            } else if (token.charAt(0) == ')') {
                var group = new TokenGroup(subTokens.toArray(Token[]::new));
                return new Object[]{
                        group,
                        i
                };
            } else if (token.charAt(0) == '|') {
                actualToken = TokenSpecial.OR;
            } else if (token.charAt(0) == '?') {
                actualToken = TokenSpecial.ZERO_OR_ONE;
            } else if (token.charAt(0) == '*') {
                actualToken = TokenSpecial.ZERO_OR_MORE;
            } else if (token.charAt(0) == '+') {
                actualToken = TokenSpecial.ONE_OR_MORE;
            } else {
                throw new RuntimeException("Invalid token. It's neither terminal or non-terminal: '%s'".formatted(token));
            }

            if (subTokens.peekLast() != null) {
                if (actualToken != TokenSpecial.OR && actualToken instanceof TokenSpecial operator) {
                    subTokens.getLast().setCount(operator);
                    continue;
                }
            }

            subTokens.add(actualToken);
        }

        // Find ORs, and create branches


        var group = new TokenGroup(subTokens.toArray(Token[]::new));
        return new Object[]{group, 0};
    }

    private static Token parseNonTerminalToken(Map<String, BnfRule> rules, String input) {
        String name = input.substring(1, input.indexOf('>'));
        return new BnfNonTerminalToken(name, rules);
    }

    private static Token parseTerminalToken(String input) {
        String name = input.substring(1, input.lastIndexOf('"'));
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

                continue;
            }
        }

        var last = line.substring(start);
        if (!last.isBlank())
            tokens.add(last);
        return tokens.toArray(String[]::new);
    }

    private static char[] PATTERN_BREAK = {
            ' ', '(', ')', '{', '}', '.', '\n', '+', '?', '*'
    };

    private static boolean isSymbol(char ch) {
        for (int i = 0; i < PATTERN_BREAK.length; i++) {
            if (PATTERN_BREAK[i] == ch)
                return true;
        }
        return false;
    }

}
