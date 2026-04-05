package com.github.alantr7.codebots.plugin.codeint.functions;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.Data;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.DataType;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Context;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.ExternalFunction;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Module;
import com.github.alantr7.codebots.plugin.config.Config;

public class RotateFunction extends ExternalFunction {

    private static final byte MEMORY_ROTATION_PROGRESS = 0;

    private static final float a45 = (float) Math.PI / 4;

    public static final float ANGLE_NORTH = 0;

    public static final float ANGLE_EAST = (float) (Math.PI + Math.PI / 2);

    public static final float ANGLE_SOUTH = (float) Math.PI;

    public static final float ANGLE_WEST = a45 + a45;

    public RotateFunction(Module module, String label) {
        super(module, label, DataType.INT);
    }

    @Override
    public void prepareContext(Context context) {
        context.getMemory()[MEMORY_ROTATION_PROGRESS] = new Data(DataType.INT, 0);
    }

    @Override
    public Data handle(Context context) {
        int ticks = context.getMemory()[MEMORY_ROTATION_PROGRESS].getValueAs(DataType.INT);
        if (ticks == 0) {
            handleRotation(context);
        }

        else if (ticks == Config.BOT_ROTATION_DURATION) {
            context.setRecall(false);
            return new Data(DataType.INT, 1);
        }

        context.getMemory()[MEMORY_ROTATION_PROGRESS].updateValue(DataType.INT, v -> v + 1);
        context.setRecall(true);
        return null;
    }

    void handleRotation(Context context) {
        var bot = (CodeBot) context.getProgram().getExtra("bot");
        var direction = this.getName().equals("rotateRight") ? bot.getDirection().getRight() : bot.getDirection().getLeft();

        bot.rotate(direction, true);
    }

}
