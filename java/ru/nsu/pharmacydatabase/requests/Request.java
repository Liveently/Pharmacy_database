package ru.nsu.pharmacydatabase.requests;

public class Request {
    private final String name;
    private final String windowName;

    Request(String name, String windowName) {
        this.name = name;
        this.windowName = windowName;
    }

    String getName()
    {
        return name;
    }

    String getWindowName()
    {
        return windowName;
    }
}
