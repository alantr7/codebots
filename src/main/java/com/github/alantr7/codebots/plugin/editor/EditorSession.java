package com.github.alantr7.codebots.plugin.editor;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.config.Config;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class EditorSession {

    private final UUID id;

    private final String accessToken;

    private final long expiry;

    @Getter @Setter
    private long lastModified;

    @Getter
    @Setter
    private boolean isCurrentlyFetching = false;

    @Getter
    @Setter
    private long lastFetched = 0;

    @Getter
    private final Map<String, EditorSessionFile> files;

    private final Consumer<EditorSession>[] subscribers = new Consumer[2];

    public EditorSession(UUID id, String accessToken, long expiry, Map<String, EditorSessionFile> files) {
        this.id = id;
        this.accessToken = accessToken;
        this.expiry = expiry;
        this.files = files;
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
        receiver.sendMessage("§eEditor session created!");
        var editorButton = Component.text("here")
                .decorate(TextDecoration.UNDERLINED)
                .color(TextColor.color(255, 200, 0))
                .clickEvent(ClickEvent.openUrl(
                        Config.EDITOR_URL + "/edit/" + id + "?token=" + accessToken
                ));
        receiver.sendMessage(
                Component.text("§eClick ")
                        .append(editorButton)
                        .append(Component.text("§e to open the editor."))
        );
    }

    public void subscribe(Consumer<EditorSession> session) {
        if (subscribers[0] == null) {
            subscribers[0] = session;
        } else if (subscribers[1] == null) {
            subscribers[1] = session;
        }
    }

    public void notifySubscribers() {
        for (var subscriber : subscribers) {
            if (subscriber != null)
                subscriber.accept(this);
        }
    }

    public static Consumer<EditorSession> createBotSubscriber(CodeBot bot) {
        return session -> {
            try {
                session.getFiles().forEach((name, fileInfo) -> {
                    var file = new File(bot.getProgramsDirectory(), name);
                    if (!file.exists())
                        return;

                    try {
                        Files.write(file.toPath(), fileInfo.getCode().getBytes(StandardCharsets.UTF_8));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                bot.reloadProgram();
            } catch (Exception e) {
//                getPlayer().sendMessage("§cThere was an error while loading the program.");
//                if (e instanceof ParserException || e instanceof ParseException) {
//                    getPlayer().sendMessage("§4" + e.getMessage());
//                }

                e.printStackTrace();
            }
        };
    }

}
