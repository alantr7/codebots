package com.github.alantr7.codebots.plugin.editor;

import com.github.alantr7.bukkitplugin.annotations.core.Inject;
import com.github.alantr7.bukkitplugin.annotations.core.Invoke;
import com.github.alantr7.bukkitplugin.annotations.core.InvokePeriodically;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.bytils.buffer.ByteArrayReader;
import com.github.alantr7.bytils.buffer.ByteArrayWriter;
import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.DataType;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.ExternalFunction;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Module;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.fs.BotFile;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Singleton
public class CodeEditorClient {

    private final HttpClient client;

    private final Map<UUID, EditorSession> activeSessions = new HashMap<>();

    private final Map<UUID, EditorSession> activeSessionsByBots = new HashMap<>();

    private String serverToken;

    private long serverTokenExpiry;

    @Inject
    private CodeBotsPlugin plugin;

    public CodeEditorClient() {
        this.client = HttpClient.newHttpClient();
    }

    public CompletableFuture<String> fetchAccessToken() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var json = new JSONObject();
                json.put("version", CodeBotsPlugin.inst().getDescription().getVersion());

                var request = HttpRequest.newBuilder()
                        .uri(new URI(Config.EDITOR_URL + "/api/servers"))
                        .POST(HttpRequest.BodyPublishers.ofString(json.toJSONString()))
                        .header("Content-Type", "application/json")
                        .build();

                var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    plugin.getLogger().warning("Received status code " + response.statusCode() + " from the editor while fetching server token.");
                    return null;
                }

                plugin.getLogger().info("Server token fetched.");
                serverTokenExpiry = System.currentTimeMillis() + 24 * 60 * 60 * 1000;
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

    public CompletableFuture<EditorSession> createSession(Collection<BotFile> files) {
        return this.createSession(files, (String)null);
    }

    @SuppressWarnings("unchecked")
    public CompletableFuture<EditorSession> createSession(Collection<BotFile> files, String author) {
        if (serverToken == null || files == null)
            return CompletableFuture.completedFuture(null);

        return CompletableFuture.supplyAsync(() -> {
            try {
                var json = new JSONObject();
                if (author != null) {
                    json.put("author", author);
                }

                var jsonModules = new JSONObject();
                json.put("modules", jsonModules);

                for (Module module : CodeBotsPlugin.inst().getModuleRepository().getModules()) {
                    JSONObject moduleObject = new JSONObject();
                    moduleObject.put("name", module.getName());

                    JSONArray jsonFunctions = new JSONArray();
                    moduleObject.put("functions", jsonFunctions);
                    for (ExternalFunction fun : module.getFunctions()) {
                        JSONObject functionObject = new JSONObject();
                        functionObject.put("module", module.getName().equals("lang") ? null : module.getName());
                        functionObject.put("name", fun.getName());
                        functionObject.put("return_type", fun.getReturnType().getTypeName().toLowerCase());
                        functionObject.put("parameter_types", Arrays.stream(fun.getParameterTypes()).map(DataType::getTypeName).map(String::toLowerCase).toList());
                        functionObject.put("completion", fun.getName() + "($1)$0");

                        jsonFunctions.add(functionObject);
                    }

                    jsonModules.put(module.getName(), moduleObject);
                }

                var jsonFiles = new JSONArray();
                json.put("files", jsonFiles);

                var sessionFiles = new LinkedHashMap<String, EditorSessionFile>();

                for (var file : files) {
                    var name = file.getName();
                    var fileObject = new JSONObject();
                    fileObject.put("name", name);
                    fileObject.put("content", new String(file.getContent()).trim());
                    jsonFiles.add(fileObject);

                    sessionFiles.put(name, new EditorSessionFile(new String(file.getContent()).trim()));
                }

                var request = HttpRequest.newBuilder()
                        .uri(new URI(Config.EDITOR_URL + "/api/sessions"))
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

                if (session.getLastModified() != 0) {
                    url.append("?last_modified=").append(session.getLastModified());
                }

                var request = HttpRequest.newBuilder()
                        .uri(new URI(url.toString()))
                        .header("Authorization", "Bearer " + session.accessToken())
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
                var lastChangeTimestamp = (Long) MathHelper.any(responseSession.get("last_modified"), 0L);

                filesArray.forEach(fileJsonObject -> {
                    var fileJson = (JSONObject) fileJsonObject;
                    var fileName = (String) fileJson.get("name");
                    var fileContent = (String) fileJson.get("content");
                    var fileLastChangeTimestamp = (Long) MathHelper.any(fileJson.get("last_modified"), 0L);

                    var file = session.getFiles().get(fileName);
                    if (file == null)
                        return;

                    file.setCode(fileContent);
                    file.setLastModified(fileLastChangeTimestamp);
                });

                session.setLastModified(lastChangeTimestamp);
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
                var request = HttpRequest.newBuilder()
                        .uri(new URI(Config.EDITOR_URL + "/api/sessions/" + session.id()))
                        .header("Authorization", "Bearer " + serverToken)
                        .DELETE()
                        .build();

                client.send(request, HttpResponse.BodyHandlers.discarding());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @InvokePeriodically(delay = 10, interval = 20 * 60 * 60)
    void renewAccessToken() {
        if (serverTokenExpiry - System.currentTimeMillis() <= 4 * 60 * 60 * 1000) {
            fetchAccessToken();
        }
    }

    @Invoke(Invoke.Schedule.AFTER_PLUGIN_ENABLE)
    void loadSessions() throws Exception {
        File editorFile = new File(CodeBotsPlugin.inst().getDataFolder(), "editor.dat");
        if (!editorFile.exists())
            return;

        ByteArrayReader reader = new ByteArrayReader(Files.readAllBytes(editorFile.toPath()));
        if (reader.readU1() == 0)
            return;

        serverToken = reader.readString();
        serverTokenExpiry = reader.readLong();

        if (serverTokenExpiry < System.currentTimeMillis()) // server token has expired
            return;

        int activeSessionsCount = reader.readU1();
        for (int i = 0; i < activeSessionsCount; i++) {
            UUID sessionId = UUID.fromString(reader.readShortString()); // session id
            String accessToken = reader.readString(); // access token
            long expiry = reader.readLong(); // expiry
            long lastModified = reader.readLong(); // last mod
            long lastFetched = reader.readLong(); // last fet

            System.out.println(sessionId + ", " + accessToken + ", " + expiry + ", " + lastModified + ", " + lastFetched);

            int filesCount = reader.readU1();

            Map<String, EditorSessionFile> files = new LinkedHashMap<>();
            for (int j = 0; j < filesCount; j++) {
                String path = reader.readShortString(); // file path
                long fileLastModified = reader.readLong(); // last mod
                EditorSessionFile file = new EditorSessionFile(reader.readString());
                file.setLastModified(fileLastModified);
                files.put(path, file);
            }

            EditorSession session = new EditorSession(sessionId, accessToken, expiry, files);
            session.setLastModified(lastModified);
            session.setLastFetched(lastFetched);

            activeSessions.put(sessionId, session);
        }

        int sessionsByBotsCount = reader.readU1();
        for (int i = 0; i < sessionsByBotsCount; i++) {
            UUID botId = UUID.fromString(reader.readShortString());
            EditorSession session = activeSessions.get(UUID.fromString(reader.readShortString()));

            if (session != null) {
                activeSessionsByBots.put(botId, session);
            }
        }
    }

    @InvokePeriodically(delay = 20 * 60L, interval = 20 * 60L)
    @Invoke(Invoke.Schedule.AFTER_PLUGIN_DISABLE)
    void saveSessions() {
        ByteArrayWriter buffer = new ByteArrayWriter(1024);
        if (serverToken != null && System.currentTimeMillis() < serverTokenExpiry) {
            buffer.writeU1(1);
            buffer.writeString(serverToken);
            buffer.writeLong(serverTokenExpiry);

            buffer.writeU1(activeSessions.size());
            for (EditorSession session : activeSessions.values()) {
                buffer.writeShortString(session.id().toString());
                buffer.writeString(session.accessToken());
                buffer.writeLong(session.expiry());
                buffer.writeLong(session.getLastModified());
                buffer.writeLong(session.getLastFetched());

                buffer.writeU1(session.getFiles().size());
                session.getFiles().forEach((path, file) -> {
                    buffer.writeShortString(path);
                    buffer.writeLong(file.getLastModified());
                    buffer.writeString(file.getCode());
                });
            }

            buffer.writeU1(activeSessionsByBots.size());
            activeSessionsByBots.forEach((botId, session) -> {
                buffer.writeShortString(botId.toString());
                buffer.writeShortString(session.id().toString());
            });
        } else {
            buffer.writeU1(0);
        }

        File file = new File(CodeBotsPlugin.inst().getDataFolder(), "editor.dat");
        try {
            Files.write(file.toPath(), buffer.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
