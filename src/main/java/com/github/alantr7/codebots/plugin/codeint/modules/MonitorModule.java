package com.github.alantr7.codebots.plugin.codeint.modules;

import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.errors.Assertions;
import com.github.alantr7.codebots.language.runtime.errors.exceptions.ExecutionException;
import com.github.alantr7.codebots.language.runtime.modules.NativeModule;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.monitor.CraftMonitor;
import com.github.alantr7.codebots.plugin.bot.CraftCodeBot;
import com.github.alantr7.codebots.plugin.data.MonitorManager;

public class MonitorModule extends NativeModule {

    public MonitorModule(Program program) {
        super(program);
        init();
    }

    private void init() {
        registerFunction("connect", args -> {
            Assertions.expectArguments(args, String.class);
            CraftCodeBot bot = (CraftCodeBot) program.getExtra("bot");
            CraftMonitor monitor = (CraftMonitor) CodeBotsPlugin.inst().getSingleton(MonitorManager.class).getMonitor((String) args[0]);

            if (monitor == null)
                throw new ExecutionException("Could not connect to monitor. Make sure the ID is correct.");

            if (monitor.getConnectedBot() != bot && monitor.getConnectedBot() != null && ((CraftCodeBot) monitor.getConnectedBot()).getMonitor().getId().equals(monitor.getId()))
                throw new ExecutionException("Could not connect to monitor. It is connect to another bot.");

            monitor.setConnectedBot(bot);
            return null;
        });

        registerFunction("print", args -> {
            Assertions.expectArguments(args, String.class);
            CraftCodeBot bot = (CraftCodeBot) program.getExtra("bot");
            CraftMonitor monitor = bot.getMonitor();

            if (monitor == null)
                throw new ExecutionException("Monitor is not connected. Make sure you connected this bot to a monitor by using `connect` command.");

            monitor.write((String) args[0]);
            return null;
        });
    }

}
