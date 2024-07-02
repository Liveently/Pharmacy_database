package ru.nsu.pharmacydatabase.controllers.insert;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableStringValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.nsu.pharmacydatabase.utils.DBInit;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class MedicamentTypeInsertController implements InsertController, Initializable {
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
    private TextField typeNameField;

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
            formTitleLabel.setText("Добавить тип лекарства");
            insertButton.setText("Добавить");
        } else {
            formTitleLabel.setText("Изменить тип лекарства");
            insertButton.setText("Изменить");
        }
    }

    public void setItem(String item) {
        this.item = item;
        insertButton.setText("Изменить");
        String typeName = DBInit.getSubstring(" TYPE_NAME=", "TYPE_NAME=", item);
        typeNameField.setText(typeName);
    }


    public void insertButtonTapped() throws SQLException {
        if (typeNameField.getText().isEmpty()) {
            showAlert("empty!", "Fill in required fields");
        } else {
            String typeName = typeNameField.getText();

            if (insertMode == InsertMode.insert) {
                dbInit.insertMedicamentType(typeName);
            } else {
                int id = DBInit.getIdFrom(item);


                System.out.println("UPDATE medType      id " + id);
                dbInit.updateMedicamentType(id, typeName);
                }


                listener.changed(name, "", name);
                Stage stage = (Stage) insertButton.getScene().getWindow();
                stage.close();
            }

    }
}

