package com.github.alantr7.codebots.plugin.codeint.functions;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.language.runtime.BlockContext;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.functions.RuntimeNativeFunction;
import com.github.alantr7.codebots.plugin.bot.BotRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Vector3f;

public class MoveFunction extends RuntimeNativeFunction {

    private boolean hasMoved = false;

    private int ticks = 0;

    private BlockDisplay entity;

    private final Vector direction;

    private Vector3f initialTranslation;

    public MoveFunction(Program program, String label) {
        super(program, label, null);
        var bot = (CodeBot) program.getExtra("bot");
        this.direction = (switch (label) {
            case "moveForward" -> bot.getDirection().toVector();
            case "moveBack" -> bot.getDirection().toVector().multiply(-1);
            case "moveLeft" -> bot.getDirection().getLeft().toVector();
            case "moveRight" -> bot.getDirection().getRight().toVector();
            default -> new Vector(0, 0, 0);
        });
    }

    @Override
    public boolean hasNext(BlockContext context) {
        return !hasMoved;
    }

    @Override
    public void next(BlockContext context) {
        var bot = BotRegistry.instance.getBots().entrySet().iterator().next().getValue();
        if (ticks == 0) {
            entity = bot.getEntity();
            beginMovement();
        } else if (ticks == 20) {
            completeMovement();
        }

        ticks++;
    }

    private void beginMovement() {
        var initialTransformation = entity.getTransformation();
        initialTranslation = initialTransformation.getTranslation();
        var nextTranslation = direction.toVector3f().add(initialTranslation);

        entity.setInterpolationDelay(0);
        entity.setInterpolationDuration(20);

        entity.setTransformation(new Transformation(
                nextTranslation,
                initialTransformation.getLeftRotation(),
                initialTransformation.getScale(),
                initialTransformation.getRightRotation()
        ));

        Bukkit.broadcastMessage("§eMovement started!");
    }

    private void completeMovement() {
        entity.setInterpolationDuration(0);
        entity.setTransformation(new Transformation(
                initialTranslation,
                entity.getTransformation().getLeftRotation(),
                entity.getTransformation().getScale(),
                entity.getTransformation().getRightRotation()
        ));
        entity.teleport(entity.getLocation().add(direction));
        hasMoved = true;
    }

}
