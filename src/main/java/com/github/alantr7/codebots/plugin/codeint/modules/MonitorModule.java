package com.github.alantr7.codebots.plugin.codeint.modules;

import com.github.alantr7.codebots.api.monitor.ColorPalette;
import com.github.alantr7.codebots.api.monitor.PresetColor;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.errors.Assertions;
import com.github.alantr7.codebots.language.runtime.errors.exceptions.ExecutionException;
import com.github.alantr7.codebots.language.runtime.modules.NativeModule;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.monitor.CraftMonitor;
import com.github.alantr7.codebots.plugin.bot.CraftCodeBot;
import com.github.alantr7.codebots.plugin.data.MonitorManager;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.TextDisplay;

public class MonitorModule extends NativeModule {

    public MonitorModule(Program program) {
        super(program);
        init();
    }

    private void init() {
        registerFunction("connect", args -> {
            Assertions.expectArguments(args, String.class);
            CraftCodeBot bot = (CraftCodeBot) program.getExtra("bot");
            CraftMonitor monitor = (CraftMonitor) CodeBotsPlugin.inst().getSingleton(MonitorManager.class).getMonitor((String) args[0]);

            if (monitor == null)
                throw new ExecutionException("Could not connect to monitor. Make sure the ID is correct.");

            if (monitor.getConnectedBot() != bot && monitor.getConnectedBot() != null && ((CraftCodeBot) monitor.getConnectedBot()).getMonitor().getId().equals(monitor.getId()))
                throw new ExecutionException("Could not connect to monitor. It is connect to another bot.");

            monitor.setConnectedBot(bot);
            return null;
        });

        registerFunction("print", args -> {
            Assertions.expectArguments(args, String.class);
            CraftCodeBot bot = (CraftCodeBot) program.getExtra("bot");
            CraftMonitor monitor = bot.getMonitor();

            if (monitor == null)
                throw new ExecutionException("Monitor is not connected. Make sure you connected this bot to a monitor by using `connect` command.");

            monitor.write(ChatColor.translateAlternateColorCodes('&', (String) args[0]));
            return null;
        });

        registerFunction("println", args -> {
            Assertions.expectArguments(args, String.class);
            CraftCodeBot bot = (CraftCodeBot) program.getExtra("bot");
            CraftMonitor monitor = bot.getMonitor();

            if (monitor == null)
                throw new ExecutionException("Monitor is not connected. Make sure you connected this bot to a monitor by using `connect` command.");

            monitor.write(ChatColor.translateAlternateColorCodes('&', (String) args[0]) + "\\n");
            return null;
        });

        registerFunction("setText", args -> {
            Assertions.expectArguments(args, String.class);
            CraftCodeBot bot = (CraftCodeBot) program.getExtra("bot");
            CraftMonitor monitor = bot.getMonitor();

            if (monitor == null)
                throw new ExecutionException("Monitor is not connected. Make sure you connected this bot to a monitor by using `connect` command.");

            monitor.setText(ChatColor.translateAlternateColorCodes('&', (String) args[0]));
            return null;
        });

        registerFunction("clear", args -> {
            CraftCodeBot bot = (CraftCodeBot) program.getExtra("bot");
            CraftMonitor monitor = bot.getMonitor();

            if (monitor == null)
                throw new ExecutionException("Monitor is not connected. Make sure you connected this bot to a monitor by using `connect` command.");

            monitor.clear();
            return null;
        });

        registerFunction("setBackgroundColor", args -> {
            Assertions.expectArguments(args, String.class);
            CraftCodeBot bot = (CraftCodeBot) program.getExtra("bot");
            CraftMonitor monitor = bot.getMonitor();

            if (monitor == null)
                throw new ExecutionException("Monitor is not connected. Make sure you connected this bot to a monitor by using `connect` command.");

            PresetColor color = ColorPalette.getColor((String) args[0]);
            if (color == null)
                throw new ExecutionException("Unknown color: " + args[0]);

            monitor.setBackgroundColor(color);
            return null;
        });

        registerFunction("setTextColor", args -> {
            Assertions.expectArguments(args, String.class);
            CraftCodeBot bot = (CraftCodeBot) program.getExtra("bot");
            CraftMonitor monitor = bot.getMonitor();

            if (monitor == null)
                throw new ExecutionException("Monitor is not connected. Make sure you connected this bot to a monitor by using `connect` command.");

            TextColor color = ColorPalette.getTextColor((String) args[0]);
            if (color == null)
                throw new ExecutionException("Unknown color: " + args[0]);

            monitor.setTextColor(color);
            return null;
        });

        registerFunction("setTextColorRGB", args -> {
            Assertions.expectArguments(args, Integer.class, Integer.class, Integer.class);
            CraftCodeBot bot = (CraftCodeBot) program.getExtra("bot");
            CraftMonitor monitor = bot.getMonitor();

            if (monitor == null)
                throw new ExecutionException("Monitor is not connected. Make sure you connected this bot to a monitor by using `connect` command.");

            int red = (int) args[0];
            Assertions.assertBool(red <= 255 && red >= 0, "Colors must be in range [0-255]!");

            int green = (int) args[1];
            Assertions.assertBool(green <= 255 && green >= 0, "Colors must be in range [0-255]!");

            int blue = (int) args[2];
            Assertions.assertBool(blue <= 255 && blue >= 0, "Colors must be in range [0-255]!");

            monitor.setTextColor(TextColor.color(red, green, blue));
            return null;
        });

        registerFunction("setTextAlignment", args -> {
            Assertions.expectArguments(args, String.class);
            String alignmentRaw = (String) args[0];
            TextDisplay.TextAlignment alignment = switch (alignmentRaw) {
                case "left" -> TextDisplay.TextAlignment.LEFT;
                case "center" -> TextDisplay.TextAlignment.CENTER;
                case "right" -> TextDisplay.TextAlignment.RIGHT;
                default -> null;
            };

            if (alignment == null) throw new ExecutionException("Invalid alignment option. Expected values were \"left\", \"center\" and \"right\".");

            CraftCodeBot bot = (CraftCodeBot) program.getExtra("bot");
            CraftMonitor monitor = bot.getMonitor();

            if (monitor == null)
                throw new ExecutionException("Monitor is not connected. Make sure you connected this bot to a monitor by using `connect` command.");

            monitor.setTextAlignment(alignment);
            return null;
        });
    }

}
