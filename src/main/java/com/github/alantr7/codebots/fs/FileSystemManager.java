package com.github.alantr7.codebots.fs;

import com.github.alantr7.bytils.buffer.ByteArrayReader;
import com.github.alantr7.bytils.buffer.ByteArrayWriter;
import com.github.alantr7.codebots.world.BotsWorld;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class FileSystemManager {

    public static final int FILE_SIZE = /*header:*/32+8 + /*content:*/2048;

    private final File file;

    public FileSystemManager(BotsWorld world) {
        this.file = new File(world.botsDirectory, "filesystems.dat");
    }

    public void load(BotFileSystem fs, long[] pts) {
        if (!file.exists())
            return;

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            for (long pointer : pts) {
                try {
                    BotFile file = load(raf, pointer);
                    if (file != null) {
                        fs.files.put(file.getName(), file);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BotFile load(RandomAccessFile raf, long position) throws Exception {
        if (position < 0)
            return null;

        raf.seek(position);

        byte[] buffer = new byte[FILE_SIZE];
        raf.read(buffer);

        ByteArrayReader reader = new ByteArrayReader(buffer);
        byte[] nameBytes = reader.readBytes(32);
        long lastModified = reader.readLong();
        byte[] content = reader.readBytes(2048);

        BotFile file = new BotFile(this, new String(nameBytes).trim(), content, lastModified);
        file.position = position;

        return file;
    }

    public void save(BotFileSystem fs) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            for (BotFile file : fs.getFiles()) {
                if (file.isUnsaved() || file.getPosition() == -1) {
                    save(raf, file);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save(RandomAccessFile raf, BotFile file) throws Exception {
        if (file.getPosition() == -1) {
            // find new space
            long pos = raf.length();
            if (pos % FILE_SIZE != 0) {
                pos += FILE_SIZE - (FILE_SIZE % pos);
            }

            raf.setLength(pos + FILE_SIZE);
            raf.seek(pos);
            file.position = pos;
        } else {
            raf.seek(file.position);
        }

        System.out.println("saved " + file.getName() + " to position " + file.position);

        ByteArrayWriter writer = new ByteArrayWriter(FILE_SIZE);
        long lastModified = file.getLastModified();

        byte[] name = new byte[32];
        byte[] nameBytes = file.getName().getBytes(StandardCharsets.UTF_8);
        byte[] content = new byte[2048];
        System.arraycopy(nameBytes, 0, name, 0, nameBytes.length);
        System.arraycopy(file.getContent(), 0, content, 0, file.getContent().length);

        writer.writeBytes(name);
        writer.writeLong(lastModified);
        writer.writeBytes(content);
        raf.write(writer.getBuffer());
    }

    public void save() {

    }

    public void delete(BotFile file) {
        if (file.position == -1)
            return;

        try (RandomAccessFile raf = new RandomAccessFile(this.file, "rw")) {
            delete(raf, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete(RandomAccessFile raf, BotFile file) throws Exception {
        if (file.position == -1)
            return;

        raf.seek(file.position);
        raf.writeByte(0);
        file.position = -1;
    }

}
