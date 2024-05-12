package com.github.alantr7.codebots.plugin.gui;

import com.github.alantr7.bukkitplugin.BukkitPlugin;
import com.github.alantr7.bukkitplugin.gui.ClickType;
import com.github.alantr7.bukkitplugin.gui.CloseInitiator;
import com.github.alantr7.bukkitplugin.gui.GUI;
import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.language.runtime.modules.FileModule;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BotGUI extends GUI {

    private final CodeBot bot;

    public BotGUI(Player player, CodeBot bot) {
        super(CodeBotsPlugin.inst(), player);
        this.bot = bot;
    }

    @Override
    protected void init() {
        createInventory("Manage Bot", 27);
        setInteractionEnabled(false);
    }

    @Override
    protected void fill(Inventory inventory) {
        var buttonInventory = new ItemStack(Material.CHEST);
        var inventoryMeta = buttonInventory.getItemMeta();
        inventoryMeta.setDisplayName("§6Inventory");
        buttonInventory.setItemMeta(inventoryMeta);

        setItem(11, buttonInventory);
        registerInteractionCallback(11, ClickType.LEFT, () -> {
            getPlayer().openInventory(bot.getInventory());
        });

        var buttonProgram = new ItemStack(Material.PAPER);
        var meta = buttonProgram.getItemMeta();
        if (!bot.isActive()) {
            buttonProgram.setType(Material.LIME_TERRACOTTA);
            meta.setDisplayName("§aStart Program");
        } else {
            buttonProgram.setType(Material.RED_TERRACOTTA);
            meta.setDisplayName("§cStop Program");
        }
        if (bot.getProgram() != null) {
            meta.setLore(List.of(
                    "§7Program: §f" + ((FileModule) bot.getProgram().getMainModule()).getFile().getName()
            ));
        }
        buttonProgram.setItemMeta(meta);

        setItem(13, buttonProgram);
        registerInteractionCallback(13, ClickType.LEFT, () -> {
            if (bot.isActive()) {
                bot.setActive(false);
                bot.getProgram().reset();
            } else {
                bot.getProgram().prepareMainFunction();
                bot.setActive(true);
            }
        });
    }

    @Override
    protected void onInventoryOpen() {

    }

    @Override
    protected void onInventoryClose(CloseInitiator closeInitiator) {

    }

    @Override
    public void onItemInteract(int i, @NotNull ClickType clickType, @Nullable ItemStack itemStack) {

    }

}
