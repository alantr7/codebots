package com.github.alantr7.codebots.plugin.codeint.functions;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.language.runtime.BlockContext;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.functions.RuntimeNativeFunction;
import com.github.alantr7.codebots.plugin.bot.CraftCodeBot;
import org.bukkit.Bukkit;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public class RotateFunction extends RuntimeNativeFunction {

    private static final float a45 = (float) Math.PI / 4;

    public static final float ANGLE_NORTH = 0;

    public static final float ANGLE_EAST = (float) (Math.PI + Math.PI / 2);

    public static final float ANGLE_SOUTH = a45 + a45 + a45 + a45;

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

        else if (ticks == 10) {
            context.setFlag(BlockContext.FLAG_COMPLETED, true);
        }

        environment.setHalted(true);
        context.advance();
    }

    void handleRotation() {
        var bot = (CodeBot) environment.getProgram().getExtra("bot");
        var direction = this.getLabel().equals("rotateRight") ? bot.getDirection().getRight() : bot.getDirection().getLeft();
        var angle = switch (direction) {
            case NORTH -> ANGLE_NORTH;
            case WEST -> ANGLE_WEST;
            case EAST -> ANGLE_EAST;
            case SOUTH -> ANGLE_SOUTH;
            default -> 0;
        };

        var translationFloats = getTranslation(direction);
        var initialTranslation = bot.getEntity().getTransformation().getTranslation();
        var initialTransformation = bot.getEntity().getTransformation();

        var nextRotation = new AxisAngle4f(angle, 0, 1, 0);
        var nextTranslation = new Vector3f(
                translationFloats[0], initialTranslation.y, translationFloats[1]
        );

        var entity = bot.getEntity();

        entity.setInterpolationDelay(0);
        entity.setInterpolationDuration(20);

        entity.setTransformation(new Transformation(
                nextTranslation,
                nextRotation,
                initialTransformation.getScale(),
                new AxisAngle4f(initialTransformation.getRightRotation())
        ));
    }

    private float[] getTranslation(Direction direction) {
        return switch (direction) {
            case EAST -> new float[] {0.6f, 0f};
            case SOUTH -> new float[] {0.6f, 0.6f};
            case WEST -> new float[]{0f, 0.6f};
            default -> new float[] {0f, 0f};
        };
    }

}
