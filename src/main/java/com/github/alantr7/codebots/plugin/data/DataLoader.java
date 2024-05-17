package com.github.alantr7.codebots.plugin.data;

import com.github.alantr7.bukkitplugin.annotations.core.Inject;
import com.github.alantr7.bukkitplugin.annotations.core.Invoke;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.api.bot.Directory;
import com.github.alantr7.codebots.api.bot.ProgramSource;
import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.player.PlayerData;
import com.github.alantr7.codebots.language.compiler.Compiler;
import com.github.alantr7.codebots.language.compiler.parser.error.ParserException;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.errors.exceptions.ParseException;
import com.github.alantr7.codebots.language.runtime.modules.FileModule;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.bot.CraftCodeBot;
import com.github.alantr7.codebots.plugin.codeint.modules.BotModule;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@Singleton
public class DataLoader {

    @Inject
    CodeBotsPlugin plugin;

    @Inject
    BotRegistry registry;

    @Inject
    PlayerRegistry players;

    @Inject
    ProgramRegistry programs;

    @Invoke(Invoke.Schedule.AFTER_PLUGIN_ENABLE)
    public void load() {
        var programsDirectory = new File(plugin.getDataFolder(), "programs");
        programsDirectory.mkdirs();

        for (var programFile : programsDirectory.listFiles()) {
            try {
                programs.registerProgram(loadProgram(Directory.SHARED_PROGRAMS, programFile));
            } catch (Exception e) {}
        }

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
        var programPath = data.getString("Program.Name");
        var programDirectory = data.getString("Program.Directory");
        int selectedSlot = data.getInt("SelectedSlot", 0);

        var bot = new CraftCodeBot(botId, entityId, interactionId);

        var ownerId = data.getString("OwnerId");
        if (ownerId != null) {
            bot.setOwnerId(UUID.fromString(ownerId));
        }

        bot.setSelectedSlot(selectedSlot);

        if (programPath != null && programDirectory != null) {
            try {
                var programDirectoryEnum = Directory.fromOrDefault(programDirectory.toUpperCase(), Directory.LOCAL_PROGRAMS);
                var program = Program.createFromSourceFile(new File(bot.getProgramsDirectory(), programPath));
                var program1 = new ProgramSource(programDirectoryEnum, programPath, new File(bot.getProgramsDirectory(), programPath), program.getCode());
                program.setExtra("bot", bot);

                var botModule = new BotModule(program);
                program.registerNativeModule("bot", botModule);

                bot.setProgram(program);
                bot.setProgramSource(program1);
                program.action(Program.Mode.FULL_EXEC); // TODO: Remove this. It's only a TEMPORARY solution!!

                bot.getInventory().updateProgramButton();
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    public ProgramSource loadProgram(Directory directory, File file) throws ParserException, IOException {
        var source = Files.readAllLines(file.toPath()).toArray(String[]::new);
        var inline = Compiler.compileModule(String.join("\n", source));

        var code = inline.split("\n");
        return new ProgramSource(directory, file.getName(), file, code);
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
        if (bot.getOwnerId() != null) {
            data.set("OwnerId", bot.getOwnerId().toString());
        } else {
            data.set("OwnerId", null);
        }
        if (bot.getProgram() != null) {
            data.set("Program.Name", bot.getProgramSource().getSource().getName());
            data.set("Program.Directory", bot.getProgramSource().getDirectory().name());
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
