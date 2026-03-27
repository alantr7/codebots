package com.github.alantr7.codebots.plugin.editor;

import com.github.alantr7.bukkitplugin.annotations.core.Inject;
import com.github.alantr7.bukkitplugin.annotations.core.Invoke;
import com.github.alantr7.bukkitplugin.annotations.core.InvokePeriodically;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Singleton
public class CodeEditorClient {

    private final HttpClient client;

    private final Map<UUID, EditorSession> activeSessions = new HashMap<>();

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

    @InvokePeriodically(interval = 20 * 60 * 60)
    void renewAccessToken() {
        fetchAccessToken();
    }

}
