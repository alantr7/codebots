package com.github.alantr7.codebots.plugin.codeint.functions;

import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.language.runtime.BlockContext;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.functions.RuntimeNativeFunction;
import com.github.alantr7.codebots.plugin.bot.CraftCodeBot;
import org.bukkit.Bukkit;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public class RotateFunction extends RuntimeNativeFunction {

    private boolean hasRotated = false;

    private Transformation initialTransformation;

    private Vector3f initialTranslation;

    private int ticks = 0;

    private final float a45 = (float) Math.PI / 4;

    private final CraftCodeBot bot;

    private Direction currentDirection;

    private Direction nextDirection;

    public RotateFunction(Program program, String label) {
        super(program, label, null);
        this.bot = (CraftCodeBot) program.getExtra("bot");
        this.currentDirection = bot.getDirection();
        this.nextDirection = switch (label) {
            case "rotateRight" -> currentDirection.getRight();
            default -> currentDirection.getLeft();
        };
    }

    @Override
    public boolean hasNext(BlockContext context) {
        return !hasRotated;
    }

    @Override
    public void next(BlockContext context) {
        var entity = bot.getEntity();
        if (ticks == 0) {
            this.initialTransformation = bot.getEntity().getTransformation();
            this.initialTranslation = initialTransformation.getTranslation();
            bot.setDirection(nextDirection);

            // Begin first half of rotation
//            var currentRotation = new AxisAngle4f(initialTransformation.getLeftRotation());
            var nextRotation = new AxisAngle4f(a45, 0, 1, 0);
            var nextTranslation = new Vector3f(
                    (float) (0.1f),
                    initialTranslation.y,
                    (float) (0.5f)
            );

            entity.setInterpolationDelay(0);
            entity.setInterpolationDuration(20);

            entity.setTransformation(new Transformation(
                    nextTranslation,
                    nextRotation,
                    initialTransformation.getScale(),
                    new AxisAngle4f(initialTransformation.getRightRotation())
            ));

            Bukkit.broadcastMessage("§eRotation started!");
        } else if (ticks == 10) {
            // Begin second half of rotation
            var nextRotation = new AxisAngle4f(a45 + a45, 0, 1, 0);
            var nextTranslation = new Vector3f(
                    0.2f,
                    initialTranslation.y,
                    0.8f
            );

            entity.setInterpolationDelay(0);
            entity.setInterpolationDuration(20);

            entity.setTransformation(new Transformation(
                    nextTranslation,
                    nextRotation,
                    initialTransformation.getScale(),
                    new AxisAngle4f(initialTransformation.getRightRotation())
            ));

            Bukkit.broadcastMessage("§eRotation started!");
        } else if (ticks == 20) {
            hasRotated = true;
        }

        ticks++;
    }

    public Transformation getHalfwayTransformation() {
        AxisAngle4f nextRotation;
        Vector3f nextTranslation;

        switch (nextDirection) {
            case NORTH -> {
                nextRotation = new AxisAngle4f(a45, 0, 1, 0);
                nextTranslation = new Vector3f(0.1f, initialTranslation.y, 0.5f);
            }
        };

        return null;
    }

    public Transformation getTargetTransformation() {
        return null;
    }

}
