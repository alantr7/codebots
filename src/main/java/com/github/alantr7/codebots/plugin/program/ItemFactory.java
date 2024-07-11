package com.github.alantr7.codebots.plugin.program;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
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

            pdc.set(key("BotId"), PersistentDataType.STRING, bot.getId().toString());

            if (bot.hasProgram()) {
                pdcProgram.set(key("File"), PersistentDataType.STRING, bot.getProgramSource().getSource().getName());
                pdcProgram.set(key("Dir"), PersistentDataType.STRING, bot.getProgramSource().getDirectory().name());
                pdc.set(key("Program"), PersistentDataType.TAG_CONTAINER, pdcProgram);
            }

            for (int i = 0; i < items.length; i++) {
                if (items[i] == null || items[i].getType().isAir()) {
                    continue;
                }

                var yaml = new YamlConfiguration();
                var serialized = items[i].serialize();
                serialized.forEach(yaml::set);

                pdcInventory.set(key(String.valueOf(i)), PersistentDataType.STRING, yaml.saveToString());
            }

            pdc.set(key("Inventory"), PersistentDataType.TAG_CONTAINER, pdcInventory);

            meta.setDisplayName(name);
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
