package com.github.alantr7.codebots.plugin.codeint.modules;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.DataType;
import com.github.alantr7.codebots.language.runtime.errors.Assertions;
import com.github.alantr7.codebots.language.runtime.modules.NativeModule;
import com.github.alantr7.codebots.plugin.codeint.functions.MineFunction;
import com.github.alantr7.codebots.plugin.codeint.functions.MoveFunction;
import com.github.alantr7.codebots.plugin.codeint.functions.RotateFunction;
import org.bukkit.block.Container;

public class BotModule extends NativeModule {

    public BotModule(Program program) {
        super(program);
        init();
    }

    private void init() {
        registerFunction("chat", arguments -> {
            Assertions.expectArguments(arguments, String.class);
            var bot = (CodeBot) program.getExtra("bot");
            bot.chat((String) arguments[0]);
            return null;
        });
        registerFunction("setStatus", arguments -> {
            Assertions.assertBool(arguments.length <= 2, "setStatus function expects either 1 or 2 arguments");
            Assertions.assertBool(arguments[0] instanceof String, "Status must be of type string!");
            int statusDuration;

            if (arguments.length > 1) {
                Assertions.expectArguments(arguments, String.class, Integer.class);
                statusDuration = (int) arguments[1];
            } else {
                statusDuration = 5;
            }

            var bot = (CodeBot) program.getExtra("bot");
            bot.setStatus((String) arguments[0], System.currentTimeMillis() + statusDuration * 1000L);
            return null;
        });

        getRootScope().setFunction("move", new MoveFunction(program));
        getRootScope().setFunction("rotateLeft", new RotateFunction(program, "rotateLeft"));
        getRootScope().setFunction("rotateRight", new RotateFunction(program, "rotateRight"));

        registerFunction("getDirection", a -> ((CodeBot) program.getExtra("bot")).getDirection().name().toLowerCase());

        registerFunction("getBlock", args -> {
            var bot = (CodeBot) program.getExtra("bot");
            var input = (String) args[0];

            var direction = input.equals("forward") ? bot.getDirection().toVector() :
                    input.equals("back") ? bot.getDirection().toVector().multiply(-1) : Direction.toDirection(input).toVector();

            return bot.getLocation().add(direction).getBlock().getType().name().toLowerCase();
        });

        getRootScope().setFunction("mine", new MineFunction(program));


        // Inventory functions
        registerFunction("getSelectedSlot", args -> ((CodeBot) program.getExtra("bot")).getSelectedSlot());
        registerFunction("selectSlot", args -> {
            var bot = (CodeBot) program.getExtra("bot");
            var slot = (int) args[0];
            bot.setSelectedSlot(slot);

            return null;
        });
        registerFunction("getItem", args -> {
            var bot = (CodeBot) program.getExtra("bot");
            var slot = bot.getSelectedSlot();

            var inventory = bot.getInventory();
            var item = inventory.getItem(slot);
            return item == null ? null : item.getType().name().toLowerCase();
        });


        // Dispense items
        registerFunction("depositItem", args -> {
            Assertions.assertEquals(args.length, 1, "depositItem only takes 1 argument.");
            Assertions.assertType(args[0], DataType.STRING, "Direction is not a valid string.");
            var bot = (CodeBot) program.getExtra("bot");
            var inventory = bot.getInventory();
            var slot = bot.getSelectedSlot();
            var item = inventory.getItem(slot);
            if (item == null) {
                return false;
            }
            var direction = args[0].equals("forward") ? bot.getDirection()
                    : args[0].equals("back") ? bot.getDirection().getRight().getRight()
                    : Direction.toDirection(((String) args[0]).toUpperCase());

            if (direction == null) {
                return false;
            }

            var state = bot.getLocation().getWorld().getBlockState(bot.getLocation().clone().add(direction.toVector()));
            if (!(state instanceof Container container)) {
                return false;
            }

            var result = container.getInventory().addItem(item);
            inventory.setItem(slot, result.isEmpty() ? null : result.get(0).clone());
            return true;
        });
    }

}
