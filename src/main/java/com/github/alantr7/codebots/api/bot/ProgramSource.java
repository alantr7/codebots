package com.github.alantr7.codebots.api.bot;

import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.bot.BotFile;
import com.github.alantr7.codebots.plugin.editor.CodeEditorClient;
import com.github.alantr7.codebots.plugin.editor.EditorSession;
import com.github.alantr7.codebots.plugin.program.ItemFactory;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

@Getter
public class ProgramSource {

    private final Directory directory;

    private final String name;

    private final BotFile source;

    private final String code;

    public ProgramSource(Directory category, String name, BotFile source, String code) {
        this.directory = category;
        this.name = name;
        this.source = source;
        this.code = code;
    }

    public ItemStack toItem() {
        return ItemFactory.createProgramItem(name, code);
    }

    public CompletableFuture<EditorSession> createEditor() {
        try {
            return CodeBotsPlugin.inst().getSingleton(CodeEditorClient.class).createSession(Collections.singletonList(source));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

}
