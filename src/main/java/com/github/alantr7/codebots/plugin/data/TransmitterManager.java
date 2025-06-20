package com.github.alantr7.codebots.plugin.data;

import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.api.redstone.RedstoneTransmitter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class TransmitterManager {

    final Map<Location, RedstoneTransmitter> transmitters = new HashMap<>();

    public Collection<RedstoneTransmitter> getTransmitters() {
        return transmitters.values();
    }

    public void registerTransmitter(RedstoneTransmitter transmitter) {
        transmitters.put(transmitter.getLocation(), transmitter);
    }

    public void unregisterTransmitter(RedstoneTransmitter transmitter) {
        transmitters.remove(transmitter.getLocation());
    }

    public RedstoneTransmitter getTransmitter(@NotNull Location location) {
        return transmitters.get(location);
    }

}
