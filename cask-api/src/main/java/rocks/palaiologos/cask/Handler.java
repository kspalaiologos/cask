package rocks.palaiologos.cask;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Handler extends URLStreamHandler {
    private String entryName;
    private int caskCode;

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        String spec = url.toString().substring(7);

        int separator = spec.indexOf("/!");

        if (separator == -1) {
            throw new MalformedURLException("no /! found in url spec:" + spec);
        }

        caskCode = Integer.parseInt(spec.substring(1, separator++));

        /* if ! is the last letter of the innerURL, entryName is null */
        if (++separator != spec.length()) {
            entryName = spec.substring(separator);
            entryName = URLDecoder.decode(entryName, StandardCharsets.UTF_8);
        }

        return new URLConnection(url) {
            @Override
            public void connect() throws IOException { }

            @Override
            public InputStream getInputStream() throws IOException {
                synchronized (CaskClassLoader.instances) {
                    CaskClassLoader.instances.clean();
                    for(var obj : CaskClassLoader.instances.values())
                        if(obj.hashCode() == caskCode)
                            return ((CaskClassLoader) obj).getResourceAsStream(entryName);
                }
                throw new IOException("CaskClassLoader not found");
            }
        };
    }
}
