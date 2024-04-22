package com.github.alantr7.codebots.language.parser;

import com.github.alantr7.codebots.language.runtime.*;
import com.github.alantr7.codebots.language.runtime.errors.exceptions.ParseException;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

public class AssemblyParser {

    public static RuntimeCodeBlock parseCodeBlock(Program program, String[] input) throws ParseException {
        var scope = new BlockScope();
        scope.setParent(program.getRootScope());

        return _parseCodeBlock(program, "__main__", scope, BlockType.MAIN, input).block;
    }

    private static ParseResult _parseCodeBlock(Program program, String label, BlockScope scope, BlockType type, String[] input) throws ParseException {
        Deque<RuntimeInstruction> block = new LinkedList<>();

        for (int i = 0; i < input.length; i++) {
            var trimmed = input[i].trim();
            if (trimmed.isEmpty())
                continue;

            var tokenized = tokenize(trimmed);
            var instruction = tokenized[0];

            System.out.println(type.name() + ": " + instruction);
            System.out.println("  " + Arrays.toString(tokenized));

            if (type.blocksInstruction(instruction)) {
                throw new ParseException("Instruction '%s' is not allowed in %s.".formatted(instruction, type.name()));
            }

            if (instruction.equals("begin")) {
                var nextBlock = new String[input.length - i - 1];
                System.arraycopy(input, i + 1, nextBlock, 0, nextBlock.length);

                var blockScope = new BlockScope();
                blockScope.setParent(scope);

                BlockType nextBlockType;
                String nextFunctionName = null;

                if (tokenized.length > 1) {
                    // Probably a loop
                    nextBlockType = BlockType.STANDARD;
                } else if (block.getLast() instanceof RuntimeSentence sentence) {
                    var previousInstruction = sentence.getInstruction();
                    if (previousInstruction.equals("define_func")) {
                        nextBlockType = BlockType.FUNCTION;
                        nextFunctionName = sentence.getTokens()[1];
                    } else {
                        nextBlockType = type;
                    }
                } else {
                    throw new ParseException("Can not have a code block at the start.");
                }

                var subBlock = _parseCodeBlock(program, tokenized.length == 2 ? tokenized[1] : null, blockScope, nextBlockType, nextBlock);
                if (nextBlockType == BlockType.FUNCTION) {
                    subBlock.block.setFunctionName(nextFunctionName);
                }

                block.add(subBlock.block);
                i += subBlock.blockLength + 1;
                continue;
            }

            if (instruction.equals("end")) {
                return new ParseResult(
                        new RuntimeCodeBlock(program, label, scope, type, block.toArray(new RuntimeInstruction[0])),
                        i
                );
            }

            block.add(new RuntimeSentence(tokenize(trimmed)));
        }

//        var blockScope = new BlockScope();
//        blockScope.setParent(scope);
        return new ParseResult(
                new RuntimeCodeBlock(program, label, scope, type, block.toArray(RuntimeInstruction[]::new)),
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
