package com.github.alantr7.codebots.api.bot;

import com.github.alantr7.codebots.CodeBotsPlugin;
import com.github.alantr7.codebots.fs.BotFile;
import com.github.alantr7.codebots.editor.CodeEditorClient;
import com.github.alantr7.codebots.editor.EditorSession;
import com.github.alantr7.codebots.item.ItemFactory;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

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
        this.code = code.trim();
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
