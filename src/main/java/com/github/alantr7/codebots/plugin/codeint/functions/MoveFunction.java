package com.github.alantr7.codebots.plugin.codeint.functions;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.language.runtime.BlockContext;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.functions.FunctionCall;
import com.github.alantr7.codebots.language.runtime.functions.RuntimeNativeFunction;
import com.github.alantr7.codebots.plugin.bot.BotRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
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
            beginMovement(context, call);
        }
        else if (context.getLineIndex() == 10) {
            completeMovement(context, call);
        }

        context.advance();
    }

    private void beginMovement(BlockContext context, FunctionCall call) {
        var bot = (CodeBot) environment.getProgram().getExtra("bot");
        var entity = bot.getEntity();
        var initialTransformation = entity.getTransformation();
        var initialTranslation = initialTransformation.getTranslation();
        var direction = Direction.toDirection((String) call.getArguments()[0]).toVector();
        var nextTranslation = direction.toVector3f().add(initialTranslation);

        entity.setInterpolationDelay(0);
        entity.setInterpolationDuration(20);

        entity.setTransformation(new Transformation(
                nextTranslation,
                initialTransformation.getLeftRotation(),
                initialTransformation.getScale(),
                initialTransformation.getRightRotation()
        ));

        context.setExtra("initialTranslation", initialTranslation);
        Bukkit.broadcastMessage("§eMovement started!");
    }

    private void completeMovement(BlockContext context, FunctionCall call) {
        var bot = (CodeBot) environment.getProgram().getExtra("bot");
        var entity = bot.getEntity();
        var initialTranslation = (Vector3f) context.getExtra("initialTranslation");
        var direction = Direction.toDirection((String) call.getArguments()[0]).toVector();
        entity.setInterpolationDuration(0);
        entity.setTransformation(new Transformation(
                initialTranslation,
                entity.getTransformation().getLeftRotation(),
                entity.getTransformation().getScale(),
                entity.getTransformation().getRightRotation()
        ));
        entity.teleport(entity.getLocation().add(direction));

        context.setFlag(BlockContext.FLAG_COMPLETED, true);
    }

}
