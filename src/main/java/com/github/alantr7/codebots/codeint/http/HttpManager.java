package com.github.alantr7.codebots.codeint.http;

import com.github.alantr7.bukkitplugin.annotations.core.Invoke;
import com.github.alantr7.bukkitplugin.annotations.core.InvokePeriodically;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.bytils.buffer.ByteArrayReader;
import com.github.alantr7.bytils.buffer.ByteArrayWriter;
import com.github.alantr7.codebots.CodeBotsPlugin;
import com.github.alantr7.codebots.cbslang.exceptions.ExecutionException;
import com.github.alantr7.codebots.config.Config;
import org.bukkit.Bukkit;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

    public CompletableFuture<StringResponse> getString(String url, UUID processId) throws ExecutionException {
        CompletableFuture<HttpResponse<String>> httpResponseFuture = get(url);
        int responseId = nextResponseId++;

        return httpResponseFuture.thenApply(httpResponse -> {
            StringResponse stringResponse = new StringResponse(responseId, processId, System.currentTimeMillis() + Response.STANDARD_LIFETIME, httpResponse.statusCode(), httpResponse.body(), null);
            Bukkit.getScheduler().runTask(CodeBotsPlugin.inst(), () -> responses.put(responseId, stringResponse));

            return stringResponse;
        }).exceptionally(error -> {
            StringResponse stringResponse = new StringResponse(responseId, processId, System.currentTimeMillis() + Response.STANDARD_LIFETIME, 0, null, error.getMessage());
            Bukkit.getScheduler().runTask(CodeBotsPlugin.inst(), () -> responses.put(responseId, stringResponse));

            return stringResponse;
        });
    }

    public CompletableFuture<JsonResponse> getJson(String url, UUID processId) throws ExecutionException {
        CompletableFuture<HttpResponse<String>> httpResponseFuture = get(url);
        int responseId = nextResponseId++;

        return httpResponseFuture.thenApply(httpResponse -> {
            JsonResponse response = null;
            try {
                response = new JsonResponse(responseId, processId, System.currentTimeMillis() + Response.STANDARD_LIFETIME, httpResponse.statusCode(), httpResponse.body(), new JSONParser().parse(httpResponse.body()), null);
            } catch (Exception e) {
                response = new JsonResponse(responseId, processId, System.currentTimeMillis() + Response.STANDARD_LIFETIME, httpResponse.statusCode(), httpResponse.body(), null, e.getMessage());
            }

            JsonResponse response1 = response;
            Bukkit.getScheduler().runTask(CodeBotsPlugin.inst(), () -> responses.put(responseId, response1));
            return response;
        }).exceptionally(error -> {
            JsonResponse response = new JsonResponse(responseId, processId, System.currentTimeMillis() + Response.STANDARD_LIFETIME, 0, null, null, error.getMessage());
            Bukkit.getScheduler().runTask(CodeBotsPlugin.inst(), () -> responses.put(responseId, response));

            return response;
        });
    }

    public Response<?> getResponse(int id) {
        return responses.get(id);
    }

    public void deleteResponse(int id) {
        responses.remove(id);
    }

    @Invoke(Invoke.Schedule.AFTER_PLUGIN_ENABLE)
    private void loadResponses() {
        File responsesFile = new File(CodeBotsPlugin.inst().getDataFolder(), "http.dat");
        if (!responsesFile.exists())
            return;

        try {
            byte[] bytes = Files.readAllBytes(responsesFile.toPath());
            ByteArrayReader reader = new ByteArrayReader(bytes);
            long time = System.currentTimeMillis();
            while (reader.hasNext()) {
                int handle = reader.readInt();
                UUID processId = new UUID(reader.readLong(), reader.readLong());
                long expiry = reader.readLong();
                int type = reader.readU1();
                int statusCode = reader.readU2();
                String body = reader.readString();

                if (time >= expiry)
                    continue;

                Response<?> response;
                if (type == 1) {
                    response = new StringResponse(handle, processId, expiry, statusCode, body, null);
                } else if (type == 2) {
                    try {
                        response = new JsonResponse(handle, processId, expiry, statusCode, body, new JSONParser().parse(body), null);
                    } catch (Exception e) {
                        response = new JsonResponse(handle, processId, expiry, statusCode, body, null, null);
                    }
                } else continue;

                responses.put(handle, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @InvokePeriodically(delay = 20 * 60, interval = 20 * 60)
    @Invoke(Invoke.Schedule.AFTER_PLUGIN_DISABLE)
    private void saveAndDeleteResponses() {
        long time = System.currentTimeMillis();
        responses.entrySet().removeIf(entry ->
                time >= entry.getValue().expiry
        );

        ByteArrayWriter writer = new ByteArrayWriter();
        responses.forEach((handle, response) -> {
            int typeU1;
            if (response instanceof StringResponse) {
                typeU1 = 1;
            } else if (response instanceof JsonResponse) {
                typeU1 = 2;
            } else return;

            writer.writeInt(handle);
            writer.writeLong(response.processId.getMostSignificantBits());
            writer.writeLong(response.processId.getLeastSignificantBits());
            writer.writeLong(response.expiry);
            writer.writeU1(typeU1);
            writer.writeU2(response.statusCode);
            writer.writeString(response.body);
        });

        File responsesFile = new File(CodeBotsPlugin.inst().getDataFolder(), "http.dat");
        if (responsesFile.exists()) {
            responsesFile.delete();
        }

        try {
            Files.write(responsesFile.toPath(), writer.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Collection<Response<?>> getResponsesByProcess(UUID processId) {
        return responses.values()
                .stream()
                .filter(r -> processId.equals(r.processId))
                .collect(Collectors.toList());
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
