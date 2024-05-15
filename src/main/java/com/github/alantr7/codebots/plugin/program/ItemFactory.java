package com.github.alantr7.codebots.plugin.program;

import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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

}
