package com.github.alantr7.codebots.plugin.codeint.functions;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.language.runtime.BlockContext;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.functions.RuntimeNativeFunction;
import com.github.alantr7.codebots.plugin.config.Config;

public class RotateFunction extends RuntimeNativeFunction {

    private static final float a45 = (float) Math.PI / 4;

    public static final float ANGLE_NORTH = 0;

    public static final float ANGLE_EAST = (float) (Math.PI + Math.PI / 2);

    public static final float ANGLE_SOUTH = (float) Math.PI;

    public static final float ANGLE_WEST = a45 + a45;

    public RotateFunction(Program program, String label) {
        super(program, label, null);
    }

    @Override
    public boolean hasNext(BlockContext context) {
        return !context.getFlag(BlockContext.FLAG_COMPLETED);
    }

    @Override
    public void next(BlockContext context) {
        int ticks = context.getLineIndex();
        if (ticks == 0) {
            handleRotation();
        }

        else if (ticks == Config.BOT_ROTATION_DURATION) {
            context.setFlag(BlockContext.FLAG_COMPLETED, true);
        }

        environment.setHalted(true);
        context.advance();
    }

    void handleRotation() {
        var bot = (CodeBot) environment.getProgram().getExtra("bot");
        var direction = this.getLabel().equals("rotateRight") ? bot.getDirection().getRight() : bot.getDirection().getLeft();

        bot.setDirection(direction, true);
    }

}
