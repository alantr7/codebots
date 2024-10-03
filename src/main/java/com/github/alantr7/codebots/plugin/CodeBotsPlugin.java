package com.github.alantr7.codebots.plugin;

import com.github.alantr7.bukkitplugin.BukkitPlugin;
import com.github.alantr7.bukkitplugin.annotations.generative.JavaPlugin;
import com.github.alantr7.bukkitplugin.annotations.relocate.Relocate;
import com.github.alantr7.bukkitplugin.annotations.relocate.Relocations;

@JavaPlugin(name = "CodeBots", version = "0.3.1")
@Relocations(@Relocate(from = "com.github.alantr7.bukkitplugin", to = "com.github.alantr7.codebots.bpf"))
public class CodeBotsPlugin extends BukkitPlugin {

    static CodeBotsPlugin instance;

    @Override
    protected void onPluginEnable() {
        instance = this;
    }

    @Override
    protected void onPluginDisable() {

    }

    public static CodeBotsPlugin inst() {
        return instance;
    }

}
