

package ru.nsu.pharmacydatabase.controllers.insert;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import ru.nsu.pharmacydatabase.utils.DBInit;

import java.net.URL;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class PreparationTechnologyInsertController implements InsertController, Initializable {
    private DBInit dbInit;
    private ChangeListener listener;
    private ObservableStringValue name = new SimpleStringProperty("");
    private InsertMode insertMode = InsertMode.insert;
    private String item;



    @FXML
    public TextArea recipeField;

    @FXML
    private Label formTitleLabel; // Added fx:id reference
    @FXML
    private Button insertButton;
    @FXML
    private ChoiceBox medIdField;

    private ObservableList<String> itemsMedicament = FXCollections.<String>observableArrayList();
    private Map<String, Integer> Medicament;

    @Override
    public void setListener(ChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbInit = new DBInit(connection);
        medIdField.setItems(itemsMedicament);
        try {
            ResultSet setMedicament = connection.executeQueryAndGetResult("select * from medicament");
            Medicament = new HashMap<>();
            itemsMedicament.clear();
            if (setMedicament != null) {
                while (setMedicament.next()) {
                    String name = setMedicament.getString(2);
                    Integer id = setMedicament.getInt(1);
                    Medicament.put(name, id);
                    itemsMedicament.add(name);
                }
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    public void setMode(InsertMode mode) {
        insertMode = mode;
        if (mode == InsertMode.insert) {
            formTitleLabel.setText("Добавить рецепт");
            insertButton.setText("Добавить");
        } else {
            formTitleLabel.setText("Изменить рецепт");
            insertButton.setText("Изменить");
        }
    }

    public void setItem(String item) {
        this.item = item;
        String medName = DBInit.getSubstring(" TITLE=", "TITLE=", item);
        medIdField.setValue(medName);


        String typeName = DBInit.getSubstring(" METHOD_OF_PREPARATION=", "METHOD_OF_PREPARATION=", item);
        recipeField.setText(typeName);


    }

    public void insertButtonTapped(ActionEvent actionEvent) {
        int medId = Medicament.get(medIdField.getValue().toString());
        if (medIdField.getSelectionModel().isEmpty() || recipeField.getText().isEmpty()) {
            showAlert("empty!", "Fill in required fields");
        } else {
            String recipe = recipeField.getText();
            if (insertMode == InsertMode.insert) {
                dbInit.insertPreparationTechnology(medId, recipe);
            } else {
                int id = DBInit.getIdFrom(item);
                dbInit.updatePreparationTechnology(id, medId, recipe);
            }
            listener.changed(name, "", name);
            Stage stage = (Stage) insertButton.getScene().getWindow();
            stage.close();
        }

    }
}



