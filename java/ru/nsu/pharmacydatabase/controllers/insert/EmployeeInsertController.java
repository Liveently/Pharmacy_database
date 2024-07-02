
package ru.nsu.pharmacydatabase.controllers.insert;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableStringValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.nsu.pharmacydatabase.utils.DBInit;

import java.net.URL;
import java.util.ResourceBundle;

public class EmployeeInsertController implements InsertController, Initializable {
    private DBInit dbInit;
    private ChangeListener listener;
    private ObservableStringValue name = new SimpleStringProperty("");
    private InsertMode insertMode = InsertMode.insert;
    private String item;
    @FXML
    private Label formTitleLabel; // Added fx:id reference
    @FXML
    private Button insertButton;
    @FXML
    private TextField firstnameField;
    @FXML
    private TextField surnameField;

    @Override
    public void setListener(ChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbInit = new DBInit(connection);
    }

    public void setMode(InsertMode mode) {
        insertMode = mode;
        if (mode == InsertMode.insert) {
            formTitleLabel.setText("Добавить фармацевта");
            insertButton.setText("Добавить");
        } else {
            formTitleLabel.setText("Изменить фармацевта");
            insertButton.setText("Изменить");
        }
    }

    @Override
    public void setItem(String item) {
        this.item = item;
        insertButton.setText("Изменить");
        String firstname = DBInit.getSubstring(" PHARMACY_EMPLOYEE_FIRSTNAME=", "PHARMACY_EMPLOYEE_FIRSTNAME=", item);
        String surname = DBInit.getSubstring(" PHARMACY_EMPLOYEE_SURNAME=", "PHARMACY_EMPLOYEE_SURNAME=", item);
        firstnameField.setText(firstname);
        surnameField.setText(surname);
    }

    public void insertButtonTapped() {
        if (firstnameField.getText().isEmpty() || surnameField.getText().isEmpty()) {
            showAlert("empty!", "Fill in required fields");
        } else {
            String firstname = firstnameField.getText();
            String surname = surnameField.getText();

            if (insertMode == InsertMode.insert) {
                dbInit.insertPharmacyEmployee(firstname, surname);
            } else {
                int id = DBInit.getIdFrom(item);
                dbInit.updatePharmacyEmployee(id, firstname, surname);
            }
            listener.changed(name, "", name);
            Stage stage = (Stage) insertButton.getScene().getWindow();
            stage.close();
        }
    }
}
