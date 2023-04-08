package org.example;

public class DummyClass {
    public static void printClassloaderInfo() {
        System.out.println("Classloader info:");
        System.out.println("  DummyClass: " + DummyClass.class.getClassLoader());
        System.out.println("  Main: " + Main.class.getClassLoader());
    }
}
