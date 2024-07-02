package ru.nsu.pharmacydatabase.controllers.filter;

public enum Filters {
    order(new Filter(
            "order_",
            "/ru/nsu/pharmacydatabase/windows/filter/order_filter.fxml"
    ));

    private final Filter filter;

    Filters(Filter filter) {
        this.filter = filter;
    }

    public String getWindowName() {
        return filter.getWindowName();
    }

    String getName() {
        return filter.getName();
    }

    public static Filters getFilterByName(String name) {
        for (Filters filters : values()) {
            if (filters.getName().equals(name)) {
                return filters;
            }
        }
        throw new IllegalArgumentException("No enum found with name: [" + name + "]");
    }
}
