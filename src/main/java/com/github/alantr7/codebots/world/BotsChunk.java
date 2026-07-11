package com.github.alantr7.codebots.world;

import com.github.alantr7.codebots.world.structure.StructureInstance;
import com.github.alantr7.codebots.world.structure.Tickable;
import lombok.Getter;
import org.joml.Vector2i;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BotsChunk {

    public final BotsWorld world;

    public final Vector2i position;

    @Getter
    int size;

    public boolean isUnsaved;

    final Map<BlockLocation, StructureInstance> structures = new HashMap<>();

    final Map<BlockLocation, Tickable> tickableStructures = new HashMap<>();

    final Map<BlockLocation, BlockLocation> occupations = new HashMap<>();

    public BotsChunk(BotsWorld world, Vector2i position) {
        this.world = world;
        this.position = position;
    }

    protected void _registerStructure(StructureInstance structure) {
        structures.put(structure.location, structure);
        if (structure instanceof Tickable tickable)
            tickableStructures.put(structure.location, tickable);
    }

    protected void _unregisterStructure(StructureInstance structure) {
        structures.remove(structure.location);
        tickableStructures.remove(structure.location);
    }

    protected void _placeStructureWithOccupations(StructureInstance structure) {
        _registerStructure(structure);
        byte[] bounds = structure.getCollisionVectors();
        for (int i = 0; i < bounds.length; i += 3) {
            BlockLocation relative = structure.location.getRelative(bounds[i], bounds[i + 1], bounds[i + 2]);
            if (contains(relative)) {
                occupations.put(relative, structure.location);
            }
        }
    }

    protected void makePhysical() {
        for (StructureInstance structure : structures.values()) {
            structure.makePhysical();
            if (structure instanceof Tickable tickable)
                tickableStructures.put(structure.location, tickable);
        }
    }

    public boolean contains(BlockLocation location) {
        return location.x >> 4 == position.x && location.z >> 4 == position.y;
    }

    public Collection<StructureInstance> getStructures() {
        return structures.values();
    }

    public Collection<BlockLocation> getOccupations() {
        return occupations.values();
    }

}
