package com.github.alantr7.codebots.item;

import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class BotsItem {

    public final String id;

    private final ItemStack internal;

    public static final BotsItem BOT = new BotsItem("bot", Material.DISPENSER, "Programmable Bot", List.of("Right-click on ground to place"));
    public static final BotsItem REDSTONE_TRANSMITTER = new BotsItem("redstone_transmitter", Material.COMPARATOR, "Redstone Transmitter", List.of("Right-click on ground to place"));
    public static final BotsItem MONITOR_2x1 = new BotsItem("monitor_2x1", Material.OBSERVER, "Monitor (2x1)", List.of("Right-click on ground to place"));
    public static final BotsItem MONITOR_3x2 = new BotsItem("monitor_3x2", Material.OBSERVER, "Monitor (3x2)", List.of("Right-click on ground to place"));
    public static final BotsItem MONITOR_4x3 = new BotsItem("monitor_4x3", Material.OBSERVER, "Monitor (4x3)", List.of("Right-click on ground to place"));

    public BotsItem(String id, Material type, String displayName, List<String> lore) {
        this.id = id;
        this.internal = new ItemStack(type);

        ItemMeta meta = internal.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + displayName);
        meta.setLore(lore.stream().map(line -> ChatColor.GRAY + line).toList());
        meta.getPersistentDataContainer().set(new NamespacedKey(CodeBotsPlugin.inst(), "item"), PersistentDataType.STRING, id);
        internal.setItemMeta(meta);
    }

    public ItemStack toItemStack() {
        return internal.clone();
    }

    public static BotsItem getById(String id) {
        return switch (id) {
            case "bot" -> BOT;
            case "redstone_transmitter" -> REDSTONE_TRANSMITTER;
            case "monitor_2x1" -> MONITOR_2x1;
            case "monitor_3x2" -> MONITOR_3x2;
            case "monitor_4x3" -> MONITOR_4x3;
            case null, default -> null;
        };
    }

    public static BotsItem getByItemStack(ItemStack stack) {
        if (!stack.hasItemMeta())
            return null;

        return getById(
          stack.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(CodeBotsPlugin.inst(), "item"), PersistentDataType.STRING)
        );
    }

}
