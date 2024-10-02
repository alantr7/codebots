package com.github.alantr7.codebots.api.bot;

import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.editor.CodeEditorClient;
import com.github.alantr7.codebots.plugin.editor.EditorSession;
import com.github.alantr7.codebots.plugin.program.ItemFactory;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

@Getter
public class ProgramSource {

    private final Directory directory;

    private final String name;

    private final File source;

    private final String[] code;

    public ProgramSource(Directory category, String name, File source, String[] code) {
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
            var bytes = Files.readAllBytes(source.toPath());
            return CodeBotsPlugin.inst().getSingleton(CodeEditorClient.class).createSession(bytes);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

}
