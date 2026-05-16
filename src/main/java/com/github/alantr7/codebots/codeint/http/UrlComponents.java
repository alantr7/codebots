package com.github.alantr7.codebots.codeint.http;

public record UrlComponents(String protocol, String host, String path) {

    public static final String ANY = "*";

    public boolean isMatch(String url) {
        UrlComponents target = fromUrl(url);
        if (target == null)
            return false;

        // match protocol
        if (!protocol.equals(target.protocol))
            return false;

        // match hostname
        String[] hostParts = host.split("\\.");
        String[] targetHostParts = target.host.split("\\.");
        if (hostParts.length != targetHostParts.length)
            return false;

        for (int i = 0; i < hostParts.length; i++) {
            if (!hostParts[i].equals(ANY) && !targetHostParts[i].equals(hostParts[i]))
                return false;
        }

        // match path
        String[] pathParts = path.substring(1).split("/");
        String[] targetPathParts = target.path.substring(1).split("/");

        for (int i = 0; i < pathParts.length; i++) {
            if (i >= targetPathParts.length)
                return false;

            if (pathParts[i].equals(ANY))
                return true;

            if (!pathParts[i].equals(targetPathParts[i]))
                return false;
        }

        return true;
    }

    public static UrlComponents fromUrl(String url) {
        String protocol;
        int protocolPosition = url.indexOf("://");
        if (protocolPosition != -1) {
            protocol = url.substring(0, protocolPosition);
            url = url.substring(protocolPosition + 3);
        } else {
            protocol = "https";
        }

        String hostname;
        String path;
        int pathPosition = url.indexOf('/');
        if (pathPosition != -1) {
            hostname = url.substring(0, pathPosition);
            int parametersPosition = url.indexOf('?', pathPosition + 1);
            if (parametersPosition != -1) {
                path = url.substring(pathPosition, parametersPosition);
            } else {
                path = url.substring(pathPosition);
            }
        } else {
            hostname = url;
            path = "/";
        }

        return new UrlComponents(protocol, hostname, path);
    }

}
