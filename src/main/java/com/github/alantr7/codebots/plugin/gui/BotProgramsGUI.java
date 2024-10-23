package com.github.alantr7.codebots.plugin.gui;

import com.github.alantr7.bukkitplugin.gui.ClickType;
import com.github.alantr7.bukkitplugin.gui.CloseInitiator;
import com.github.alantr7.bukkitplugin.gui.GUI;
import com.github.alantr7.codebots.api.CodeBots;
import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Directory;
import com.github.alantr7.codebots.api.error.ProgramError;
import com.github.alantr7.codebots.language.compiler.parser.error.ParserException;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.bot.CraftCodeBot;
import com.github.alantr7.codebots.plugin.config.Config;
import com.github.alantr7.codebots.plugin.data.BotRegistry;
import com.github.alantr7.codebots.plugin.data.ProgramRegistry;
import com.github.alantr7.codebots.plugin.editor.CodeEditorClient;
import com.github.alantr7.codebots.plugin.editor.EditorSession;
import com.github.alantr7.codebots.plugin.program.ItemFactory;
import com.github.alantr7.codebots.plugin.utils.FileHelper;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public class BotProgramsGUI extends GUI {

    @Getter
    private final CodeBot bot;

    private static final int CATEGORY_SHARED = 0;

    private static final int CATEGORY_LOCAL = 1;

    private static final ItemStack BTN_CREATE_PROGRAM = ItemFactory.createItem(Material.WRITABLE_BOOK, "§eCreate Program");

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

        selectedCategory = Config.BOT_ALLOWED_SCRIPTS == 0
                ? bot.getProgramSource() != null && bot.getProgramSource().getDirectory() == Directory.LOCAL_PROGRAMS ? CATEGORY_LOCAL : CATEGORY_SHARED
                : Config.BOT_ALLOWED_SCRIPTS == 1 ? CATEGORY_LOCAL : CATEGORY_SHARED;
    }

    @Override
    protected void fill(Inventory inventory) {
        clearInteractionCallbacks();

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
            if (Config.BOT_ALLOWED_SCRIPTS == 1) {
                return;
            }

            selectedCategory = CATEGORY_SHARED;
            refill();
        });

        setItem(19, categoryLocal);
        registerInteractionCallback(19, ClickType.LEFT, () -> {
            if (Config.BOT_ALLOWED_SCRIPTS == 2) {
                return;
            }

            selectedCategory = CATEGORY_LOCAL;
            refill();
        });


        if (selectedCategory == CATEGORY_LOCAL) {
            var files = bot.getProgramsDirectory().listFiles();
            int index = 0;
            if (files != null) {
                for (var file : files) {
                    int slot = getProgramSlot(index++);

                    boolean isSelected = bot.getProgramSource() != null && bot.getProgramSource().getDirectory() == Directory.LOCAL_PROGRAMS && bot.getProgramSource().getSource().getName().equals(file.getName());
                    setItem(slot, ItemFactory.createItem(
                            isSelected ? Material.ENCHANTED_BOOK : Material.BOOK,
                            meta -> {
                                meta.setDisplayName("§f" + file.getName());
                                meta.setLore(List.of(
                                        "§eLeft-click to load the program",
                                        "§eShift-right-click to delete §c(Irreversible!)"
                                ));
                            }
                    ));
                    registerInteractionCallback(slot, ClickType.LEFT, () -> {
                        try {
                            bot.loadProgram(CodeBots.loadProgram(Directory.LOCAL_PROGRAMS, file));
                            refill();
                        } catch (ParserException e) {
                            ((CraftCodeBot) bot).setError(new ProgramError(ProgramError.ErrorLocation.PARSER, e.getMessage()));
                            getPlayer().sendMessage("§cThere was an error while loading the program.");
                            getPlayer().sendMessage("§4" + e.getMessage());

                            e.printStackTrace();
                        } catch (Exception e) {
                            getPlayer().sendMessage("§cThere was an error while loading the program.");
                            e.printStackTrace();
                        }
                    });
                    registerInteractionCallback(slot, ClickType.RIGHT, () -> {
                        if (!hasClickEvent() || !getClickEvent().isShiftClick())
                            return;

                        if (bot.hasProgram() && bot.getProgramSource().getSource().getPath().equals(file.getPath())) {
                            getPlayer().sendMessage("§cYou can not delete a program that's being used.");
                            return;
                        }

                        file.delete();
                        refill();

                        getPlayer().sendMessage("§eSuccessfully deleted a program.");
                    });
                }

                // Add a button for creating a new program
                if (index < Config.BOT_MAX_LOCAL_PROGRAMS) {
                    int slot = getProgramSlot(index);

                    setItem(slot, BTN_CREATE_PROGRAM);

                    int fileNameId = 0;
                    while (new File(bot.getProgramsDirectory(), "program_" + fileNameId + ".js").exists())
                        fileNameId++;

                    var fileName = "program_" + fileNameId + ".js";
                    registerInteractionCallback(slot, ClickType.LEFT, () -> {
                        FileHelper.saveResource("default_program.js", new File(bot.getProgramsDirectory(), fileName));
                        refill();
                    });
                }
            }
        } else if (selectedCategory == CATEGORY_SHARED) {
            var programs = CodeBotsPlugin.inst().getSingleton(ProgramRegistry.class).getPrograms();
            int index = 0;

            for (var program : programs) {
                int column = index % 5;
                int row = index / 5;

                boolean isSeleceted = bot.getProgramSource() != null && bot.getProgramSource().getDirectory() == program.getDirectory() && bot.getProgramSource().getSource().getName().equals(program.getName());
                setItem(12 + row * 9 + column, ItemFactory.createItem(isSeleceted ? Material.ENCHANTED_BOOK : Material.BOOK, "§f" + program.getName()));
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

    private static int getProgramSlot(int index) {
        int column = index % 5;
        int row = index / 5;
        return 12 + row * 9 + column;
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
