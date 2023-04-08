package rocks.palaiologos.cask;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;

import java.io.*;

public class Main {
    public static void main(String[] args) {
        if(args.length < 4) {
            System.out.println("Usage: java -jar cask-bundler.jar <output> <cask-api> <manifest> <input(s)>");
            System.exit(1);
        }

        // Merge cask-api jar and the temporary cask file.
        JarArchiveOutputStream jarOutputStream;
        try {
            String output = args[0];
            String caskApi = args[1];
            FileOutputStream fos = new FileOutputStream(output);
            jarOutputStream = new JarArchiveOutputStream(fos);
            FileInputStream fis = new FileInputStream(caskApi);
            JarArchiveInputStream caskApiInput = new JarArchiveInputStream(fis);

            JarArchiveEntry entry;
            while((entry = caskApiInput.getNextJarEntry()) != null) {
                jarOutputStream.putArchiveEntry(entry);
                IOUtils.copy(caskApiInput, jarOutputStream);
                jarOutputStream.closeArchiveEntry();
            }

            jarOutputStream.flush();
            caskApiInput.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Add the manifest file.
        try {
            String manifest = args[2];
            FileInputStream manifestInput = new FileInputStream(manifest);
            jarOutputStream.putArchiveEntry(jarOutputStream.createArchiveEntry(new File(manifest), "MANIFEST"));
            byte[] buffer = new byte[4096];
            int len;
            while ((len = manifestInput.read(buffer)) > 0) {
                jarOutputStream.write(buffer, 0, len);
            }
            jarOutputStream.closeArchiveEntry();
            manifestInput.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Start adding individual casks.
        for(int i = 3; i < args.length; i++) {
             try {
                 SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel();
                 SevenZOutputFile outputFile = new SevenZOutputFile(channel);
                 String jarfile = args[i];
                 FileInputStream jarfileInput = new FileInputStream(jarfile);
                 JarArchiveInputStream input = new JarArchiveInputStream(jarfileInput);
                 for (JarArchiveEntry entry = input.getNextJarEntry(); entry != null; entry = input.getNextJarEntry()) {
                     SevenZArchiveEntry sevenZArchiveEntry = new SevenZArchiveEntry();
                     sevenZArchiveEntry.setName(entry.getName());
                     sevenZArchiveEntry.setSize(entry.getSize());
                     sevenZArchiveEntry.setDirectory(entry.isDirectory());
                     sevenZArchiveEntry.setLastModifiedDate(entry.getLastModifiedDate());
                     outputFile.putArchiveEntry(sevenZArchiveEntry);
                     byte[] buffer = new byte[4096];
                     int len;
                     while ((len = input.read(buffer)) > 0) {
                         outputFile.write(buffer, 0, len);
                     }
                     outputFile.closeArchiveEntry();
                 }
                 outputFile.finish();
                 outputFile.close();
                 input.close();

                 jarOutputStream.putArchiveEntry(new JarArchiveEntry(new File(jarfile).getName() + ".class"));
                 jarOutputStream.write(channel.array(), 0, (int) channel.position());
                 jarOutputStream.closeArchiveEntry();
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
        }

        try {
            jarOutputStream.finish();
            jarOutputStream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}