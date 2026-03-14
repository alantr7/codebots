package com.github.alantr7.codebots.plugin;

import com.github.alantr7.bukkitplugin.BukkitPlugin;
import com.github.alantr7.bukkitplugin.annotations.generative.JavaPlugin;
import com.github.alantr7.bukkitplugin.annotations.relocate.Relocate;
import com.github.alantr7.bukkitplugin.annotations.relocate.Relocations;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.ModuleRepository;
import com.github.alantr7.codebots.plugin.codeint.modules.*;

@JavaPlugin(name = "CodeBots", version = "0.8.0")
@Relocations(@Relocate(from = "com.github.alantr7.bukkitplugin", to = "com.github.alantr7.codebots.bpf"))
public class CodeBotsPlugin extends BukkitPlugin {

    static CodeBotsPlugin instance;

    static ModuleRepository MODULE_REPOSITORY = new ModuleRepository();

    @Override
    protected void onPluginEnable() {
        instance = this;

        MODULE_REPOSITORY.registerModule(new LangModule());
        MODULE_REPOSITORY.registerModule(new BotModule());
        MODULE_REPOSITORY.registerModule(new MonitorModule());
        MODULE_REPOSITORY.registerModule(new RedstoneModule());
        MODULE_REPOSITORY.registerModule(new MemoryModule());
    }

    @Override
    protected void onPluginDisable() {

    }

    public ModuleRepository getModuleRepository() {
        return MODULE_REPOSITORY;
    }

    public static CodeBotsPlugin inst() {
        return instance;
    }

}
