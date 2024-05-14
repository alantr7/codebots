package com.github.alantr7.codebots.plugin.data;

import com.github.alantr7.bukkitplugin.annotations.core.Inject;
import com.github.alantr7.bukkitplugin.annotations.core.Invoke;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.player.PlayerData;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.modules.FileModule;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.bot.CraftCodeBot;
import com.github.alantr7.codebots.plugin.codeint.modules.BotModule;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.UUID;

@Singleton
public class DataLoader {

    @Inject
    CodeBotsPlugin plugin;

    @Inject
    BotRegistry registry;

    @Inject
    PlayerRegistry players;

    @Invoke(Invoke.Schedule.AFTER_PLUGIN_ENABLE)
    public void load() {
        var botsDirectory = new File(plugin.getDataFolder(), "bots");
        botsDirectory.mkdirs();

        for (var directory : botsDirectory.listFiles()) {
            loadBot(directory);
        }

        plugin.getLogger().info("Loaded " + registry.getBots().size() + " bots.");

        // Load players
        for (var player : Bukkit.getOnlinePlayers()) {
            players.registerPlayer(new PlayerData(player.getUniqueId()));
        }
    }

    private void loadBot(File directory) {
        var data = YamlConfiguration.loadConfiguration(new File(directory, "bot.yml"));
        var botId = UUID.fromString(directory.getName());

        var entityId = UUID.fromString(data.getString("EntityId"));
        var interactionId = UUID.fromString(data.getString("InteractionId"));
        var programPath = data.getString("Program");
        int selectedSlot = data.getInt("SelectedSlot", 0);

        var bot = new CraftCodeBot(botId, entityId, interactionId);
        try {
            var program = Program.createFromSourceFile(new File(bot.getProgramsDirectory(), programPath));
            program.setExtra("bot", bot);

            var botModule = new BotModule(program);
            program.registerNativeModule("bot", botModule);

            bot.setProgram(program);
            program.action(Program.Mode.FULL_EXEC); // TODO: Remove this. It's only a TEMPORARY solution!!
        } catch (Exception e) {
            e.printStackTrace();
        }

        var inventoryFile = new File(directory, "inventory.yml");
        if (inventoryFile.exists()) {
            var inventoryData = YamlConfiguration.loadConfiguration(inventoryFile);
            for (int i = 0; i < 7; i++) {
                bot.getInventory().setItem(i, inventoryData.getItemStack("Slot" + i));
            }
        }

        this.registry.registerBot(bot);
        this.registry.updateBotLocation(bot);
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
        data.set("InteractionId", bot.getInteractionId().toString());
        if (bot.getProgram() != null) {
            data.set("Program", ((FileModule) bot.getProgram().getMainModule()).getFile().getName());
        } else {
            data.set("Program", null);
        }
        data.set("SelectedSlot", bot.getSelectedSlot());
        try {
            data.save(botFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        var programs = new File(directory, "programs");
        programs.mkdirs();
    }

    public void saveInventory(CodeBot bot) {
        var file = new File(bot.getDirectory(), "inventory.yml");
        var data = new YamlConfiguration();

        var inventory = bot.getInventory();
        for (int i = 0; i < 7; i++) {
            var item = inventory.getItem(i);
            data.set("Slot" + i, item);
        }

        try {
            data.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
