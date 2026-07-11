package com.github.alantr7.codebots.integration.torus.fs;

import com.github.alantr7.codebots.CodeBotsPlugin;
import com.github.alantr7.codebots.fs.BotFile;
import com.github.alantr7.codebots.fs.FileSystem;
import com.github.alantr7.codebots.integration.torus.machine.ComputerInstance;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ComputerFileSystem implements FileSystem {

    protected ComputerInstance computer;

    protected Map<String, BotFile> files = new LinkedHashMap<>();

    public ComputerFileSystem(ComputerInstance computer) {
        this.computer = computer;
    }

    @Override
    public BotFile createFile(String name) {
        BotFile file = new BotFile(CodeBotsPlugin.inst().getWorldManager().getWorld(computer.location.world.getBukkit()).fsManager, name, new byte[0], System.currentTimeMillis());
        files.put(file.getName(), file);

        computer.isDirty = true;
        computer.location.getChunk().isUnsaved = true;
        return file;
    }

    @Override
    public BotFile getFile(String name) {
        return files.get(name);
    }

    @Override
    public void deleteFile(String name) {
        files.remove(name);
        computer.isDirty = true;
        computer.location.getChunk().isUnsaved = true;
    }

    @Override
    public Collection<BotFile> getFiles() {
        return files.values();
    }

}
