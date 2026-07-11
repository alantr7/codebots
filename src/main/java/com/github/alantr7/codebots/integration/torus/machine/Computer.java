package com.github.alantr7.codebots.integration.torus.machine;

import com.github.alantr7.codebots.integration.torus.CodeBotsTorusIntEntry;
import com.github.alantr7.torus.structure.Structure;
import com.github.alantr7.torus.structure.StructureFlag;
import com.github.alantr7.torus.structure.StructureInstance;
import com.github.alantr7.torus.structure.builder.StructureBodyDef;
import com.github.alantr7.torus.structure.builder.StructurePartDef;
import com.github.alantr7.torus.world.BlockLocation;
import com.github.alantr7.torus.world.Direction;
import com.github.alantr7.torus.world.Pitch;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class Computer extends Structure {

    public Computer() {
        super(CodeBotsTorusIntEntry.addon, "computer", ComputerInstance.class);
        setFlags(StructureFlag.COLLIDABLE | StructureFlag.TICKABLE | StructureFlag.INTERACTABLE);
    }

    @Override
    protected StructureInstance instantiate(@NotNull BlockLocation blockLocation, Direction direction, Pitch pitch) {
        return new ComputerInstance(this, blockLocation, new StructureBodyDef(new StructurePartDef[]{
            new StructurePartDef("base", new Vector3f())
        }), direction);
    }

}
