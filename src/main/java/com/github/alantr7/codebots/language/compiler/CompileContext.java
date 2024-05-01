package com.github.alantr7.codebots.language.compiler;

public class CompileContext {

    private int nextLoopId = 0;

    private int nextLoopEntryId = 0;

    public String currentLoop() {
        return "loop_" + nextLoopId;
    }

    public String currentLoopEntry() {
        return "loop_entry_" + nextLoopEntryId;
    }

    public String nextLoopEntryName() {
        return "loop_entry_" + ++nextLoopEntryId;
    }

    public String nextLoopName() {
        return "loop_" + ++nextLoopId;
    }

}
