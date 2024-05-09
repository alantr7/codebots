package com.github.alantr7.codebots.plugin.data;

import com.github.alantr7.bukkitplugin.annotations.core.Inject;
import com.github.alantr7.bukkitplugin.annotations.core.Invoke;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.modules.FileModule;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.bot.BotRegistry;
import com.github.alantr7.codebots.plugin.bot.CraftCodeBot;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.UUID;

@Singleton
public class DataLoader {

    @Inject
    CodeBotsPlugin plugin;

    @Inject
    BotRegistry registry;

    @Invoke(Invoke.Schedule.AFTER_PLUGIN_ENABLE)
    public void load() {
        var botsDirectory = new File(plugin.getDataFolder(), "bots");
        botsDirectory.mkdirs();

        for (var directory : botsDirectory.listFiles()) {
            var data = YamlConfiguration.loadConfiguration(new File(directory, "bot.yml"));
            var botId = UUID.fromString(directory.getName());

            var entityId = UUID.fromString(data.getString("EntityId"));
            var programPath = data.getString("Program");

            var bot = new CraftCodeBot(botId, entityId);
            try {
                var program = Program.createFromSourceFile(new File(bot.getProgramsDirectory(), programPath));
                bot.setProgram(program);
            } catch (Exception e) {
                e.printStackTrace();
            }

            this.registry.registerBot(bot);
        }

        plugin.getLogger().info("Loaded " + registry.getBots().size() + " bots.");
    }

    public void save() {
        registry.getBots().forEach((id, bot) -> save(bot));
    }

    public void save(CodeBot bot) {
        var directory = new File(new File(plugin.getDataFolder(), "bots"), bot.getId().toString());
        directory.mkdirs();

        var botFile = new File(directory, "bot.yml");
        var data = new YamlConfiguration();
        data.set("Location", bot.getLocation());
        data.set("EntityId", bot.getEntityId().toString());
        if (bot.getProgram() != null) {
            data.set("Program", ((FileModule) bot.getProgram().getMainModule()).getFile().getName());
        }
        try {
            data.save(botFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        var programs = new File(directory, "programs");
        programs.mkdirs();
    }

}
