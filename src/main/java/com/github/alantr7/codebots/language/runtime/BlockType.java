package com.github.alantr7.codebots.language.runtime;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum BlockType {

    STANDARD(
            "import",
            "define_func"
    ),

    FUNCTION(
            "import",
            "define_func"
    ),

    MAIN(
            "return"
    );

    private final Set<String> blacklist;

    BlockType(String... allowed) {
        this.blacklist = new HashSet<>(List.of(allowed));
    }

    BlockType(Set<String> inherit, String... allowed) {
        var array = new String[inherit.size() + allowed.length];
        // TODO: Temporary solution, do it properly a bit later
        System.arraycopy(inherit.toArray(String[]::new), 0, array, 0, inherit.size());
        System.arraycopy(allowed, 0, array, inherit.size(), allowed.length);

        this.blacklist = new HashSet<>(List.of(array));
    }

    public boolean blocksInstruction(String instr) {
        return blacklist.contains(instr);
    }

}
