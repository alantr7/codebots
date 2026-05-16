package com.github.alantr7.codebots.codeint.http;

import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.CodeBotsPlugin;
import com.github.alantr7.codebots.cbslang.exceptions.ExecutionException;
import com.github.alantr7.codebots.config.Config;
import org.bukkit.Bukkit;
import org.json.simple.parser.JSONParser;

import java.net.URI;
import java.net.URISyntaxException;
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

    private CompletableFuture<HttpResponse<String>> get(String url) throws ExecutionException {
        if (Config.SCRIPTS_HTTP_ENABLE_URL_WHITELIST) {
            if (!isUrlWhitelisted(url))
                throw new ExecutionException("Website is not whitelisted");
        }

        try {
            return client.sendAsync(HttpRequest.newBuilder()
                .GET()
                .uri(new URI(url))
                .build(),
              HttpResponse.BodyHandlers.ofString());
        } catch (URISyntaxException exception) {
            throw new ExecutionException("Invalid URL: " + url);
        }
    }

    public CompletableFuture<StringResponse> getString(String url) throws ExecutionException {
        CompletableFuture<HttpResponse<String>> httpResponseFuture = get(url);
        int responseId = nextResponseId++;

        return httpResponseFuture.thenApply(httpResponse -> {
            StringResponse stringResponse = new StringResponse(responseId, httpResponse, httpResponse.body(), null);
            Bukkit.getScheduler().runTask(CodeBotsPlugin.inst(), () -> responses.put(responseId, stringResponse));

            return stringResponse;
        }).exceptionally(error -> {
            StringResponse stringResponse = new StringResponse(responseId, null, null, error);
            Bukkit.getScheduler().runTask(CodeBotsPlugin.inst(), () -> responses.put(responseId, stringResponse));

            return stringResponse;
        });
    }

    public CompletableFuture<JsonResponse> getJson(String url) throws ExecutionException {
        CompletableFuture<HttpResponse<String>> httpResponseFuture = get(url);
        int responseId = nextResponseId++;

        return httpResponseFuture.thenApply(httpResponse -> {
            JsonResponse response = null;
            try {
                response = new JsonResponse(responseId, httpResponse, new JSONParser().parse(httpResponse.body()), null);
            } catch (Exception e) {
                response = new JsonResponse(responseId, httpResponse, null, e);
            }

            JsonResponse response1 = response;
            Bukkit.getScheduler().runTask(CodeBotsPlugin.inst(), () -> responses.put(responseId, response1));
            return response;
        }).exceptionally(error -> {
            JsonResponse response = new JsonResponse(responseId, null, null, error);
            Bukkit.getScheduler().runTask(CodeBotsPlugin.inst(), () -> responses.put(responseId, response));

            return response;
        });
    }

    public Response<?> getResponse(int id) {
        return responses.get(id);
    }

    public static boolean isUrlWhitelisted(String url) {
        for (String whitelist : Config.SCRIPTS_HTTP_URL_WHITELIST) {
            UrlComponents whitelistComps = UrlComponents.fromUrl(whitelist);
            if (whitelistComps == null)
                return false;

            if (whitelistComps.isMatch(url))
                return true;
        }
        return false;
    }

}
