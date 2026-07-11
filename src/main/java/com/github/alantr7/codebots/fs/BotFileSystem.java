package com.github.alantr7.codebots.fs;

import com.github.alantr7.codebots.world.bot.CraftCodeBot;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BotFileSystem implements FileSystem {

    final CraftCodeBot bot;

    final Map<String, BotFile> files = new HashMap<>();

    public BotFileSystem(CraftCodeBot bot) {
        this.bot = bot;
    }

    @Override
    public BotFile createFile(String name) {
        BotFile file = new BotFile(bot.location.world.fsManager, name, new byte[0], System.currentTimeMillis());
        files.put(file.getName(), file);

        bot.setDirty(true);
        bot.location.getChunk().isUnsaved = true;
        return file;
    }

    @Nullable
    @Override
    public BotFile getFile(String name) {
        return files.get(name);
    }

    @Override
    public void deleteFile(String name) {
        files.remove(name);
    }

    @Override
    public Collection<BotFile> getFiles() {
        return files.values();
    }

}
