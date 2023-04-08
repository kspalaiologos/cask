package org.example;

import java.net.URL;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        DummyClass.printClassloaderInfo();
        URL r = Main.class.getResource("DummyClass.class");
        System.out.println("Resource: " + r);
    }
}