package com.github.alantr7.codebots.codeint.http;

import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.CodeBotsPlugin;
import org.bukkit.Bukkit;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Singleton
public class HttpManager {

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    private final Map<Integer, Response<?>> responses = new HashMap<>();

    private int nextResponseId = 1;

    public CompletableFuture<StringResponse> getString(String url) {
        int responseId = nextResponseId++;
        return CompletableFuture.supplyAsync(() -> {
            StringResponse stringResponse;
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .GET()
                        .uri(new URI(url))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                stringResponse = new StringResponse(responseId, response, response.body(), null);
            } catch (Exception e) {
                stringResponse = new StringResponse(responseId, null, null, e);
            }

            StringResponse finalResponse = stringResponse;
            Bukkit.getScheduler().runTask(CodeBotsPlugin.inst(), () -> responses.put(responseId, finalResponse));
            return stringResponse;
        });
    }

    public CompletableFuture<JsonResponse> getJson(String url) {
        int responseId = nextResponseId++;
        return CompletableFuture.supplyAsync(() -> {
            JsonResponse jsonResponse;
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .GET()
                        .uri(new URI(url))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                try {
                    jsonResponse = new JsonResponse(responseId, response, new JSONParser().parse(response.body()), null);
                } catch (Exception e) {
                    jsonResponse = new JsonResponse(responseId, response, null, null);
                }
            } catch (Exception e) {
                jsonResponse = new JsonResponse(responseId, null, null, e);
            }

            JsonResponse finalResponse = jsonResponse;
            Bukkit.getScheduler().runTask(CodeBotsPlugin.inst(), () -> responses.put(responseId, finalResponse));
            return jsonResponse;
        });
    }

    public Response<?> getResponse(int id) {
        return responses.get(id);
    }

}
