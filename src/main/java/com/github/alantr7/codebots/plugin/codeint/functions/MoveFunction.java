package com.github.alantr7.codebots.plugin.codeint.functions;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.language.runtime.BlockContext;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.errors.Assertions;
import com.github.alantr7.codebots.language.runtime.functions.FunctionCall;
import com.github.alantr7.codebots.language.runtime.functions.RuntimeNativeFunction;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.bot.CraftCodeBot;
import com.github.alantr7.codebots.plugin.config.Config;
import com.github.alantr7.codebots.plugin.data.BotRegistry;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

public class MoveFunction extends RuntimeNativeFunction {

    public MoveFunction(Program program) {
        super(program, "move", null);
    }

    @Override
    public boolean hasNext(BlockContext context) {
        return !context.getFlag(BlockContext.FLAG_COMPLETED);
    }

    @Override
    public void next(BlockContext context) {
        var call = environment.getCallStack().getLast();

        // If this is the first tick, then begin movement
        if (context.getLineIndex() == 0) {
            try {
                if (!beginMovement(call)) {
                    environment.setHalted(true);
                    return;
                }
            } catch (Exception e) {
                environment.interrupt(e);
                return;
            }
        } else if (context.getLineIndex() == Config.BOT_MOVEMENT_DURATION) {
            context.setFlag(BlockContext.FLAG_COMPLETED, true);
        }

        context.advance();
        environment.setHalted(true);
    }

    private boolean beginMovement(FunctionCall call) throws Exception {
        Assertions.assertEquals(call.getArguments().length, 1, "Expected 1 argument");
        Assertions.assertEquals(call.getArguments()[0] instanceof String, true, "Expected a string argument");

        var bot = (CodeBot) environment.getProgram().getExtra("bot");
        var arg = call.getArguments()[0];
        var direction = (arg.equals("forward")
                ? bot.getDirection()
                : arg.equals("back")
                ? bot.getDirection().getRight().getRight()
                : Direction.toDirection((String) call.getArguments()[0]));

        return bot.move(direction, true);
    }

}
