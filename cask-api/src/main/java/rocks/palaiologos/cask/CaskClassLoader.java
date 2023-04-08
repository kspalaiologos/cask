package rocks.palaiologos.cask;

import rocks.palaiologos.cask.xz.XZInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class CaskClassLoader extends ClassLoader {
    private final SevenZFile cask;

    public CaskClassLoader(String caskFile) throws IOException {
        super(CaskClassLoader.class.getClassLoader());
        SeekableInMemoryByteChannel caskChannel = new SeekableInMemoryByteChannel(new URL(caskFile).openStream().readAllBytes());
        cask = new SevenZFile(caskChannel);
    }

    public CaskClassLoader(URL caskFile) throws IOException {
        super(CaskClassLoader.class.getClassLoader());
        SeekableInMemoryByteChannel caskChannel = new SeekableInMemoryByteChannel(caskFile.openStream().readAllBytes());
        cask = new SevenZFile(caskChannel);
    }

    public CaskClassLoader(InputStream caskFile) throws IOException {
        super(CaskClassLoader.class.getClassLoader());
        SeekableInMemoryByteChannel caskChannel = new SeekableInMemoryByteChannel(caskFile.readAllBytes());
        cask = new SevenZFile(caskChannel);
    }

    @Override
    public Class findClass(String name) throws ClassNotFoundException {
        try {
            byte[] b = loadClassFromFile(name);
            return defineClass(name, b, 0, b.length);
        } catch (IOException e) {
            throw new ClassNotFoundException("Could not find class " + name, e);
        }
    }

    private byte[] loadClassFromFile(String fileName) throws IOException, ClassNotFoundException {
        // Expecting fileName to be like org.example.Main
        String path = fileName.replace('.', '/') + ".class";
        for(var entry : cask.getEntries())
            if(entry.getName().equals(path))
                return cask.getInputStream(entry).readAllBytes();
        throw new ClassNotFoundException("Could not find class " + fileName);
    }
}
