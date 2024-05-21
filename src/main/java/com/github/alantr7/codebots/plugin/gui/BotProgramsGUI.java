package com.github.alantr7.codebots.plugin.gui;

import com.github.alantr7.bukkitplugin.BukkitPlugin;
import com.github.alantr7.bukkitplugin.gui.ClickType;
import com.github.alantr7.bukkitplugin.gui.CloseInitiator;
import com.github.alantr7.bukkitplugin.gui.GUI;
import com.github.alantr7.codebots.api.CodeBots;
import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Directory;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.data.DataLoader;
import com.github.alantr7.codebots.plugin.data.ProgramRegistry;
import com.github.alantr7.codebots.plugin.program.ItemFactory;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class BotProgramsGUI extends GUI {

    private final CodeBot bot;

    private static final int CATEGORY_SHARED = 0;

    private static final int CATEGORY_LOCAL = 1;

    private int selectedCategory;

    public BotProgramsGUI(Player player, CodeBot bot) {
        super(CodeBotsPlugin.inst(), player, false);
        this.bot = bot;

        init();
    }

    @Override
    protected void init() {
        createInventory("Manage Bot > Programs", 54);
        setInteractionEnabled(false);

        selectedCategory = bot.getProgramSource() != null && bot.getProgramSource().getDirectory() == Directory.LOCAL_PROGRAMS ? CATEGORY_LOCAL : CATEGORY_SHARED;
    }

    @Override
    protected void fill(Inventory inventory) {

        // Create filler items
        var filler = ItemFactory.createItem(Material.GRAY_STAINED_GLASS_PANE, "§7");
        for (int i = 0; i < 9; i++) {
            setItem(i, filler);
            setItem(i + 45, filler);
        }

        for (int i = 1; i < 5; i++) {
            setItem(i * 9, filler);
            setItem(i * 9 + 2, filler);
            setItem(i * 9 + 8, filler);
        }

        var categoryAll = ItemFactory.createItem(Material.BOOKSHELF, meta -> {
            meta.setDisplayName("§eShared Programs");
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        });
        var categoryLocal = ItemFactory.createItem(Material.BOOKSHELF, meta -> {
            meta.setDisplayName("§eLocal Programs");
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        });

        if (selectedCategory == CATEGORY_LOCAL) {
            categoryLocal.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
        } else {
            categoryAll.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
        }

        setItem(10, categoryAll);
        registerInteractionCallback(10, ClickType.LEFT, () -> {
            selectedCategory = CATEGORY_SHARED;
            refill();
        });

        setItem(19, categoryLocal);
        registerInteractionCallback(19, ClickType.LEFT, () -> {
            selectedCategory = CATEGORY_LOCAL;
            refill();
        });


        if (selectedCategory == CATEGORY_LOCAL) {
            var files = bot.getProgramsDirectory().listFiles();
            int index = 0;
            if (files != null) {
                for (var file : files) {
                    int column = index % 5;
                    int row = index / 5;

                    boolean isSelected = bot.getProgramSource() != null && bot.getProgramSource().getDirectory() == Directory.LOCAL_PROGRAMS && bot.getProgramSource().getSource().getName().equals(file.getName());
                    setItem(12 + row * 9 + column, ItemFactory.createItem(isSelected?  Material.ENCHANTED_BOOK : Material.BOOK, "§f" + file.getName()));
                    registerInteractionCallback(12 + row * 9 + column, ClickType.LEFT, () -> {
                        try {
                            bot.loadProgram(CodeBots.loadProgram(Directory.LOCAL_PROGRAMS, file));
                            refill();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    index++;
                }
            }
        }

        else if (selectedCategory == CATEGORY_SHARED) {
            var programs = CodeBotsPlugin.inst().getSingleton(ProgramRegistry.class).getPrograms();
            int index = 0;

            for (var program : programs) {
                int column = index % 5;
                int row = index / 5;

                boolean isSeleceted = bot.getProgramSource() != null && bot.getProgramSource().getDirectory() == program.getDirectory() && bot.getProgramSource().getSource().getName().equals(program.getName());
                setItem(12 + row * 9 + column, ItemFactory.createItem(isSeleceted?  Material.ENCHANTED_BOOK : Material.BOOK, "§f" + program.getName()));
                registerInteractionCallback(12 + row * 9 + column, ClickType.LEFT, () -> {
                    try {
                        bot.loadProgram(program);
                        refill();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                index++;
            }
        }

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
