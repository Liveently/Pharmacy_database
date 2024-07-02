module ru.nsu.pharmacydatabase {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.oracle.database.jdbc;


    opens ru.nsu.pharmacydatabase to javafx.fxml;
    exports ru.nsu.pharmacydatabase;
    exports ru.nsu.pharmacydatabase.requests;
    opens ru.nsu.pharmacydatabase.requests to javafx.fxml;
    exports ru.nsu.pharmacydatabase.controllers.insert;
    opens ru.nsu.pharmacydatabase.controllers.insert to javafx.fxml;
    exports ru.nsu.pharmacydatabase.controllers.base;
    opens ru.nsu.pharmacydatabase.controllers.base to javafx.fxml;
    exports ru.nsu.pharmacydatabase.utils;
    opens ru.nsu.pharmacydatabase.utils to javafx.fxml;
    exports ru.nsu.pharmacydatabase.controllers.select;
    opens ru.nsu.pharmacydatabase.controllers.select to javafx.fxml;
    exports ru.nsu.pharmacydatabase.controllers.filter;
    opens ru.nsu.pharmacydatabase.controllers.filter to javafx.fxml;

}