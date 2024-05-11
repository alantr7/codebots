package com.github.alantr7.codebots.plugin.codeint.modules;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.modules.NativeModule;
import com.github.alantr7.codebots.plugin.codeint.functions.MineFunction;
import com.github.alantr7.codebots.plugin.codeint.functions.MoveFunction;
import com.github.alantr7.codebots.plugin.codeint.functions.RotateFunction;

public class BotModule extends NativeModule {

    public BotModule(Program program) {
        super(program);
        init();
    }

    private void init() {
        getRootScope().setFunction("move", new MoveFunction(program));
        getRootScope().setFunction("rotateLeft", new RotateFunction(program, "rotateLeft"));
        getRootScope().setFunction("rotateRight", new RotateFunction(program, "rotateRight"));

        registerFunction("getDirection", a -> ((CodeBot) program.getExtra("bot")).getDirection().name().toLowerCase());

        registerFunction("getBlock", args -> {
            var bot = (CodeBot) program.getExtra("bot");
            var input = (String) args[0];

            var direction = input.equals("front") ? bot.getDirection().toVector() :
                    input.equals("back") ? bot.getDirection().toVector().multiply(-1) : Direction.toDirection(input).toVector();

            return bot.getLocation().add(direction).getBlock().getType().name().toLowerCase();
        });

        getRootScope().setFunction("mine", new MineFunction(program));
    }

}
