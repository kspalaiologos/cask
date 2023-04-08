package rocks.palaiologos.cask;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Objects;

public class CaskBootstrap {
    public static void main(String[] args) {
        InputStream manifest = CaskBootstrap.class.getClassLoader().getResourceAsStream("MANIFEST");
        if(manifest == null) {
            throw new RuntimeException("The MANIFEST file for Cask is missing.");
        }
        String data;
        try {
            data = new String(manifest.readAllBytes());
            manifest.close();
        } catch (IOException e) {
            throw new RuntimeException("The MANIFEST file for Cask is not readable.");
        }
        // Primarily just future-proofing.
        HashMap<String, String> manifestMap = new HashMap<>();
        String[] properties = data.replace("\r", "").split("\n");
        for (String property : properties) {
            int index = property.indexOf(":");
            if (index == -1) {
                throw new RuntimeException("Invalid property in MANIFEST: " + property);
            }
            manifestMap.put(property.substring(0, index).trim(), property.substring(index + 1).trim());
        }

        String mainClass = manifestMap.get("Main-Class");
        String caskFile = manifestMap.get("Cask-File");
        if (mainClass == null || caskFile == null) {
            throw new RuntimeException("The MANIFEST file for Cask is missing required properties.");
        }

        try {
            CaskClassLoader caskClassLoader = new CaskClassLoader(Objects.requireNonNull(CaskBootstrap.class.getClassLoader().getResourceAsStream(caskFile)));
            Class<?> mainClassObject = caskClassLoader.loadClass(mainClass);
            mainClassObject.getMethod("main", String[].class).invoke(null, (Object) args);
        } catch (Exception e) {
            throw new RuntimeException("Could not start Cask application.", e);
        }
    }
}
