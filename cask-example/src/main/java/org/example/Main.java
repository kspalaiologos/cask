package org.example;

import java.io.IOException;
import java.net.URL;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Hello world!");
        DummyClass.printClassloaderInfo();
        URL r = Main.class.getResource("DummyClass.class");
        System.out.println("Resource: " + r);
        System.out.println("First byte of content: " + r.openStream().read());
        new Thread(null, new Runnable() {
            @Override
            public void run() {
                System.out.println("Hello from thread! " + DummyTwo.PI);
            }
        }, "mythread", 4194304L).start();
    }
}