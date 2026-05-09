package com.github.alantr7.codebots.world;

import com.github.alantr7.bytils.buffer.ByteArrayReader;
import com.github.alantr7.bytils.buffer.ByteArrayWriter;
import com.github.alantr7.codebots.utils.StringPool;
import com.github.alantr7.codebots.world.structure.StructureInstance;
import org.joml.Vector2i;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BotsRegion {

    public static final int SECTION_LENGTH_KEYS             = 2048;
    public static final int SECTION_LENGTH_CHUNKS_OFFSETS   = 3072;
    public static final int SECTION_LENGTH_CHUNK_DATA       = 16_384;

    public static final int CHUNKS_IN_REGION = 1024;

    public final BotsWorld world;
    public final File regionFile;

    public final int x, z;

    public final StringPool strings = new StringPool();
    public final Map<Vector2i, BotsChunk> chunks = new HashMap<>();

    public byte[] header = new byte[1 + SECTION_LENGTH_KEYS + SECTION_LENGTH_CHUNKS_OFFSETS];

    public static final byte FILE_FORMAT_VERSION = 1;

    public BotsRegion(BotsWorld world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
        this.regionFile = new File(world.botsRegionsDirectory, "r." + x + "." + z + ".dat");
    }

    private void createFile() {
        try {
            RandomAccessFile file = new RandomAccessFile(regionFile, "rw");
            header[0] = FILE_FORMAT_VERSION;
            file.write(FILE_FORMAT_VERSION);
            file.setLength(header.length);
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void load() throws Exception {
        if (!regionFile.exists())
            return;

        RandomAccessFile raf = new RandomAccessFile(regionFile, "r");
        raf.readFully(header);

        if (header[0] != FILE_FORMAT_VERSION) {
            byte oldFormat = header[0];
            header = new byte[1 + SECTION_LENGTH_KEYS + SECTION_LENGTH_CHUNKS_OFFSETS];
            raf.close();

            createFile();

            throw new Exception("Region " + x + ", " + z + " is saved in format " + oldFormat + " but current is " + FILE_FORMAT_VERSION + ". Skipping it as converters do not exist yet.");
        }

        // Read string pool
        for (int i = 1; i <= SECTION_LENGTH_KEYS; i += 16) {
            if (header[i] == 0)
                break;

            String rawString = new String(header, i, 16);
            strings.pool(rawString.substring(0, rawString.indexOf(0)));
        }

        raf.close();
    }

    public void save() throws Exception {
        boolean hasDirtyChunks = false;
        for (BotsChunk chunk : chunks.values()) {
            if (chunk.isUnsaved) {
                hasDirtyChunks = true;
                break;
            }
        }

        if (!hasDirtyChunks)
            return;

        if (!regionFile.exists()) {
            createFile();
        }

        RandomAccessFile raf = new RandomAccessFile(regionFile, "rw");
        for (BotsChunk chunk : chunks.values()) {
            saveChunk(raf, chunk);
        }

        raf.close();
    }

    private void loadChunk(RandomAccessFile raf, BotsChunk chunk) throws Exception {
        int index = (chunk.position.x & 31) + (chunk.position.y & 31) * 32;
        int regionFileOffset = ByteArrayReader.toInt(header, 1 + SECTION_LENGTH_KEYS + index * 3, 3);

        // Chunk is empty
        if (regionFileOffset == 0) {
            return;
        }

        raf.seek(regionFileOffset);

        int chunkSize = ByteArrayReader.toInt(new byte[] { raf.readByte(), raf.readByte() });
        chunk.size = chunkSize;

        byte[] buffer = new byte[chunkSize];
        raf.readFully(buffer);

        ByteArrayReader reader = new ByteArrayReader(buffer);
        while (reader.hasNext()) {
            int basePointer = reader.getPointer();
            int structureId = ByteArrayReader.toInt(reader.readBytes(2));

            // Occupation
            if (structureId == 1) {
                int packedXZ = reader.readU1() & 0xff;
                int x = (packedXZ >> 4) & 0x0f;
                int z = packedXZ & 0x0f;
                int y = ByteArrayReader.toInt(reader.readBytes(2));

                int packedStructureXZ = reader.readU1() & 0xff;
                int sX = ((packedStructureXZ >> 4)) - 7;
                int sZ = (packedStructureXZ & 0x0f) - 7;
                int sY = ByteArrayReader.toInt(reader.readBytes(2));

                BlockLocation occupation = new BlockLocation(world, x + (chunk.position.x << 4), y, z + (chunk.position.y << 4));
                BlockLocation structureLocation = occupation.getRelative(sX, sY - y, sZ);

                chunk.occupations.put(occupation, structureLocation);
            }

            // Structure
            else {
                int length = ByteArrayReader.toInt(reader.readBytes(2));
                try {
                    StructureInstance structure = StructureInstance.fromBytes(this, chunk, reader, structureId);
                    if (structure != null) {
                        structure.setup();
                        StructureInstance.place(structure);
                        chunk._placeStructureWithOccupations(structure);

                        if (structure.isCorrupted) {
                            System.err.println("Corrupted structure, but still loaded it.");
                        }
                    } else {
                        System.err.println("Could not load structure " + structureId + " in " + chunk.position.x + ", " + chunk.position.y + " at offset #" + basePointer);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Corrupted structure in region " + x + ", " + z + " with ID: " + structureId);
                }
                reader.setPointer(basePointer + length + 4);
            }
        }
    }

    public Set<Integer[]> getExistingChunksPositions() {
        Set<Integer[]> set = new LinkedHashSet<>();
        for (int i = 0; i < SECTION_LENGTH_CHUNKS_OFFSETS; i += 3) {
            int offset = ByteArrayReader.toInt(header, 1 + SECTION_LENGTH_KEYS + i, 3);
            if (offset == 0)
                continue;

            int x = (i / 3) & 31;
            int z = (i / 3) >> 5;
            set.add(new Integer[] { x, z });
        }

        return set;
    }

    // Finds empty space to save a NEW chunk
    // Go through header and find unsaved chunk. Check if that location is busy.
    private int _allocateChunkData(int chunkIndex) {
        int end = (int) regionFile.length();
        System.arraycopy(ByteArrayWriter.toBytes(end, 3), 0, header, 1 + SECTION_LENGTH_KEYS + chunkIndex * 3, 3);
        return end;
    }

    private void saveChunk(RandomAccessFile raf, BotsChunk chunk) throws Exception {
        if (!chunk.isUnsaved)
            return;

        // Find chunk's section in the file
        int index = (chunk.position.x & 31) + (chunk.position.y & 31) * 32;
        int regionFileOffset = ByteArrayReader.toInt(header, 1 + SECTION_LENGTH_KEYS + index * 3, 3);
        if (regionFileOffset == 0) {
            regionFileOffset = _allocateChunkData(index);

            raf.seek(1 + SECTION_LENGTH_KEYS + index * 3);
            raf.write(ByteArrayWriter.toBytes(regionFileOffset, 3));
        }

        raf.seek(regionFileOffset + 2);

        ByteArrayWriter writer = new ByteArrayWriter(SECTION_LENGTH_CHUNK_DATA);
        int keysLength = strings.getSize();

        // Save structures
        for (StructureInstance structure : chunk.structures.values()) {
            structure.save(writer, strings);
        }

        // Save occupations that don't belong to this chunk
        for (Map.Entry<BlockLocation, BlockLocation> entry : chunk.occupations.entrySet()) {
            BlockLocation occupation = entry.getKey();
            BlockLocation structureLocation = entry.getValue();

            if (chunk.contains(structureLocation))
                continue;

            writer.writeU2(1);
            writer.writeU1(((occupation.x & 15) << 4) | (occupation.z & 15));
            writer.writeU2(occupation.y);

            writer.writeU1(((structureLocation.x - occupation.x + 7) << 4) | (structureLocation.z - occupation.z + 7));
            writer.writeU2(structureLocation.y);
        }

        raf.write(writer.getBuffer());
        int chunkSize = writer.getPointer();

        chunk.size = chunkSize;

        // Save new keys
        if (keysLength < strings.getSize()) {
            for (int i = keysLength; i < strings.getSize(); i++) {
                raf.seek(1 + i * 16L);

                String key = strings.at(i);
                if (key.length() > 16) {
                    throw new Exception("Key length must be shorter than or equal 16.");
                }
                raf.write(strings.at(i).getBytes(StandardCharsets.UTF_8));
            }
        }

        // Save chunk size
        raf.seek(regionFileOffset);
        raf.write(ByteArrayWriter.toBytes(chunkSize, 2));

        chunk.isUnsaved = false;
    }

    BotsChunk getOrLoadChunk(int absoluteX, int absoluteZ, boolean virtual) {
        BotsChunk chunk = chunks.get(new Vector2i(absoluteX, absoluteZ));
        if (chunk != null) {
            return chunk;
        }

        chunk = new BotsChunk(world, new Vector2i(absoluteX, absoluteZ));
        chunks.put(chunk.position, chunk);

        if (!regionFile.exists())
            return chunk;

        try {
            RandomAccessFile raf = new RandomAccessFile(regionFile, "r");
            loadChunk(raf, chunk);
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return chunk;
    }

    public Collection<BotsChunk> getLoadedChunks() {
        return chunks.values();
    }

}
