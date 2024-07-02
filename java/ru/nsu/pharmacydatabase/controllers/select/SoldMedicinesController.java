package ru.nsu.pharmacydatabase.controllers.select;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import ru.nsu.pharmacydatabase.utils.DBInit;

import java.net.URL;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SoldMedicinesController implements SelectController, Initializable {
    @FXML
    public Button listButton;
    @FXML
    TextField startDate;
    @FXML
    TextField endDate;
    @FXML
    public ChoiceBox choiceBox;
    private DBInit dbInit;
    private ObservableList<String> items = FXCollections.<String>observableArrayList();
    private Map<String, Integer> Medicament;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbInit = new DBInit(connection);
        choiceBox.setItems(items);

        try {
            ResultSet set = connection.executeQueryAndGetResult("select * from medicament");
            Medicament = new HashMap<>();
            items.clear();
            items.add("all");
            Medicament.put("all", 0);
            if (set != null) {
                while (set.next()) {
                    String name = set.getString(2);
                    Integer id = set.getInt(1);
                    Medicament.put(name, id);
                    items.add(name);
                }
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void listButtonTapped() {
        if (startDate.getText().isEmpty() || endDate.getText().isEmpty()) {
            showAlert("empty!", "Fill in required fields");
        } else if (Medicament.get(choiceBox.getValue().toString()) == 0) {
            String sql = "SELECT title, COUNT(*) AS volume " +
                    "FROM medicament med " +
                    "INNER JOIN prescription pr ON pr.med_id = med.medicament_id " +
                    "LEFT OUTER JOIN order_ ord ON ord.PRESCRIPTION_ID = pr.PRESCRIPTION_ID " +
                    "WHERE ord.start_date > TO_DATE(?, 'DD-MM-YYYY') " +
                    "AND ord.start_date < TO_DATE(?, 'DD-MM-YYYY') " +
                    "GROUP BY title " +
                    "HAVING COUNT(*) > 0 " +
                    "ORDER BY volume DESC";

            showResult(sql, startDate.getText(), endDate.getText());
        } else {

            String sql = "SELECT title, COUNT(*) AS volume " +
                    "FROM medicament med " +
                    "INNER JOIN prescription pr ON pr.med_id = med.medicament_id " +
                    "LEFT OUTER JOIN order_ ord ON ord.PRESCRIPTION_ID = pr.PRESCRIPTION_ID " +
                    "WHERE ord.start_date > TO_DATE(?, 'DD-MM-YYYY') " +
                    "AND ord.start_date < TO_DATE(?, 'DD-MM-YYYY') " +
                    "AND med.MEDICAMENT_ID = ? " +
                    "GROUP BY title " +
                    "HAVING COUNT(*) > 0 " +
                    "ORDER BY volume DESC";

            showResult(sql, startDate.getText(), endDate.getText(), Medicament.get(choiceBox.getValue().toString()));
        }
    }

}
