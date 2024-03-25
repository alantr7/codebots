package com.github.alantr7.codebots.api.bot;

import com.github.alantr7.codebots.language.runtime.Program;
import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;

import java.util.UUID;

public interface CodeBot {

    UUID getId();

    BlockDisplay getEntity();

    Location getLocation();

    Direction getDirection();

    Program getProgram();

    void setProgram(Program program);

    boolean isActive();

    void setActive(boolean flag);

}
