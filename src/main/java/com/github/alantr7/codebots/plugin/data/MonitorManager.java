package com.github.alantr7.codebots.plugin.data;

import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.api.monitor.Monitor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class MonitorManager {

    final Map<String, Monitor> monitors = new HashMap<>();

    final Map<Location, String> monitorBlocks = new HashMap<>();

    public Monitor getMonitor(String id) {
        return monitors.get(id);
    }

    public Monitor getMonitor(@NotNull Location location) {
        String monitorId = monitorBlocks.get(location);
        if (monitorId == null) return null;

        return monitors.get(monitorId);
    }

    public void registerMonitor(Monitor monitor) {
        monitors.put(monitor.getId(), monitor);
        Location monitorBlockLocation = monitor.getLocation().getBlock().getLocation().clone();
        Direction direction = monitor.getDirection().getLeft();
        for (int i = 0; i < monitor.getWidth(); i++) {
            Location backgroundBlockHorizontalOrigin = monitorBlockLocation.clone();
            for (int j = 0; j < monitor.getHeight(); j++) {
                monitorBlocks.put(backgroundBlockHorizontalOrigin.clone(), monitor.getId());
                backgroundBlockHorizontalOrigin.add(0, 1, 0);
            }

            monitorBlockLocation.add(direction.toVector());
        }
    }

    public void unregisterMonitor(Monitor monitor) {
        monitors.remove(monitor.getId());
        Location monitorBlockLocation = monitor.getLocation().clone();
        Direction direction = monitor.getDirection().getLeft();

        // Can be replaced with do-while
        for (int i = 0; i < monitor.getWidth(); i++) {
            Location backgroundBlockHorizontalOrigin = monitorBlockLocation.clone();
            for (int j = 0; j < monitor.getHeight(); j++) {
                backgroundBlockHorizontalOrigin.getBlock().setType(Material.AIR);
                monitorBlocks.remove(backgroundBlockHorizontalOrigin);

                backgroundBlockHorizontalOrigin.add(0, 1, 0);
            }

            monitorBlockLocation.add(direction.toVector());
        }
    }

    public Collection<Monitor> getMonitors() {
        return monitors.values();
    }

}
