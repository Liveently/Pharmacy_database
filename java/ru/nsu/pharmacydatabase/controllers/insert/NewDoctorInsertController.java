package ru.nsu.pharmacydatabase.controllers.insert;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableStringValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.nsu.pharmacydatabase.utils.DBInit;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class NewDoctorInsertController implements Initializable, InsertController {
    private DBInit dbInit;
    private ChangeListener listener;
    private ObservableStringValue name = new SimpleStringProperty("");
    private InsertMode insertMode = InsertMode.insert;
    private String item;
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
    }

    @Override
    public void setItem(String item) {
        this.item = item;
        insertButton.setText("Изменить");
        String firstname = DBInit.getSubstring(" DOCTOR_FIRSTNAME=", "DOCTOR_FIRSTNAME=", item);
        String surname = DBInit.getSubstring(" DOCTOR_SURNAME=", "DOCTOR_SURNAME=", item);
        firstnameField.setText(firstname);
        surnameField.setText(surname);
    }

    public void insertButtonTapped(ActionEvent event) throws IOException {
        if (firstnameField.getText().isEmpty() || surnameField.getText().isEmpty()) {
            showAlert("empty!", "Fill in required fields");
        } else {
            String firstname = firstnameField.getText();
            String surname = surnameField.getText();

            if (insertMode == InsertMode.insert) {
                dbInit.insertDoctor(firstname, surname);
            } else {
                int id = DBInit.getIdFrom(item);
                dbInit.updateDoctor(id, firstname, surname);
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();

       //     FXMLLoader loader = new FXMLLoader();
//            Parent root = loader.load(getClass().getResourceAsStream("/ru/nsu/pharmacydatabase/windows/insert/prescription.fxml"));
//            primaryStage.setScene(new Scene(root));

            //listener.changed(name, "", name);
        }
    }
}
