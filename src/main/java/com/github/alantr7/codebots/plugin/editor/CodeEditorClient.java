package com.github.alantr7.codebots.plugin.editor;

import com.github.alantr7.bukkitplugin.annotations.core.Inject;
import com.github.alantr7.bukkitplugin.annotations.core.Invoke;
import com.github.alantr7.bukkitplugin.annotations.core.InvokePeriodically;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.config.Config;
import com.github.alantr7.codebots.plugin.utils.MathHelper;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Singleton
public class CodeEditorClient {

    private final HttpClient client;

    private final Map<UUID, EditorSession> activeSessions = new HashMap<>();

    private final Map<String, EditorSession> activeSessionsByFile = new HashMap<>();

    private final Map<UUID, EditorSession> activeSessionsByBots = new HashMap<>();

    private String serverToken;

    @Inject
    private CodeBotsPlugin plugin;

    public CodeEditorClient() {
        this.client = HttpClient.newHttpClient();
    }

    @Invoke(Invoke.Schedule.AFTER_PLUGIN_ENABLE)
    void onPluginEnable() {
        this.initialize();
    }

    @SneakyThrows
    private void initialize() {
        // Get an access token from the server
        fetchAccessToken();
    }

    public CompletableFuture<String> fetchAccessToken() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var json = new JSONObject();
                json.put("version", CodeBotsPlugin.inst().getDescription().getVersion());

                var request = HttpRequest.newBuilder()
                        .uri(new URI(Config.EDITOR_URL + "/api/create-server-token"))
                        .POST(HttpRequest.BodyPublishers.ofString(json.toJSONString()))
                        .header("Content-Type", "application/json")
                        .header("X-Not-Secret", "xtfQbY9g5n56jsi9KEvocB2p")
                        .build();

                var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    plugin.getLogger().warning("Received status code " + response.statusCode() + " from the editor while fetching server token.");
                    return null;
                }

                plugin.getLogger().info("Server token fetched.");
                return serverToken = response.body();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public void registerActiveSessionByBot(EditorSession session, CodeBot bot) {
        activeSessionsByBots.put(bot.getId(), session);
    }

    public CompletableFuture<EditorSession> createSession(File[] files) {
        if (serverToken == null || files == null)
            return CompletableFuture.completedFuture(null);

        return CompletableFuture.supplyAsync(() -> {
            try {
                var json = new JSONObject();
                var jsonFiles = new JSONArray();
                json.put("files", jsonFiles);

                var sessionFiles = new LinkedHashMap<String, EditorSessionFile>();

                for (var file : files) {
                    var name = file.getName();
                    var content = new String(Files.readAllBytes(file.toPath()));

                    var fileObject = new JSONObject();
                    fileObject.put("name", name);
                    fileObject.put("content", content);
                    jsonFiles.add(fileObject);

                    sessionFiles.put(name, new EditorSessionFile(content));
                }

                var request = HttpRequest.newBuilder()
                        .uri(new URI(Config.EDITOR_URL + "/api/create-session"))
                        .POST(HttpRequest.BodyPublishers.ofString(json.toJSONString()))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + serverToken)
                        .build();

                var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200)
                    return null;

                var responseSession = (JSONObject) new JSONParser().parse(response.body());
                var session = new EditorSession(
                        UUID.fromString((String) responseSession.get("id")),
                        (String) responseSession.get("access_token"),
                        (Long) responseSession.get("expires_at"),
                        sessionFiles
                );

                Bukkit.getScheduler().runTask(CodeBotsPlugin.inst(), () -> {
                    activeSessions.put(session.id(), session);
                });
                return session;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public CompletableFuture<Void> fetchSession(EditorSession session) {
        if (serverToken == null)
            return CompletableFuture.completedFuture(null);

        if (session.isCurrentlyFetching())
            return CompletableFuture.completedFuture(null);

        session.setCurrentlyFetching(true);

        return CompletableFuture.runAsync(() -> {
            try {
                StringBuilder url = new StringBuilder(Config.EDITOR_URL);
                url.append("/api/sessions/");
                url.append(session.id().toString());

                if (session.getLastChangeId() != null) {
                    url.append("?last_change_id=").append(session.getLastChangeId());
                }

                var request = HttpRequest.newBuilder()
                        .uri(new URI(url.toString()))
                        .header("Authorization", "Bearer " + serverToken)
                        .header("Cookie", "access_token=" + session.accessToken())
                        .GET()
                        .build();

                var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 304) {
                    return;
                }
                if (response.statusCode() != 200) {
                    throw new Exception("Status code: " + response.statusCode());
                }

                var responseSession = (JSONObject) new JSONParser().parse(response.body());
                var filesArray = (JSONArray) responseSession.get("files");

                filesArray.forEach(fileJsonObject -> {
                    var fileJson = (JSONObject) fileJsonObject;
                    var fileName = (String) fileJson.get("name");
                    var fileContent = (String) fileJson.get("content");
                    var fileLastChangeId = (String) fileJson.get("last_change_id");
                    var fileLastChangeTimestamp = (Long) MathHelper.any(responseSession.get("last_change_timestamp"), 0L);

                    var file = session.getFiles().get(fileName);
                    if (file == null)
                        return;

                    file.setCode(fileContent);
                    file.setLastChangeId(fileLastChangeId);
                    file.setLastChangeTimestamp(fileLastChangeTimestamp);
                });

                session.notifySubscribers();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                session.setCurrentlyFetching(false);
                session.setLastFetched(System.currentTimeMillis());
            }
        });
    }

    public @Nullable EditorSession getActiveSession(@NotNull UUID id) {
        return activeSessions.get(id);
    }

    public @Nullable EditorSession getActiveSessionByBot(@NotNull CodeBot bot) {
        return activeSessionsByBots.get(bot.getId());
    }

    public void deleteSession(EditorSession session) {
        activeSessions.remove(session.id());
        var it = activeSessionsByBots.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue() == session) {
                it.remove();
                break;
            }
        }

        CompletableFuture.runAsync(() -> {
            try {
                HttpRequest.newBuilder()
                        .uri(new URI(Config.EDITOR_URL + "/api/sessions/" + session.id()))
                        .header("Authorization", "Bearer " + serverToken)
                        .DELETE()
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @InvokePeriodically(interval = 20 * 60 * 60)
    void renewAccessToken() {
        fetchAccessToken();
    }

}
