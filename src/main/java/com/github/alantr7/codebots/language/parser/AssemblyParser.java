package com.github.alantr7.codebots.language.parser;

import com.github.alantr7.codebots.language.runtime.*;

import java.util.Deque;
import java.util.LinkedList;

public class AssemblyParser {

    public static RuntimeCodeBlock parseCodeBlock(Program program, String[] input) {
        var scope = new BlockScope();
        scope.setParent(program.getRootScope());

        return _parseCodeBlock(program,  "__main__", scope, input).block;
    }

    private static ParseResult _parseCodeBlock(Program program, String label, BlockScope scope, String[] input) {
        Deque<RuntimeInstruction> block = new LinkedList<>();
        int position = 0;

        for (int i = 0; i < input.length; i++) {
            var trimmed = input[i].trim();
            if (trimmed.isEmpty())
                continue;

            var tokenized = tokenize(trimmed);
            var instruction = tokenized[0];

            if (instruction.equals("begin")) {
                var nextBlock = new String[input.length - i - 1];
                System.arraycopy(input, i + 1, nextBlock, 0, nextBlock.length);

                var blockScope = new BlockScope();
                blockScope.setParent(scope);
                var subBlock = _parseCodeBlock(program, tokenized.length == 2 ? tokenized[1] : null, blockScope, nextBlock);

                block.add(subBlock.block);
                i += subBlock.blockLength + 1;
                continue;
            }

            if (instruction.equals("end")) {
                return new ParseResult(
                        new RuntimeCodeBlock(program, label, scope, block.toArray(new RuntimeInstruction[0])),
                        i
                );
            }

            block.add(new RuntimeSentence(tokenize(trimmed)));
        }

//        var blockScope = new BlockScope();
//        blockScope.setParent(scope);
        return new ParseResult(
                new RuntimeCodeBlock(program, label, scope, block.toArray(RuntimeInstruction[]::new)),
                input.length
        );
    }

    public static String[] tokenize(String raw) {
        var input = raw.trim();
        var tokens = new LinkedList<String>();
        var token = new StringBuilder();

        boolean underQuotes = false;
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (!underQuotes && ch == ' ') {
                tokens.add(token.toString().replace("\"", ""));
                token.setLength(0);
                continue;
            }

            if (ch == '"') {
                if (input.charAt(i - 1) == '\\')
                    continue;

                underQuotes = !underQuotes;
            }

            token.append(ch);
        }

        if (!token.isEmpty()) {
            tokens.add(token.toString().replace("\"", ""));
        }

        return tokens.toArray(String[]::new);
    }

}
