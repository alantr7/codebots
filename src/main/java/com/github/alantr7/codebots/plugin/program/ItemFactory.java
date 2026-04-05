package com.github.alantr7.codebots.plugin.program;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.fs.BotFile;
import com.github.alantr7.codebots.fs.BotFileSystem;
import com.github.alantr7.codebots.item.BotsItem;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;
import java.util.function.Consumer;

public class ItemFactory {

    public static ItemStack createProgramItem(String name, String code) {
        return createItem(Material.PAPER, meta -> {
            meta.setDisplayName("§eProgram: §f" + name);
            meta.getPersistentDataContainer().set(new NamespacedKey(CodeBotsPlugin.inst(), "code"), PersistentDataType.STRING, code);
        });
    }

    public static ItemStack createBotItem(CodeBot bot) {
        ItemStack item = BotsItem.BOT.toItemStack();
        ItemMeta meta = item.getItemMeta();

        var items = bot.getInventory().getItems();
        var pdc = meta.getPersistentDataContainer();
        var pdcProgram = pdc.getAdapterContext().newPersistentDataContainer();
        var pdcInventory = pdc.getAdapterContext().newPersistentDataContainer();
        var lore = new LinkedList<String>();
        lore.add("§7Right-click on ground to place");
        lore.add("");

        pdc.set(key("BotId"), PersistentDataType.STRING, bot.getId().toString());

        long[] fileSystemPointers = new long[bot.getFileSystem().getFiles().size()];
        Iterator<BotFile> fs = bot.getFileSystem().getFiles().iterator();
        for (int i = 0; fs.hasNext(); i++) {
            fileSystemPointers[i] = fs.next().getPosition();
        }
        pdc.set(key("FileSystem"), PersistentDataType.LONG_ARRAY, fileSystemPointers);
        lore.add("§7• File System: §f" + fileSystemPointers.length + " files");

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
        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
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
