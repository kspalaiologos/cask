package rocks.palaiologos.cask;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class CaskClassLoader extends ClassLoader {
    private class ByteArray {
        public byte[] data;
        public ByteArray(byte[] data) {
            this.data = data;
        }
    }

    private final SevenZFile cask;
    private final HashMap<String, ByteArray> files = new HashMap<>();
    public static final WeakBag instances = new WeakBag();

    public CaskClassLoader(String caskFile) throws IOException {
        super(CaskClassLoader.class.getClassLoader());
        SeekableInMemoryByteChannel caskChannel = new SeekableInMemoryByteChannel(new URL(caskFile).openStream().readAllBytes());
        cask = new SevenZFile(caskChannel);
        cache();
    }

    public CaskClassLoader(URL caskFile) throws IOException {
        super(CaskClassLoader.class.getClassLoader());
        SeekableInMemoryByteChannel caskChannel = new SeekableInMemoryByteChannel(caskFile.openStream().readAllBytes());
        cask = new SevenZFile(caskChannel);
        cache();
    }

    public CaskClassLoader(InputStream caskFile) throws IOException {
        super(CaskClassLoader.class.getClassLoader());
        SeekableInMemoryByteChannel caskChannel = new SeekableInMemoryByteChannel(caskFile.readAllBytes());
        cask = new SevenZFile(caskChannel);
        cache();
    }

    private void cache() throws IOException {
        for(var entry : cask.getEntries()) {
            if(!entry.isDirectory())
                files.put(entry.getName(), new ByteArray(cask.getInputStream(entry).readAllBytes()));
        }
        synchronized (instances) {
            instances.clean();
            instances.add(this);
        }
    }

    @Override
    public Class findClass(String name) throws ClassNotFoundException {
        try {
            byte[] b = loadClassFromFile(name);
            if(b == null)
                return getParent().loadClass(name);
            return defineClass(name, b, 0, b.length);
        } catch (IOException e) {
            throw new ClassNotFoundException("Could not find class " + name, e);
        }
    }

    @Override
    protected URL findResource(String name) {
        ByteArray file = files.get(name);
        if(file == null)
            return getParent().getResource(name);
        try {
            return new URL(null, "cask://c" + hashCode() + "/!" + name);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        ByteArray file = files.get(name);
        if(file != null)
            return new ByteArrayInputStream(file.data);
        return getParent().getResourceAsStream(name);
    }

    private byte[] loadClassFromFile(String fileName) throws IOException {
        // Expecting fileName to be like org.example.Main
        String path = fileName.replace('.', '/') + ".class";
        ByteArray file = files.get(path);
        if(file != null)
            return file.data;
        return null;
    }
}
