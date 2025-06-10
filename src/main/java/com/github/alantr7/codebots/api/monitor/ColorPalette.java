package com.github.alantr7.codebots.api.monitor;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class ColorPalette {

    private static final Map<String, PresetColor> colorsMap = new HashMap<>();

    public static final PresetColor BLACK = new PresetColor(
            TextColor.color(0, 0, 0),
            Material.BLACK_CONCRETE
    );

    public static final PresetColor DARK_BLUE = new PresetColor(
            TextColor.color(0, 0, 170),
            Material.BLUE_CONCRETE
    );

    public static final PresetColor DARK_GREEN = new PresetColor(
            TextColor.color(0, 170, 0),
            Material.GREEN_CONCRETE
    );

    public static final PresetColor DARK_AQUA = new PresetColor(
            TextColor.color(0, 170, 170),
            Material.CYAN_CONCRETE
    );

    public static final PresetColor DARK_RED = new PresetColor(
            TextColor.color(170, 0, 0),
            Material.RED_CONCRETE
    );

    public static final PresetColor DARK_PURPLE = new PresetColor(
            TextColor.color(170, 0, 170),
            Material.PURPLE_CONCRETE
    );

    public static final PresetColor GOLD = new PresetColor(
            TextColor.color(255, 170, 0),
            Material.YELLOW_CONCRETE
    );

    public static final PresetColor GRAY = new PresetColor(
            TextColor.color(170, 170, 170),
            Material.LIGHT_GRAY_CONCRETE
    );

    public static final PresetColor DARK_GRAY = new PresetColor(
            TextColor.color(85, 85, 85),
            Material.GRAY_CONCRETE
    );

    public static final PresetColor BLUE = new PresetColor(
            TextColor.color(85, 85, 255),
            Material.BLUE_TERRACOTTA
    );

    public static final PresetColor GREEN = new PresetColor(
            TextColor.color(85, 255, 85),
            Material.LIME_CONCRETE
    );

    public static final PresetColor AQUA = new PresetColor(
            TextColor.color(85, 255, 255),
            Material.LIGHT_BLUE_CONCRETE
    );

    public static final PresetColor RED = new PresetColor(
            TextColor.color(255, 85, 85),
            Material.PINK_TERRACOTTA
    );

    public static final PresetColor LIGHT_PURPLE = new PresetColor(
            TextColor.color(255, 85, 255),
            Material.PINK_CONCRETE
    );

    public static final PresetColor YELLOW = new PresetColor(
            TextColor.color(255, 255, 85),
            Material.YELLOW_TERRACOTTA
    );

    public static final PresetColor WHITE = new PresetColor(
            TextColor.color(255, 255, 255),
            Material.WHITE_CONCRETE
    );

    static {
        colorsMap.put("black", BLACK);
        colorsMap.put("dark_blue", DARK_BLUE);
        colorsMap.put("dark_green", DARK_GREEN);
        colorsMap.put("dark_aqua", DARK_AQUA);
        colorsMap.put("dark_red", DARK_RED);
        colorsMap.put("dark_purple", DARK_PURPLE);
        colorsMap.put("gold", GOLD);
        colorsMap.put("gray", GRAY);
        colorsMap.put("dark_gray", DARK_GRAY);
        colorsMap.put("blue", BLUE);
        colorsMap.put("green", GREEN);
        colorsMap.put("aqua", AQUA);
        colorsMap.put("red", RED);
        colorsMap.put("light_purple", LIGHT_PURPLE);
        colorsMap.put("yellow", YELLOW);
        colorsMap.put("white", WHITE);
    }

    public static PresetColor getColor(String name) {
        return colorsMap.get(name);
    }

    public static TextColor getTextColor(String name) {
        // Get color by hex value
        if (name.startsWith("#")) {
            if (name.length() != 4 && name.length() != 7)
                return null;

            return TextColor.fromCSSHexString(name);
        }

        PresetColor color = getColor(name);
        return color != null ? color.text() : null;
    }

}
