package com.github.alantr7.codebots.plugin;


import com.github.alantr7.bukkitplugin.BukkitPlugin;
import com.github.alantr7.bukkitplugin.annotations.generative.JavaPlugin;
import com.github.alantr7.bukkitplugin.annotations.relocate.Relocate;
import com.github.alantr7.bukkitplugin.annotations.relocate.Relocations;

@JavaPlugin(name = "CodeBots")
@Relocations(@Relocate(from = "com.github.alantr7.bukkitplugin", to = "com.github.alantr7.codebots.bpf"))
public class CodeBotsPlugin extends BukkitPlugin {

    @Override
    protected void onPluginEnable() {

    }

    @Override
    protected void onPluginDisable() {

    }

}
