package rocks.palaiologos.cask;

import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.spi.URLStreamHandlerProvider;

public class CaskURLHandlerProvider extends URLStreamHandlerProvider {

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if ("cask".equals(protocol)) {
            return new Handler();
        }
        return null;
    }

}
