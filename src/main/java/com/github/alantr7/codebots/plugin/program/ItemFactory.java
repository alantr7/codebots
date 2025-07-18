package com.github.alantr7.codebots.plugin.program;

import com.github.alantr7.codebots.api.bot.BotBuilder;
import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.monitor.Monitor;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.UUID;
import java.util.function.Consumer;

public class ItemFactory {

    public static ItemStack createProgramItem(String name, String[] code) {
        return createItem(Material.PAPER, meta -> {
            meta.setDisplayName("§eProgram: §f" + name);
            meta.getPersistentDataContainer().set(new NamespacedKey(CodeBotsPlugin.inst(), "code"), PersistentDataType.LIST.strings(), Arrays.asList(code));
        });
    }

    public static ItemStack createBotItem(String name, CodeBot bot) {
        return createItem(Material.FURNACE, meta -> {
            var items = bot.getInventory().getItems();
            var pdc = meta.getPersistentDataContainer();
            var pdcInventory = pdc.getAdapterContext().newPersistentDataContainer();
            var pdcProgram = pdc.getAdapterContext().newPersistentDataContainer();
            var lore = new LinkedList<String>();
            lore.add("§7Right-click on ground to place");
            lore.add("");

            pdc.set(key("BotId"), PersistentDataType.STRING, bot.getId().toString());

            if (bot.hasProgram()) {
                pdcProgram.set(key("File"), PersistentDataType.STRING, bot.getProgramSource().getSource().getName());
                pdcProgram.set(key("Dir"), PersistentDataType.STRING, bot.getProgramSource().getDirectory().name());
                pdc.set(key("Program"), PersistentDataType.TAG_CONTAINER, pdcProgram);

                lore.add("§7• Program: §f" + bot.getProgramSource().getSource().getName());
            }

            int itemsCount = 0;
            for (int i = 0; i < items.length; i++) {
                if (items[i] == null || items[i].getType().isAir()) {
                    continue;
                }

                var yaml = new YamlConfiguration();
                var serialized = items[i].serialize();
                serialized.forEach(yaml::set);

                pdcInventory.set(key(String.valueOf(i)), PersistentDataType.STRING, yaml.saveToString());
                itemsCount += items[i].getAmount();
            }

            pdc.set(key("Inventory"), PersistentDataType.TAG_CONTAINER, pdcInventory);
            lore.add("§7• Inventory: §f" + itemsCount + " items");

            meta.setDisplayName(name);
            meta.setLore(lore);
        });
    }

    public static ItemStack createBotItem(BotBuilder bot) {
        return createItem(Material.DISPENSER, meta -> {
            var pdc = meta.getPersistentDataContainer();
            pdc.set(key("BotId"), PersistentDataType.STRING, UUID.randomUUID().toString());

            var lore = new LinkedList<String>();
            lore.add("§7Right-click on ground to place");

            meta.setDisplayName("§eCodeBot");
            meta.setLore(lore);
        });
    }

    public static ItemStack createMonitorItem(String monitorId, Monitor.Size size) {
        return createItem(Material.OBSERVER, meta -> {
            var pdc = meta.getPersistentDataContainer();
            pdc.set(key("MonitorSize"), PersistentDataType.SHORT, (short) size.ordinal());
            if (monitorId != null)
                pdc.set(key("MonitorId"), PersistentDataType.STRING, monitorId);

            var lore = new LinkedList<String>();
            lore.add("§7Right-click on ground to place");

            meta.setDisplayName("§eMonitor §o(" + size.getWidth() + "x" + size.getHeight() + ")");
            meta.setLore(lore);
        });
    }

    public static ItemStack createMonitorItem(Monitor.Size size) {
        return createMonitorItem(null, size);
    }

    public static ItemStack createTransmitterItem() {
        return createItem(Material.COMPARATOR, meta -> {
            var pdc = meta.getPersistentDataContainer();
            pdc.set(key("Transmitter"), PersistentDataType.BOOLEAN, true);

            meta.setDisplayName("Redstone Transmitter");
        });
    }

    public static ItemStack createItem(Material material, Consumer<ItemMeta> meta) {
        var stack = new ItemStack(material);
        var itemMeta = stack.getItemMeta();

        meta.accept(itemMeta);
        stack.setItemMeta(itemMeta);

        return stack;
    }

    public static ItemStack createItem(Material material, String name) {
        return createItem(material, meta -> meta.setDisplayName(name));
    }

    public static NamespacedKey key(String key) {
        return new NamespacedKey(CodeBotsPlugin.inst(), key);
    }

}
