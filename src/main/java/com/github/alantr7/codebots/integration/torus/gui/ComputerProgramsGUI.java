package com.github.alantr7.codebots.integration.torus.gui;

import com.github.alantr7.bukkitplugin.gui.ClickType;
import com.github.alantr7.bukkitplugin.gui.CloseInitiator;
import com.github.alantr7.bukkitplugin.gui.GUI;
import com.github.alantr7.codebots.CodeBotsPlugin;
import com.github.alantr7.codebots.api.CodeBots;
import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Directory;
import com.github.alantr7.codebots.api.error.ProgramError;
import com.github.alantr7.codebots.cbslang.exceptions.ParserException;
import com.github.alantr7.codebots.config.Config;
import com.github.alantr7.codebots.data.ProgramRegistry;
import com.github.alantr7.codebots.editor.CodeEditorClient;
import com.github.alantr7.codebots.editor.EditorSession;
import com.github.alantr7.codebots.fs.BotFile;
import com.github.alantr7.codebots.integration.torus.CodeBotsTorusIntEntry;
import com.github.alantr7.codebots.integration.torus.machine.ComputerInstance;
import com.github.alantr7.codebots.item.ItemFactory;
import com.github.alantr7.codebots.utils.FileHelper;
import com.github.alantr7.codebots.world.bot.CraftCodeBot;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ComputerProgramsGUI extends GUI {

    @Getter
    private final ComputerInstance computer;

    private static final ItemStack BTN_CREATE_PROGRAM = ItemFactory.createItem(Material.WRITABLE_BOOK, "§eCreate Program");

    public ComputerProgramsGUI(Player player, ComputerInstance computer) {
        super(CodeBotsPlugin.inst(), player, false);
        this.computer = computer;

        init();
    }

    @Override
    protected void init() {
        createInventory("Computer", 54);
        setInteractionEnabled(false);
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

        var startBtn = new ItemStack(computer.isActive() ? Material.RED_CONCRETE : Material.LIME_CONCRETE);
        var startBtnMeta = startBtn.getItemMeta();
        startBtnMeta.setDisplayName(computer.isActive() ? (ChatColor.GREEN + "Start Program") : (ChatColor.RED + "Stop Program"));
        startBtn.setItemMeta(startBtnMeta);
        setItem(10, startBtn);
        registerInteractionCallback(10, ClickType.LEFT, () -> {
            if (!computer.hasProgram()) {
                getPlayer().sendMessage("§cProgram is not loaded.");
                return;
            }

            var session = CodeBotsPlugin.inst().getSingleton(CodeEditorClient.class).getActiveSessionByBot(computer.getEditorIdentifier());
            if (session != null && session.isCurrentlyFetching()) {
                getPlayer().sendMessage("§cProgram download is in progress. Please try again.");
                return;
            }

            if (session != null && System.currentTimeMillis() - session.getLastFetched() > 3000) {
                session.fetch().whenComplete((sess, err) -> {
                    Bukkit.getScheduler().runTaskLater(CodeBotsPlugin.inst(), () -> computer.setActive(!computer.isActive()), 1L);
                });
            } else {
                computer.setActive(!computer.isActive());
            }
        });

        var editorSession = computer.getEditorSession();
        var editorButton = ItemFactory.createItem(Material.WRITABLE_BOOK, meta -> {
            meta.setDisplayName("§eOpen §6Code Editor");
            if (editorSession != null) {
                meta.setLore(List.of("§eEditor session is active!", "§eShift-right-click to close the editor"));
            }
        });

        setItem(37, editorButton);
        registerInteractionCallback(37, ClickType.LEFT, () -> {
            if (editorSession != null) {
                editorSession.sendLink(getPlayer());
            } else {
                var player = getPlayer();
                if (computer.getFileSystem().getFiles().isEmpty()) {
                    player.sendMessage("§cYou must create at least one file before opening editor.");
                    return;
                }

                player.sendMessage("§oCreating an editor session. Please wait...");
                CodeBotsPlugin.inst().getSingleton(CodeEditorClient.class).createSession(CodeBotsTorusIntEntry.getComputerModuleRepository(), computer.getFileSystem().getFiles(), player.getName())
                        .whenComplete((sess, err) -> {
                            CodeBotsPlugin.inst().getSingleton(CodeEditorClient.class).registerActiveSessionByBot(sess, computer.getEditorIdentifier());
                            sess.subscribe(EditorSession.createComputerSubscriber(computer));
                            sess.sendLink(player);
                        });
            }
            close();
        });
        registerInteractionCallback(37, ClickType.RIGHT, () -> {
            if (!hasClickEvent() || !getClickEvent().isShiftClick())
                return;

            var session = computer.getEditorSession();
            if (session == null)
                return;

            CodeBotsPlugin.inst().getSingleton(CodeEditorClient.class).deleteSession(session);
            getPlayer().sendMessage("§eCode editor session closed.");
            refill();
        });

        var files = computer.getFileSystem().getFiles();
        int index = 0;
        if (files != null) {
            for (var file : files) {
                int slot = getProgramSlot(index++);

                boolean isSelected = computer.getProgramSource() != null && computer.getProgramSource().getSource().getName().equals(file.getName());
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
                        computer.loadProgram(CodeBots.loadProgram(Directory.LOCAL_PROGRAMS, file));
                        refill();
                    } catch (ParserException e) {
                        computer.setError(new ProgramError(ProgramError.ErrorLocation.PARSER, e.getMessage()));
                        getPlayer().sendMessage("§cThere was an error while loading the program.");
                        getPlayer().sendMessage("§cLine #" + e.getLine() + ": §4" + e.getMessage());

                        e.printStackTrace();
                    } catch (Exception e) {
                        getPlayer().sendMessage("§cThere was an error while loading the program.");
                        e.printStackTrace();
                    }
                });
                registerInteractionCallback(slot, ClickType.RIGHT, () -> {
                    if (!hasClickEvent() || !getClickEvent().isShiftClick())
                        return;

                    if (computer.hasProgram() && computer.getProgramSource().getSource().getName().equals(file.getName())) {
                        getPlayer().sendMessage("§cYou can not delete a program that's being used.");
                        return;
                    }

                    if (computer.getEditorSession() != null) {
                        getPlayer().sendMessage("§cYou can not delete a program while editor is active.");
                        return;
                    }

                    computer.getFileSystem().deleteFile(file.getName());
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
                while (computer.getFileSystem().getFile("program_" + fileNameId + ".cbs") != null)
                    fileNameId++;

                var fileName = "program_" + fileNameId + ".cbs";
                registerInteractionCallback(slot, ClickType.LEFT, () -> {
                    if (computer.getEditorSession() != null) {
                        getPlayer().sendMessage("§cYou can not create a new program while editor is active.");
                        return;
                    }
                    BotFile file = computer.getFileSystem().createFile(fileName);
                    file.setContent(FileHelper.loadResource("computer_default_program.cbs"));
                    if (computer.getFileSystem().getFiles().size() == 1) {
                        try {
                            computer.loadProgram(CodeBots.loadProgram(Directory.LOCAL_PROGRAMS, file));
                            refill();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    refill();
                });
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
