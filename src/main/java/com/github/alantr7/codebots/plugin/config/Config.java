package com.github.alantr7.codebots.plugin.config;

public class Config {

    // <editor-fold desc="Bots Configuration">
    public static int BOT_MOVEMENT_DURATION = 10;

    public static int BOT_ROTATION_DURATION = 10;

    public static boolean BOT_ALLOW_BLOCK_BREAKING = true;

    public static int BOT_ALLOWED_SCRIPTS = 0;

    public static String BOT_CHAT_FORMAT = "&7[Bot] {message}";

    public static int BOT_MAX_MEMORY_ENTRIES = 32;

    public static int BOT_MAX_LOCAL_PROGRAMS = 4;

    public static final double BOT_STATUS_ENTITY_OFFSET = 1d;

    public static final int BOT_MAX_STATUS_LENGTH = 64;

    public static final boolean BOT_SHOW_CHAT_AS_STATUS = true;

    // </editor-fold>

    public static final int BOT_AUTO_SAVE_COOLDOWN = 5000;

    // <editor-fold desc="Scripts Configuration">
    public static int SCRIPTS_MAX_FUNCTION_CALL_STACK_SIZE = 8;

    public static int SCRIPTS_MAX_VARIABLES_COUNT = 20;

    public static int SCRIPTS_MAX_FUNCTIONS_COUNT = 10;
    // </editor-fold>

    public static String EDITOR_URL = "https://codebots.myqualia.net";


}
