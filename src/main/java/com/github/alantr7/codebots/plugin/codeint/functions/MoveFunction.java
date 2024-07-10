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
                if (!beginMovement(context, call)) {
                    environment.setHalted(true);
                    return;
                }
            } catch (Exception e) {
                environment.interrupt(e);
                return;
            }
        } else if (context.getLineIndex() == Config.BOT_MOVEMENT_DURATION) {
            completeMovement(context, call);
        }

        context.advance();
        environment.setHalted(true);
    }

    private boolean beginMovement(BlockContext context, FunctionCall call) throws Exception {
        Assertions.assertEquals(call.getArguments().length, 1, "Expected 1 argument");
        Assertions.assertEquals(call.getArguments()[0] instanceof String, true, "Expected a string argument");

        var bot = (CodeBot) environment.getProgram().getExtra("bot");
        var entity = bot.getEntity();
        var arg = call.getArguments()[0];
        var direction = (arg.equals("forward")
                ? bot.getDirection()
                : arg.equals("back")
                ? bot.getDirection().getRight().getRight()
                : Direction.toDirection((String) call.getArguments()[0])).toVector();

        if (!bot.getLocation().add(direction).getBlock().getType().isAir()) {
            return false;
        }

        var initialTransformation = entity.getTransformation();
        var initialTranslation = initialTransformation.getTranslation();

        var nextTranslation = direction.toVector3f().add(initialTranslation);

        entity.setInterpolationDelay(0);
        entity.setInterpolationDuration(Config.BOT_MOVEMENT_DURATION * 2);

        entity.setTransformation(new Transformation(
                nextTranslation,
                initialTransformation.getLeftRotation(),
                initialTransformation.getScale(),
                initialTransformation.getRightRotation()
        ));

        context.setExtra("initialTranslation", initialTranslation);
        CodeBotsPlugin.inst().getSingleton(BotRegistry.class).updateBotLocation((CraftCodeBot) bot);

        return true;
    }

    private void completeMovement(BlockContext context, FunctionCall call) {
        var bot = (CodeBot) environment.getProgram().getExtra("bot");
        var entity = bot.getEntity();
        var initialTranslation = (Vector3f) context.getExtra("initialTranslation");
        var arg = call.getArguments()[0];
        var direction = (arg.equals("forward")
                ? bot.getDirection()
                : arg.equals("back")
                ? bot.getDirection().getRight().getRight()
                : Direction.toDirection((String) call.getArguments()[0])).toVector();
        entity.setInterpolationDuration(0);
        entity.setTransformation(new Transformation(
                initialTranslation,
                entity.getTransformation().getLeftRotation(),
                entity.getTransformation().getScale(),
                entity.getTransformation().getRightRotation()
        ));
        bot.setLocation(entity.getLocation().add(direction));
        context.setFlag(BlockContext.FLAG_COMPLETED, true);
    }

}
