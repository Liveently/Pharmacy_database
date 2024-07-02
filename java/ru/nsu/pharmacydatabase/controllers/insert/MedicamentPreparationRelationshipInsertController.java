


package ru.nsu.pharmacydatabase.controllers.insert;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ru.nsu.pharmacydatabase.utils.DBInit;

import java.net.URL;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MedicamentPreparationRelationshipInsertController implements InsertController, Initializable {

    @FXML
    public TextField countField;

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
    private ChoiceBox medIdField;

    @FXML
    private ChoiceBox preparationTechnologyIdField;


    private ObservableList<String> itemsMedicament = FXCollections.<String>observableArrayList();
    private ObservableList<String> itemsPreparationTechnology = FXCollections.<String>observableArrayList();

    private Map<String, Integer> Medicament;
    private Map<String, Integer> PreparationTechnology;

    @Override
    public void setListener(ChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbInit = new DBInit(connection);
        medIdField.setItems(itemsMedicament);
        preparationTechnologyIdField.setItems(itemsPreparationTechnology);

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

        try {
            ResultSet setRequest = connection.executeQueryAndGetResult("SELECT pr.PREPARATION_TECHNOLOGY_ID, med.title " +
                                                                            "FROM PREPARATION_TECHNOLOGY pr " +
                                                                            "INNER JOIN MEDICAMENT med ON pr.medic_id = med.MEDICAMENT_ID");
            PreparationTechnology = new HashMap<>();
            itemsPreparationTechnology.clear();
            if (setRequest != null) {
                while (setRequest.next()) {
                    String med = setRequest.getString(2);
                    Integer id = setRequest.getInt(1);
                    PreparationTechnology.put( id + " ("+ med +")" , id);
                    itemsPreparationTechnology.add(id + " ("+ med +")");
                }
            }

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }


    }

    public void setMode(InsertMode mode) {
        insertMode = mode;
        if (mode == InsertMode.insert) {
            formTitleLabel.setText("Добавить ингредиент в состав лекарства");
            insertButton.setText("Добавить");
        } else {
            formTitleLabel.setText("Изменить ингредиент лекарства");
            insertButton.setText("Изменить");
        }
    }

    public void setItem(String item) {
        this.item = item;
        String medName = DBInit.getSubstring(" TITLE=", "TITLE=", item);
        medIdField.setValue(medName);

        String namePr = DBInit.getSubstring(" PREPARATION_TECHNOLOGY=", "PREPARATION_TECHNOLOGY=", item);
        preparationTechnologyIdField.setValue( namePr );

        String balance = DBInit.getSubstring(" COUNT=", "COUNT=", item);


        countField.setText(balance);
    }

    public void insertButtonTapped(ActionEvent actionEvent) {
        int medId = Medicament.get(medIdField.getValue().toString());
        int tehId = PreparationTechnology.get(preparationTechnologyIdField.getValue().toString());
        if (medIdField.getSelectionModel().isEmpty()
                || countField.getText().isEmpty() || preparationTechnologyIdField.getSelectionModel().isEmpty()) {
            showAlert("empty!", "Fill in required fields");
        } else {

            int balance = Integer.valueOf(countField.getText());


            if (insertMode == InsertMode.insert) {
                dbInit.insertMedicamentPreparationRelationship(tehId ,medId, balance);
            } else {
                int id = DBInit.getIdFrom(item);
                dbInit.updateMedicamentPreparationRelationship(id,tehId, medId, balance);
            }
            listener.changed(name, "", name);
            Stage stage = (Stage) insertButton.getScene().getWindow();
            stage.close();
        }

    }
}
