package ru.nsu.pharmacydatabase.controllers.filter;

public class Filter {
    private final String name;
    private final String windowName;

    Filter(String name, String windowName) {
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
