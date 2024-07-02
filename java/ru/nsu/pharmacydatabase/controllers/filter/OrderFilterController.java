package ru.nsu.pharmacydatabase.controllers.filter;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import ru.nsu.pharmacydatabase.Main;
import ru.nsu.pharmacydatabase.utils.Connection;
import ru.nsu.pharmacydatabase.utils.DBInit;

import java.net.URL;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class OrderFilterController implements Initializable, FilterController {
    @FXML
    private ChoiceBox medicamentChoiceBox;
    @FXML
    private ChoiceBox isReadyChoiceBox;
    @FXML
    private ChoiceBox isReceiveChoiceBox;

    private Connection connection = Main.getConnection();
    private DBInit dbInit;
    private ObservableList<String> itemsMedicament = FXCollections .<String>observableArrayList();
    private ObservableList<String> itemsIsReady = FXCollections .<String>observableArrayList();
    private ObservableList<String> itemsIsReceive = FXCollections .<String>observableArrayList();

    @FXML
    public void searchButtonTapped() {
        String sql = "SELECT ord.order_id, med.title, ord.is_ready, ord.is_received, ord.start_date " +
                "FROM order_ ord " +
                "INNER JOIN prescription pr ON pr.PRESCRIPTION_ID = ord.PRESCRIPTION_ID " +
                "INNER JOIN medicament med ON med.medicament_id = pr.med_id ";

        if ((medicamentChoiceBox.getValue() == null || medicamentChoiceBox.getValue().equals("all")) &&
                isReadyChoiceBox.getSelectionModel().isEmpty() &&
                isReceiveChoiceBox.getSelectionModel().isEmpty()) {
            showResult(sql);
        } else if (!(medicamentChoiceBox.getSelectionModel().isEmpty() || medicamentChoiceBox.getValue().equals("all")) &&
                isReadyChoiceBox.getSelectionModel().isEmpty() &&
                isReceiveChoiceBox.getSelectionModel().isEmpty()) {
            String filter = " WHERE med.title = '" + medicamentChoiceBox.getValue() + "' ";
            showResult(sql + filter);
        } else if ((medicamentChoiceBox.getSelectionModel().isEmpty() || medicamentChoiceBox.getValue().equals("all")) &&
                !isReadyChoiceBox.getSelectionModel().isEmpty() &&
                isReceiveChoiceBox.getSelectionModel().isEmpty()) {
            String filter = " WHERE ord.is_ready = '" + isReadyChoiceBox.getValue() + "' ";
            showResult(sql + filter);
        } else if ((medicamentChoiceBox.getSelectionModel().isEmpty() || medicamentChoiceBox.getValue().equals("all")) &&
                isReadyChoiceBox.getSelectionModel().isEmpty() &&
                !isReceiveChoiceBox.getSelectionModel().isEmpty()) {
            String filter = " WHERE ord.is_received='" + isReceiveChoiceBox.getValue() + "' ";
            showResult(sql + filter);
        } else if (!(medicamentChoiceBox.getSelectionModel().isEmpty() || medicamentChoiceBox.getValue().equals("all")) &&
                !isReadyChoiceBox.getSelectionModel().isEmpty() &&
                isReceiveChoiceBox.getSelectionModel().isEmpty()) {
            String filter = " WHERE ord.is_ready='" + isReadyChoiceBox.getValue() + "' and med.title = '" + medicamentChoiceBox.getValue() + "' ";
            showResult(sql + filter);
        } else if (!(medicamentChoiceBox.getSelectionModel().isEmpty() || medicamentChoiceBox.getValue().equals("all")) &&
                isReadyChoiceBox.getSelectionModel().isEmpty() &&
                !isReceiveChoiceBox.getSelectionModel().isEmpty()) {
            String filter = " WHERE ord.is_received='" + isReceiveChoiceBox.getValue() + "' and med.title = '" + medicamentChoiceBox.getValue() + "' ";
            showResult(sql + filter);
        } else if ((medicamentChoiceBox.getSelectionModel().isEmpty() || medicamentChoiceBox.getValue().equals("all")) &&
                !isReadyChoiceBox.getSelectionModel().isEmpty() &&
                !isReceiveChoiceBox.getSelectionModel().isEmpty()) {
            String filter = " WHERE ord.is_received='" + isReceiveChoiceBox.getValue() + "' and ord.is_ready='" + isReadyChoiceBox.getValue() + "' ";
            showResult(sql + filter);
        } else {
            String filter = " WHERE ord.is_received='" + isReceiveChoiceBox.getValue() + "' and ord.is_ready='" + isReadyChoiceBox.getValue() + "' and med.title = '" + medicamentChoiceBox.getValue() + "' ";
            showResult(sql + filter);
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dbInit = new DBInit(connection);

        medicamentChoiceBox.setItems(itemsMedicament);
        isReadyChoiceBox.setItems(itemsIsReady);
        isReceiveChoiceBox.setItems(itemsIsReceive);
        try {
            ResultSet setMedicament = connection.executeQueryAndGetResult("select distinct title from medicament order by title");
            itemsMedicament.clear();
            itemsMedicament.add("all");

            itemsIsReady.clear();
            itemsIsReady.add("YES");
            itemsIsReady.add("NO");
            itemsIsReceive.clear();
            itemsIsReceive.add("YES");
            itemsIsReceive.add("NO");

            if (setMedicament != null) {
                while (setMedicament.next()) {
                    String name = setMedicament.getString(1);
                    itemsMedicament.add(name);
                }
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }
}
