package com.github.alantr7.codebots.fs;

import com.github.alantr7.codebots.world.bot.CraftCodeBot;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BotFileSystem {

    final CraftCodeBot bot;

    final Map<String, BotFile> files = new HashMap<>();

    public BotFileSystem(CraftCodeBot bot) {
        this.bot = bot;
    }

    public BotFile createFile(String name) {
        BotFile file = new BotFile(name, new byte[0], System.currentTimeMillis());
        files.put(file.getName(), file);

        bot.setDirty(true);
        bot.location.getChunk().isUnsaved = true;
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
