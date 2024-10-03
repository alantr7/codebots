package com.github.alantr7.codebots.plugin.editor;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.config.Config;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class EditorSession {

    private final UUID id;

    private final String accessToken;

    private final long expiry;

    @Getter
    @Setter
    private String code;

    @Getter @Setter
    private String lastChangeId;

    @Getter @Setter
    private long lastChangeTimestamp;

    @Getter @Setter
    private CodeBot attachedBot;

    @Getter @Setter
    private boolean isCurrentlyFetching = false;

    @Getter @Setter
    private long lastFetched = 0;

    public EditorSession(UUID id, String accessToken, long expiry, String code) {
        this.id = id;
        this.accessToken = accessToken;
        this.expiry = expiry;
        this.code = code;
    }

    public CompletableFuture<Void> fetch() {
        return CodeBotsPlugin.inst().getSingleton(CodeEditorClient.class).fetchSession(this);
    }

    public UUID id() {
        return id;
    }

    public String accessToken() {
        return accessToken;
    }

    public long expiry() {
        return expiry;
    }

    public void sendLink(CommandSender receiver) {
        receiver.sendMessage("Created a new editor session!");
        var editorButton = Component.text("here")
                .decorate(TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.openUrl(
                        Config.EDITOR_URL + "/edit/" + id + "?token=" + accessToken
                ));
        receiver.sendMessage(
                Component.text("Click ")
                        .append(editorButton)
                        .append(Component.text("§r to open the editor."))
        );
    }

}
