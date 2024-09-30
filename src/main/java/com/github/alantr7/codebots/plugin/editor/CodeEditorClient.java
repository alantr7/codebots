package com.github.alantr7.codebots.plugin.editor;

import com.github.alantr7.bukkitplugin.annotations.core.InvokePeriodically;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.plugin.config.Config;
import org.bukkit.Bukkit;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Singleton
public class CodeEditorClient {

    private final HttpClient client;

    public CodeEditorClient() {
        this.client = HttpClient.newHttpClient();
    }

    public CompletableFuture<EditorSession> createSession(byte[] code) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var json = new JSONObject();
                json.put("file", new String(code));

                var request = HttpRequest.newBuilder()
                        .uri(new URI(Config.EDITOR_URL + "/api/create-session"))
                        .POST(HttpRequest.BodyPublishers.ofString(json.toJSONString()))
                        .setHeader("Content-Type", "application/json")
                        .build();

                var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200)
                    return null;

                var responseSession = (JSONObject) new JSONParser().parse(response.body());
                return new EditorSession(
                        UUID.fromString((String) responseSession.get("id")),
                        (String) responseSession.get("access_token"),
                        (Long) responseSession.get("expires_at"),
                        (String) responseSession.get("content")
                );
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public CompletableFuture<Void> fetchSession(EditorSession session) {
        return CompletableFuture.runAsync(() -> {
            try {
                var request = HttpRequest.newBuilder()
                        .uri(new URI(Config.EDITOR_URL + "/api/sessions/" + session.id().toString()))
                        .header("Cookie", "access_token=" + session.accessToken())
                        .GET()
                        .build();

                var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200)
                    return;

                var responseSession = (JSONObject) new JSONParser().parse(response.body());
                var newSession = new EditorSession(
                        UUID.fromString((String) responseSession.get("id")),
                        (String) responseSession.get("access_token"),
                        (Long) responseSession.get("expires_at"),
                        (String) responseSession.get("content")
                );

                session.setCode(newSession.getCode());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}