package com.github.alantr7.codebots.plugin.monitor;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.api.monitor.Monitor;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.data.MonitorManager;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;

import java.security.SecureRandom;

public class MonitorFactory {

    public static Monitor createMonitor(Location blockLocation, Direction facingDirection, Monitor.Size size) {
        return createMonitor(null, blockLocation, facingDirection, size);
    }

    public static Monitor createMonitor(String id, Location blockLocation, Direction facingDirection, Monitor.Size size) {
        Location backgroundBlock = blockLocation.clone();
        for (int i = 0; i < size.getWidth(); i++) {
            Location backgroundBlockHorizontalOrigin = backgroundBlock.clone();
            for (int j = 0; j < size.getHeight(); j++) {
                backgroundBlockHorizontalOrigin.getBlock().setType(Material.BARRIER);
                backgroundBlockHorizontalOrigin.add(0, 1, 0);
            }

            backgroundBlock.add(facingDirection.getLeft().toVector());
        }

        Location location = blockLocation.clone().add(facingDirection.toVector());
        BlockDisplay blockEntity = (BlockDisplay) location.getWorld().spawnEntity(location, EntityType.BLOCK_DISPLAY);
        BlockDisplay screenEntity = (BlockDisplay) location.getWorld().spawnEntity(location, EntityType.BLOCK_DISPLAY);
        TextDisplay textEntity = (TextDisplay) location.getWorld().spawnEntity(location, EntityType.TEXT_DISPLAY);
        CraftMonitor monitor = new CraftMonitor(
                id != null ? id : NanoIdUtils.randomNanoId(new SecureRandom(), NanoIdUtils.DEFAULT_ALPHABET, 8),
                blockLocation,
                facingDirection,
                blockEntity,
                screenEntity,
                textEntity,
                size
        );

        CodeBotsPlugin.inst().getSingleton(MonitorManager.class).registerMonitor(monitor);

        location.setDirection(facingDirection.toVector());
        textEntity.setLineWidth(monitor.getMaxLineWidth());
        Transformation transformation = textEntity.getTransformation();
        transformation.getTranslation().z = -0.98f;
        transformation.getTranslation().x = CraftMonitor.TEXT_DISPLAY_HORIZONTAL_OFFSETS[monitor.getWidth() - 1];
        transformation.getTranslation().y = -0.1f + CraftMonitor.TEXT_DISPLAY_VERTICAL_OFFSETS[monitor.getHeight() - 1];
        transformation.getScale().set(0.75);
        textEntity.setTransformation(transformation);
        textEntity.setAlignment(TextDisplay.TextAlignment.LEFT);
        textEntity.setBackgroundColor(Color.fromARGB(0));

        // TODO: Fix this offsetX, offsetZ mess

        // Get right direction
        float modX = 0; float modZ = 0;

        if (facingDirection.getModZ() == -1) {
            modX = 0.5f - 0.08f + -monitor.getWidth() / 2f;
            modZ = -0.5f;
        } else if (facingDirection.getModZ() == 1) {
            modX = -0.5f + 0.08f + monitor.getWidth() / 2f;
            modZ = 0.5f;
        }

        if (facingDirection.getModX() == 1) {
            modX = 0.5f;
            modZ = 0.5f - 0.08f + -monitor.getWidth() / 2f;
        }
        else if (facingDirection.getModX() == -1) {
            modX = -0.5f;
            modZ = -0.5f + 0.08f + monitor.getWidth() / 2f;
        }

        float xOffset = 0.5f + modX;
        float zOffset = 0.5f + modZ;

        location.add(xOffset, 0, zOffset);
        textEntity.teleport(location);


        Transformation blockTransform = blockEntity.getTransformation();
        blockTransform.getScale().x = monitor.getWidth();
        blockTransform.getScale().y = monitor.getHeight();
        blockEntity.setTransformation(blockTransform);


        Transformation screenTransform = screenEntity.getTransformation();
        screenTransform.getScale().x = monitor.getWidth() - 0.16f;
        screenTransform.getScale().z = 0.01f;
        screenTransform.getScale().y = monitor.getHeight() - 0.16f;
        screenEntity.setTransformation(screenTransform);


        xOffset = 0;
        zOffset = 0;
        if (facingDirection.getModZ() == -1) {
            xOffset = 0.5f;
            zOffset = 0.5f;
        }
        else if (facingDirection.getModZ() == 1) {
            xOffset = -0.5f;
            zOffset = -0.5f;
        }

        if (facingDirection.getModX() == -1) {
            xOffset = 0.5f;
            zOffset = -0.5f;
        }
        else if (facingDirection.getModX() == 1) {
            xOffset = -0.5f;
            zOffset = 0.5f;
        }

        Location blockEntityLocation = blockLocation.clone().add(.5, 0, .5).add(xOffset, 0, zOffset);
        blockEntity.setBlock(Material.LIGHT_GRAY_CONCRETE.createBlockData());
        blockEntityLocation.setDirection(facingDirection.toVector());
        blockEntity.teleport(blockEntityLocation);

        xOffset = 0;
        zOffset = 0;
        if (facingDirection.getModZ() == -1) {
            xOffset = 0.5f - 0.08f;
            zOffset = -0.5f;
        }
        else if (facingDirection.getModZ() == 1) {
            xOffset = -0.5f + 0.08f;
            zOffset = 0.5f;
        }

        if (facingDirection.getModX() == -1) {
            xOffset = -0.5f;
            zOffset = -0.5f + 0.08f;
        }
        else if (facingDirection.getModX() == 1) {
            xOffset = 0.5f;
            zOffset = 0.5f - 0.08f;
        }
        Location screenLocation = blockLocation.clone().add(0.5, 0, 0.5).add(xOffset, 0.08f, zOffset);
        screenEntity.setBlock(Material.BLACK_CONCRETE.createBlockData());
        screenLocation.setDirection(facingDirection.toVector());
        screenEntity.teleport(screenLocation);

        monitor.showDefaultText();
        return monitor;
    }

}
