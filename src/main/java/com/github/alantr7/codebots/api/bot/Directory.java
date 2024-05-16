package com.github.alantr7.codebots.api.bot;

public enum Directory {

    LOCAL_PROGRAMS,

    SHARED_PROGRAMS;

    public static Directory from(String name) {
        return switch (name) {
            case "local" -> LOCAL_PROGRAMS;
            case "shared" -> SHARED_PROGRAMS;
            default -> null;
        };
    }

    public static Directory fromOrDefault(String name, Directory def) {
        var dir = from(name);
        return dir == null ? def : dir;
    }

}
