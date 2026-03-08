package com.github.alantr7.codebots.plugin.codeint.modules;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.Data;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.DataType;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Context;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.ExternalFunction;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Module;
import com.github.alantr7.codebots.plugin.codeint.functions.MineFunction;
import com.github.alantr7.codebots.plugin.codeint.functions.MoveFunction;
import com.github.alantr7.codebots.plugin.codeint.functions.RotateFunction;
import org.bukkit.block.Container;
import org.objectweb.asm.Type;

public class BotModule extends Module {

    public BotModule() {
        super("bot");
    }

    @Override
    public void setup() {
        registerFunction("chat", new ExternalFunction(this, "chat", DataType.INT, DataType.STRING) {
            @Override
            public Data handle(Context context) {
                var bot = (CodeBot) context.getProgram().getExtra("bot");
                bot.chat(context.getArguments()[0].getValueAs(DataType.STRING));

                return new Data(DataType.INT, 0);
            }
        });
        registerFunction("setStatus", new ExternalFunction(this, "setStatus", DataType.INT, DataType.STRING, DataType.INT) {
            @Override
            public Data handle(Context context) {
                int statusDuration = context.getArguments()[1].getValueAs(DataType.INT);
                var bot = (CodeBot) context.getProgram().getExtra("bot");
                bot.setStatus(context.getArguments()[0].getValueAs(DataType.STRING), System.currentTimeMillis() + statusDuration * 1000L);

                return new Data(DataType.INT, 1);
            }
        });
        registerFunction("move", new MoveFunction(this));
        registerFunction("rotateLeft", new RotateFunction(this, "rotateLeft"));
        registerFunction("rotateRight", new RotateFunction(this, "rotateRight"));

        registerFunction("getDirection", new ExternalFunction(this, "getDirection", DataType.STRING) {
            @Override
            public Data handle(Context context) {
                return new Data(DataType.STRING, ((CodeBot) context.getProgram().getExtra("bot")).getDirection().name().toLowerCase());
            }
        });

        registerFunction("getBlock", new ExternalFunction(this, "getBlock", DataType.STRING, DataType.STRING) {
            @Override
            public Data handle(Context context) {
                var bot = (CodeBot) context.getProgram().getExtra("bot");
                var input = context.getArguments()[0].getValueAs(DataType.STRING);

                var direction = input.equals("forward") ? bot.getDirection().toVector() :
                  input.equals("back") ? bot.getDirection().toVector().multiply(-1) : Direction.toDirection(input).toVector();

                return new Data(DataType.STRING, bot.getLocation().add(direction).getBlock().getType().name().toLowerCase());
            }
        });
        registerFunction("mine", new MineFunction(this));


        // Inventory functions
        registerFunction("getSelectedSlot", new ExternalFunction(this, "getSelectedSlot", DataType.INT) {
            @Override
            public Data handle(Context context) {
                return new Data(DataType.INT, ((CodeBot) context.getProgram().getExtra("bot")).getSelectedSlot());
            }
        });
        registerFunction("selectSlot", new ExternalFunction(this, "selectSlot", DataType.INT, DataType.INT) {
            @Override
            public Data handle(Context context) {
                var bot = (CodeBot) context.getProgram().getExtra("bot");
                var slot = context.getArguments()[0].getValueAs(DataType.INT);
                bot.setSelectedSlot(slot);

                return new Data(DataType.INT, 1);
            }
        });
        registerFunction("getItem", new ExternalFunction(this, "getItem", DataType.INT, DataType.INT) {
            @Override
            public Data handle(Context context) {
                var bot = (CodeBot) context.getProgram().getExtra("bot");
                var slot = bot.getSelectedSlot();

                var inventory = bot.getInventory();
                var item = inventory.getItem(slot);
                return new Data(DataType.STRING, item == null ? "none" : item.getType().name().toLowerCase());
            }
        });


        // Dispense items
        registerFunction("depositItem", new ExternalFunction(this, "depositItem", DataType.INT, DataType.STRING) {
            @Override
            public Data handle(Context context) {
                var bot = (CodeBot) context.getProgram().getExtra("bot");
                var inventory = bot.getInventory();
                var slot = bot.getSelectedSlot();
                var item = inventory.getItem(slot);
                if (item == null) {
                    return new Data(DataType.INT, 0);
                }

                String directionArgument = context.getArguments()[0].getValueAs(DataType.STRING);
                var direction = directionArgument.equals("forward") ? bot.getDirection()
                  : directionArgument.equals("back") ? bot.getDirection().getRight().getRight()
                  : Direction.toDirection((directionArgument).toUpperCase());

                if (direction == null) {
                    return new Data(DataType.INT, 0);
                }

                var state = bot.getLocation().getWorld().getBlockState(bot.getLocation().clone().add(direction.toVector()));
                if (!(state instanceof Container container)) {
                    return new Data(DataType.INT, 0);
                }

                var result = container.getInventory().addItem(item);
                inventory.setItem(slot, result.isEmpty() ? null : result.get(0).clone());
                return new Data(DataType.INT, 1);
            }
        });
    }

}
