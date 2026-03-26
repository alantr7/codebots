package com.github.alantr7.codebots.plugin.bot;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BotFileSystem {

    private final Map<String, BotFile> files = new HashMap<>();

    public BotFile createFile(String name) {
        BotFile file = new BotFile(name, new byte[0]);
        files.put(file.getName(), file);
        return file;
    }

    @Nullable
    public BotFile getFile(String name) {
        return files.get(name);
    }

    public Collection<BotFile> getFiles() {
        return files.values();
    }

}
