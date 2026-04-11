package com.github.alantr7.codebots.plugin;

import com.github.alantr7.bukkitplugin.BukkitPlugin;
import com.github.alantr7.bukkitplugin.annotations.generative.JavaPlugin;
import com.github.alantr7.bukkitplugin.annotations.relocate.Relocate;
import com.github.alantr7.bukkitplugin.annotations.relocate.Relocations;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.ModuleRepository;
import com.github.alantr7.codebots.plugin.codeint.modules.*;
import com.github.alantr7.codebots.plugin.data.ProgramRegistry;
import com.github.alantr7.codebots.plugin.editor.CodeEditorClient;
import com.github.alantr7.codebots.world.BotsWorldManager;
import org.bstats.bukkit.Metrics;

@JavaPlugin(name = "CodeBots", version = "1.0.0")
@Relocations(@Relocate(from = "com.github.alantr7.bukkitplugin", to = "com.github.alantr7.codebots.bpf"))
public class CodeBotsPlugin extends BukkitPlugin {

    static CodeBotsPlugin instance;

    static ModuleRepository MODULE_REPOSITORY = new ModuleRepository();

    private Metrics metrics;

    @Override
    protected void onPluginEnable() {
        instance = this;

        MODULE_REPOSITORY.registerModule(new LangModule());
        MODULE_REPOSITORY.registerModule(new BotModule());
        MODULE_REPOSITORY.registerModule(new MonitorModule());
        MODULE_REPOSITORY.registerModule(new RedstoneModule());
        MODULE_REPOSITORY.registerModule(new MemoryModule());

        metrics = new Metrics(this, 28911);
    }

    @Override
    protected void onPluginDisable() {

    }

    public static CodeBotsPlugin inst() {
        return instance;
    }

    public CodeEditorClient getEditorClient() {
        return getSingleton(CodeEditorClient.class);
    }

    public ProgramRegistry getProgramRegistry() {
        return getSingleton(ProgramRegistry.class);
    }

    public ModuleRepository getModuleRepository() {
        return MODULE_REPOSITORY;
    }

    public BotsWorldManager getWorldManager() {
        return getSingleton(BotsWorldManager.class);
    }

}
