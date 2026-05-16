package com.github.alantr7.codebots.config;

import java.util.Collections;
import java.util.List;

public class Config {

    // <editor-fold desc="Bots Configuration">
    public static int BOT_MOVEMENT_DURATION = 10;

    public static int BOT_ROTATION_DURATION = 10;

    public static boolean BOT_ALLOW_BLOCK_BREAKING = true;

    public static boolean BOT_ALLOW_BLOCK_PLACING = true;

    public static boolean BOT_ALLOW_SOUNDS_PLAYING = true;

    public static int BOT_ALLOWED_SCRIPTS = 0;

    public static String BOT_CHAT_FORMAT = "&7[Bot] {message}";

    public static int BOT_MAX_MEMORY_ENTRIES = 32;

    public static int BOT_MAX_LOCAL_PROGRAMS = 4;

    public static final double BOT_STATUS_ENTITY_OFFSET = 1d;

    public static final int BOT_MAX_STATUS_LENGTH = 64;

    public static final boolean BOT_SHOW_CHAT_AS_STATUS = true;
    // </editor-fold>

    // <editor-fold desc="Scripting Configuration">
    public static boolean SCRIPTS_HTTP_ENABLE_URL_WHITELIST = true;

    public static List<String> SCRIPTS_HTTP_URL_WHITELIST = Collections.emptyList();
    // </editor-fold>

    public static String EDITOR_URL = "https://editor.myqualia.net";


}
