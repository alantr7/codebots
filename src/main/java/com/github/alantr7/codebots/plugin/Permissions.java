package com.github.alantr7.codebots.plugin;

import com.github.alantr7.bukkitplugin.annotations.generative.Permission;

public class Permissions {

    @Permission(allowed = Permission.Allowed.OP)
    public static final String COMMAND_CREATE_BOT = "codebots.command.bot.create";

    @Permission(allowed = Permission.Allowed.OP)
    public static final String COMMAND_DELETE = "codebots.command.bot.delete";

    @Permission(allowed = Permission.Allowed.OP)
    public static final String COMMAND_SELECT = "codebots.command.bot.select";

    @Permission(allowed = Permission.Allowed.OP)
    public static final String COMMAND_SET_SKIN = "codebots.command.bot.setskin";

    @Permission(allowed = Permission.Allowed.OP)
    public static final String COMMAND_EDITOR = "codebots.command.bot.editor";

    @Permission(allowed = Permission.Allowed.OP)
    public static final String COMMAND_TELEPORT = "codebots.command.bot.teleport";

    @Permission(allowed = Permission.Allowed.OP)
    public static final String COMMAND_INVENTORY = "codebots.command.bot.inventory";


    @Permission(allowed = Permission.Allowed.OP)
    public static final String COMMAND_CREATE_MONITOR = "codebots.command.monitor.create";


    @Permission(allowed = Permission.Allowed.OP)
    public static final String COMMAND_CREATE_TRANSMITTER = "codebots.command.transmitter.create";


    @Permission(allowed = Permission.Allowed.OP)
    public static final String COMMAND_RELOAD = "codebots.command.reload";

    @Permission(allowed = Permission.Allowed.OP)
    public static final String ACTION_OPEN_ANY_INVENTORY = "codebots.bot.openany";

}
