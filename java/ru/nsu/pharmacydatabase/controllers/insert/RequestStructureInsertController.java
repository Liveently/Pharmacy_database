


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

public class RequestStructureInsertController implements InsertController, Initializable {

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
    private ChoiceBox requestIdField;


    private ObservableList<String> itemsMedicament = FXCollections.<String>observableArrayList();
    private ObservableList<String> itemsRequest = FXCollections.<String>observableArrayList();

    private Map<String, Integer> Medicament;
    private Map<String, Integer> Request;

    @Override
    public void setListener(ChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbInit = new DBInit(connection);
        medIdField.setItems(itemsMedicament);
        requestIdField.setItems(itemsRequest);

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
            ResultSet setRequest = connection.executeQueryAndGetResult("SELECT r.REQUEST_ID, pr.PROVIDER_NAME " +
                                                                                "FROM REQUEST r " +
                                                                                "INNER JOIN PROVIDER pr ON r.PROVIDER_ID = pr.PROVIDER_ID");
            Request = new HashMap<>();
            itemsRequest.clear();
            if (setRequest != null) {
                while (setRequest.next()) {
                    String name = setRequest.getString(2);
                    Integer id = setRequest.getInt(1);
                    Request.put( id + " ("+ name +")" , id);
                    itemsRequest.add(id + " ("+ name +")");
                }
            }

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }


    }

    public void setMode(InsertMode mode) {
        insertMode = mode;
        if (mode == InsertMode.insert) {
            formTitleLabel.setText("Добавить позицию заявки");
            insertButton.setText("Добавить");
        } else {
            formTitleLabel.setText("Изменить позицию заявки");
            insertButton.setText("Изменить");
        }
    }

    public void setItem(String item) {
        this.item = item;
        String medName = DBInit.getSubstring(" TITLE=", "TITLE=", item);
        medIdField.setValue(medName);

        String id = DBInit.getSubstring(" REQUEST_ID=", "REQUEST_ID=", item);
        String namePr = DBInit.getSubstring(" PROVIDER_NAME=", "PROVIDER_NAME=", item);
        requestIdField.setValue(id + " ("+ namePr +")");

        String balance = DBInit.getSubstring(" AMOUNT=", "AMOUNT=", item);


        countField.setText(balance);
    }

    public void insertButtonTapped(ActionEvent actionEvent) {
        int medId = Medicament.get(medIdField.getValue().toString());
        int reqId = Request.get(requestIdField.getValue().toString());
        if (medIdField.getSelectionModel().isEmpty()
                || countField.getText().isEmpty() || requestIdField.getSelectionModel().isEmpty()) {
            showAlert("empty!", "Fill in required fields");
        } else {



            int balance = Integer.valueOf(countField.getText());




            if (insertMode == InsertMode.insert) {
                dbInit.insertRequestStructure(medId,reqId, balance);
            } else {
                int id = DBInit.getIdFrom(item);
                dbInit.updateRequestStructure(id, medId,reqId, balance);
            }
            listener.changed(name, "", name);
            Stage stage = (Stage) insertButton.getScene().getWindow();
            stage.close();
        }

    }
}
