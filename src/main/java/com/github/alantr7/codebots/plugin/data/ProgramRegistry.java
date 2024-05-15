package com.github.alantr7.codebots.plugin.data;

import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.api.bot.ProgramSource;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class ProgramRegistry {

    private final Map<String, ProgramSource> sharedPrograms = new HashMap<>();

    public void registerProgram(ProgramSource program) {
        sharedPrograms.put(program.getName(), program);
    }

    public ProgramSource getProgram(String name) {
        return sharedPrograms.get(name);
    }

}
