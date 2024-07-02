package ru.nsu.pharmacydatabase.utils;

public enum Tables {
    provider(new Table(
            "PROVIDER",
            "/ru/nsu/pharmacydatabase/windows/insert/provider.fxml"
    )),
    medicament(new Table(
            "MEDICAMENT",
            "/ru/nsu/pharmacydatabase/windows/insert/medicament.fxml"
    )),
    request(new Table("REQUEST",
            "/ru/nsu/pharmacydatabase/windows/insert/request.fxml"
    )),
    medicament_type(new Table(
            "MEDICAMENT_TYPE",
            "/ru/nsu/pharmacydatabase/windows/insert/medicament_type.fxml"
    )),
    patient(new Table(
            "PATIENT",
            "/ru/nsu/pharmacydatabase/windows/insert/patient.fxml"
    )),
    doctor(new Table(
            "DOCTOR",
            "/ru/nsu/pharmacydatabase/windows/insert/doctor.fxml"
    )),
    order(new Table(
            "ORDER_",
            "/ru/nsu/pharmacydatabase/windows/insert/order.fxml"
    )),

    preparation_technology(new Table(
            "PREPARATION_TECHNOLOGY",
            "/ru/nsu/pharmacydatabase/windows/insert/preparation_technology.fxml"
    )),

    pharmacy_employee(new Table(
            "PHARMACY_EMPLOYEE",
            "/ru/nsu/pharmacydatabase/windows/insert/employee.fxml"
    )),




    storage(new Table(
            "STORAGE",
            "/ru/nsu/pharmacydatabase/windows/insert/storage.fxml"
    )),


    request_structure(new Table(
            "REQUEST_STRUCTURE",
            "/ru/nsu/pharmacydatabase/windows/insert/request_structure.fxml"
    )),

    medicament_preparation_relationship(new Table(
            "MEDICAMENT_PREPARATION_RELATIONSHIP",
            "/ru/nsu/pharmacydatabase/windows/insert/medicament_preparation_relationship.fxml"
    )),


    prescription(new Table(
            "PRESCRIPTION",
            "/ru/nsu/pharmacydatabase/windows/insert/prescription.fxml"
    ));

    private Table table;

    Tables(Table table) {
        this.table = table;
    }

    public String getWindowName() {
        return table.getWindowName();
    }

    String getName() {
        return table.getName();
    }

    public static Tables getTableByName(String name) {
        for (Tables table: values()) {
            if (table.getName().equals(name)) {
                return table;
            }
        }
        throw new IllegalArgumentException("No enum found with name: [" + name + "]");
    }
}
