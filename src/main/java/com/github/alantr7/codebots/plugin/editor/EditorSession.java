package com.github.alantr7.codebots.plugin.editor;

import com.github.alantr7.codebots.plugin.CodeBotsPlugin;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public final class EditorSession {

    private final UUID id;

    private final String accessToken;

    private final long expiry;

    private String code;

    private int lastChangeId;

    public EditorSession(UUID id, String accessToken, long expiry, String code) {
        this.id = id;
        this.accessToken = accessToken;
        this.expiry = expiry;
        this.code = code;
    }

    public CompletableFuture<Void> fetch() {
        return CompletableFuture.runAsync(() -> {
            CodeBotsPlugin.inst().getSingleton(CodeEditorClient.class).fetchSession(this);
        });
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (EditorSession) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.accessToken, that.accessToken) &&
                this.expiry == that.expiry;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accessToken, expiry);
    }

    @Override
    public String toString() {
        return "EditorSession[" +
                "id=" + id + ", " +
                "accessToken=" + accessToken + ", " +
                "expiry=" + expiry + ']';
    }


}
