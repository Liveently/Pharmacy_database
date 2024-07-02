package ru.nsu.pharmacydatabase.utils;

public class Table {
    private final String name;
    private final String windowName;

    Table(String name, String windowName) {
        this.name = name;
        this.windowName = windowName;
    }

    String getName() {
        return name;
    }

    String getWindowName() {
        return windowName;
    }
}
