package com.github.alantr7.codebots.plugin.codeint.functions;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.Data;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.DataType;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Context;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.ExternalFunction;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Module;
import com.github.alantr7.codebots.plugin.config.Config;

public class MoveFunction extends ExternalFunction {

    private static final byte MEMORY_MINE_PROGRESS = 0;

    public MoveFunction(Module module) {
        super(module, "move", DataType.INT, DataType.STRING);
    }

    @Override
    public void prepareContext(Context context) {
        context.getMemory()[MEMORY_MINE_PROGRESS] = new Data(DataType.INT, 0);
    }

    private boolean beginMovement(Context context) throws Exception {
        var bot = (CodeBot) context.getProgram().getExtra("bot");
        var arg = context.getArguments()[0].getValueAs(DataType.STRING);
        var direction = (arg.equals("forward")
                ? bot.getDirection()
                : arg.equals("back")
                ? bot.getDirection().getRight().getRight()
                : Direction.toDirection(arg));

        return bot.move(direction, true);
    }

    @Override
    public Data handle(Context context) {
        // If this is the first tick, then begin movement
        if (context.getMemory()[MEMORY_MINE_PROGRESS].getValueAs(DataType.INT) == 0) {
            try {
                if (!beginMovement(context)) {
                    context.setRecall(true);
                    return null;
                }
            } catch (Exception e) {
                context.getProgram().interrupt(e);
                return null;
            }
        } else if (context.getMemory()[MEMORY_MINE_PROGRESS].getValueAs(DataType.INT) == Config.BOT_MOVEMENT_DURATION) {
            context.setRecall(false);
            return new Data(DataType.INT, 1);
        }

        context.setRecall(true);
        return null;
    }

}
