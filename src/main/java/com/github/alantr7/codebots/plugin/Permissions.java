package com.github.alantr7.codebots.plugin;

import com.github.alantr7.bukkitplugin.annotations.generative.Permission;

public class Permissions {

    @Permission(allowed = Permission.Allowed.OP)
    public static final String COMMAND_CREATE = "codebots.command.create";

    @Permission(allowed = Permission.Allowed.OP)
    public static final String COMMAND_DELETE = "codebots.command.delete";

    @Permission(allowed = Permission.Allowed.OP)
    public static final String COMMAND_SELECT = "codebots.command.select";

    @Permission(allowed = Permission.Allowed.OP)
    public static final String COMMAND_START = "codebots.command.start";

    @Permission(allowed = Permission.Allowed.OP)
    public static final String COMMAND_PAUSE = "codebots.command.pause";

    @Permission(allowed = Permission.Allowed.OP)
    public static final String COMMAND_STOP = "codebots.command.stop";

    @Permission(allowed = Permission.Allowed.OP)
    public static final String COMMAND_TELEPORT = "codebots.command.teleport";

    @Permission(allowed = Permission.Allowed.OP)
    public static final String COMMAND_INVENTORY = "codebots.command.inventory";

    @Permission(allowed = Permission.Allowed.OP)
    public static final String COMMAND_RELOAD = "codebots.command.reload";

    @Permission(allowed = Permission.Allowed.OP)
    public static final String ACTION_OPEN_ANY_INVENTORY = "codebots.bot.openany";

}
