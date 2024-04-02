package com.github.alantr7.codebots.language.runtime.modules.outline;

import com.github.alantr7.codebots.language.runtime.BlockType;
import com.github.alantr7.codebots.language.runtime.RuntimeCodeBlock;
import com.github.alantr7.codebots.language.runtime.RuntimeSentence;
import com.github.alantr7.codebots.language.runtime.ValueType;
import com.github.alantr7.codebots.language.runtime.modules.Module;
import lombok.Getter;

import java.util.LinkedList;

public class ModuleOutline {

    @Getter
    private final OutlineEntry root;

    public ModuleOutline(OutlineEntry root) {
        this.root = root;
    }

    public String toString() {
        return stringify(root, 0);
    }

    private static String stringify(OutlineEntry entry, int indent) {
        var builder = new StringBuilder();
        builder.append(" ".repeat(indent));
        builder.append(entry.getName() + ": " + (entry.getType() == OutlineEntry.Type.BLOCK ? "BLOCK" : entry.getValueType().name()));
        builder.append("\n");

        for (var child : entry.getChildren()) {
            builder.append(stringify(child, indent + 2));
        }

        return builder.toString();
    }

    public static ModuleOutline forModule(Module module) {
        var root = read(OutlineEntry.Type.BLOCK, "__main__", module.getBlock());
        return new ModuleOutline(root);
    }

    private static OutlineEntry read(OutlineEntry.Type type, String name, RuntimeCodeBlock block) {
        var instructions = block.getBlock();
        var children = new LinkedList<OutlineEntry>();

        for (var instruction : instructions) {
            if (instruction instanceof RuntimeSentence sentence) {
                if (sentence.getInstruction().equals("define_var")) {
                    children.add(new OutlineEntry(OutlineEntry.Type.VARIABLE, sentence.getTokens()[1], ValueType.fromString(sentence.getTokens()[2]), new OutlineEntry[0]));
                }
                continue;
            }

            if (instruction instanceof RuntimeCodeBlock nestedBlock) {
                if (nestedBlock.getBlockType() == BlockType.FUNCTION) {
                    children.add(read(OutlineEntry.Type.BLOCK, nestedBlock.getFunctionName(), nestedBlock));
                    continue;
                }

                if (nestedBlock.getBlockType() == BlockType.STANDARD) {
                    children.add(read(OutlineEntry.Type.BLOCK, nestedBlock.getLabel(), nestedBlock));
                    continue;
                }
            }
        }

        return new OutlineEntry(type, name, ValueType.ANY, children.toArray(OutlineEntry[]::new));
    }

}
