package com.github.alantr7.codebots.plugin.codeint.modules;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.modules.NativeModule;
import com.github.alantr7.codebots.plugin.codeint.functions.MoveFunction;
import com.github.alantr7.codebots.plugin.codeint.functions.RotateFunction;

public class BotModule extends NativeModule {

    public BotModule(Program program) {
        super(program);
        init();
    }

    private void init() {
        getRootScope().setFunction("moveForward", new MoveFunction(program, "moveForward"));
        getRootScope().setFunction("moveRight", new MoveFunction(program, "moveRight"));
        getRootScope().setFunction("moveBack", new MoveFunction(program, "moveBack"));
        getRootScope().setFunction("moveLeft", new MoveFunction(program, "moveLeft"));

        getRootScope().setFunction("rotateRight", new RotateFunction(program, "rotateRight"));

        registerFunction("getBlock", args -> {
            var bot = (CodeBot) program.getExtra("bot");
            var input = (String) args[0];

            var direction = input.equals("front") ? bot.getDirection().toVector() :
                    input.equals("back") ? bot.getDirection().toVector().multiply(-1) : Direction.toDirection(input).toVector();

            return bot.getLocation().add(direction).getBlock().getType().name().toLowerCase();
        });
    }

}
