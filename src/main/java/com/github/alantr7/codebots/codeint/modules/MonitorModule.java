package com.github.alantr7.codebots.codeint.modules;

import com.github.alantr7.codebots.api.monitor.ColorPalette;
import com.github.alantr7.codebots.api.monitor.PresetColor;
import com.github.alantr7.codebots.cbslang.exceptions.ExecutionException;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.Data;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.DataType;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Context;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.ExternalFunction;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Module;
import com.github.alantr7.codebots.codeint.Assertions;
import com.github.alantr7.codebots.world.BlockLocation;
import com.github.alantr7.codebots.world.structure.CraftMonitor;
import com.github.alantr7.codebots.world.bot.CraftCodeBot;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.TextDisplay;

public class MonitorModule extends Module {

    public MonitorModule() {
        super("monitor");
    }


    @Override
    public void setup() {
        registerFunction("connect", new ExternalFunction(this, "connect", DataType.INT, DataType.STRING) {
            @Override
            public Data handle(Context context) throws ExecutionException {
                CraftCodeBot bot = (CraftCodeBot) context.getProgram().getExtra("bot");
                CraftMonitor monitor = new BlockLocation(bot.getLocation()).world.getMonitorById(context.getArguments()[0].getValueAs(DataType.STRING));

                if (monitor == null)
                    throw new ExecutionException("Could not connect to monitor. Make sure the ID is correct.");

                if (monitor.getConnectedBot() != bot && monitor.getConnectedBot() != null && ((CraftCodeBot) monitor.getConnectedBot()).getMonitor().getId().equals(monitor.getId()))
                    throw new ExecutionException("Could not connect to monitor. It is connect to another bot.");

                monitor.setConnectedBot(bot);
                return new Data(DataType.INT, 1);
            }
        });

        registerFunction("print", new ExternalFunction(this, "print", DataType.INT, DataType.STRING) {
            @Override
            public Data handle(Context context) throws ExecutionException {
                CraftCodeBot bot = (CraftCodeBot) context.getProgram().getExtra("bot");
                CraftMonitor monitor = bot.getMonitor();

                if (monitor == null)
                    throw new ExecutionException("Monitor is not connected. Make sure you connected this bot to a monitor by using `connect` command.");

                monitor.write(ChatColor.translateAlternateColorCodes('&', context.getArguments()[0].getValueAs(DataType.STRING)));
                return new Data(DataType.INT, 1);
            }
        });

        registerFunction("print_line", new ExternalFunction(this, "print_line", DataType.INT, DataType.STRING) {
            @Override
            public Data handle(Context context) throws ExecutionException {
                CraftCodeBot bot = (CraftCodeBot) context.getProgram().getExtra("bot");
                CraftMonitor monitor = bot.getMonitor();

                if (monitor == null)
                    throw new ExecutionException("Monitor is not connected. Make sure you connected this bot to a monitor by using `connect` command.");

                monitor.write(ChatColor.translateAlternateColorCodes('&', context.getArguments()[0].getValueAs(DataType.STRING) + "\n"));
                return new Data(DataType.INT, 1);
            }
        });

        registerFunction("set_text", new ExternalFunction(this, "set_text", DataType.INT, DataType.STRING) {
            @Override
            public Data handle(Context context) throws ExecutionException {
                CraftCodeBot bot = (CraftCodeBot) context.getProgram().getExtra("bot");
                CraftMonitor monitor = bot.getMonitor();

                if (monitor == null)
                    throw new ExecutionException("Monitor is not connected. Make sure you connected this bot to a monitor by using `connect` command.");

                monitor.setText(ChatColor.translateAlternateColorCodes('&', context.getArguments()[0].getValueAs(DataType.STRING)));
                return new Data(DataType.INT, 1);
            }
        });

        registerFunction("clear", new ExternalFunction(this, "clear", DataType.INT) {
            @Override
            public Data handle(Context context) throws ExecutionException {
                CraftCodeBot bot = (CraftCodeBot) context.getProgram().getExtra("bot");
                CraftMonitor monitor = bot.getMonitor();

                if (monitor == null)
                    throw new ExecutionException("Monitor is not connected. Make sure you connected this bot to a monitor by using `connect` command.");

                monitor.clear();
                return new Data(DataType.INT, 1);
            }
        });

        registerFunction("set_background_color", new ExternalFunction(this, "set_background_color", DataType.INT, DataType.STRING) {
            @Override
            public Data handle(Context context) throws ExecutionException {
                CraftCodeBot bot = (CraftCodeBot) context.getProgram().getExtra("bot");
                CraftMonitor monitor = bot.getMonitor();

                if (monitor == null)
                    throw new ExecutionException("Monitor is not connected. Make sure you connected this bot to a monitor by using `connect` command.");

                PresetColor color = ColorPalette.getColor(context.getArguments()[0].getValueAs(DataType.STRING));
                if (color == null)
                    throw new ExecutionException("Unknown color: " + context.getArguments()[0].getValueAs(DataType.STRING));

                monitor.setBackgroundColor(color);
                return new Data(DataType.INT, 1);
            }
        });

        registerFunction("set_text_color", new ExternalFunction(this, "set_text_color", DataType.INT, DataType.STRING) {
            @Override
            public Data handle(Context context) throws ExecutionException {
                CraftCodeBot bot = (CraftCodeBot) context.getProgram().getExtra("bot");
                CraftMonitor monitor = bot.getMonitor();

                if (monitor == null)
                    throw new ExecutionException("Monitor is not connected. Make sure you connected this bot to a monitor by using `connect` command.");

                TextColor color = ColorPalette.getTextColor(context.getArguments()[0].getValueAs(DataType.STRING));
                if (color == null)
                    throw new ExecutionException("Unknown color: " + context.getArguments()[0].getValueAs(DataType.STRING));

                monitor.setTextColor(color);
                return new Data(DataType.INT, 1);
            }
        });

        registerFunction("set_text_color_rgb", new ExternalFunction(this, "set_text_color_rgb", DataType.INT, DataType.INT, DataType.INT, DataType.INT) {
            @Override
            public Data handle(Context context) throws ExecutionException {
                CraftCodeBot bot = (CraftCodeBot) context.getProgram().getExtra("bot");
                CraftMonitor monitor = bot.getMonitor();

                if (monitor == null)
                    throw new ExecutionException("Monitor is not connected. Make sure you connected this bot to a monitor by using `connect` command.");

                TextColor color = ColorPalette.getTextColor(context.getArguments()[0].getValueAs(DataType.STRING));
                if (color == null)
                    throw new ExecutionException("Unknown color: " + context.getArguments()[0].getValueAs(DataType.STRING));

                int red = context.getArguments()[0].getValueAs(DataType.INT);
                Assertions.assertBool(red <= 255 && red >= 0, "Colors must be in range [0-255]!");

                int green = context.getArguments()[1].getValueAs(DataType.INT);
                Assertions.assertBool(green <= 255 && green >= 0, "Colors must be in range [0-255]!");

                int blue = context.getArguments()[2].getValueAs(DataType.INT);
                Assertions.assertBool(blue <= 255 && blue >= 0, "Colors must be in range [0-255]!");

                monitor.setTextColor(TextColor.color(red, green, blue));
                return new Data(DataType.INT, 1);
            }
        });

        registerFunction("set_text_alignment", new ExternalFunction(this, "set_text_alignment", DataType.INT, DataType.STRING) {
            @Override
            public Data handle(Context context) throws ExecutionException {
                String alignmentRaw = context.getArguments()[0].getValueAs(DataType.STRING);
                TextDisplay.TextAlignment alignment = switch (alignmentRaw) {
                    case "left" -> TextDisplay.TextAlignment.LEFT;
                    case "center" -> TextDisplay.TextAlignment.CENTER;
                    case "right" -> TextDisplay.TextAlignment.RIGHT;
                    default -> null;
                };

                if (alignment == null) throw new ExecutionException("Invalid alignment option. Expected values were \"left\", \"center\" and \"right\".");

                CraftCodeBot bot = (CraftCodeBot) context.getProgram().getExtra("bot");
                CraftMonitor monitor = bot.getMonitor();

                if (monitor == null)
                    throw new ExecutionException("Monitor is not connected. Make sure you connected this bot to a monitor by using `connect` command.");

                monitor.setTextAlignment(alignment);
                return new Data(DataType.INT, 1);
            }
        });

    }

}
