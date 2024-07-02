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

public class r11 implements SelectController, Initializable {
    @FXML
    public Button listButton;
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
        if (choiceBox.getSelectionModel().isEmpty()) {
            showAlert("empty!", "Fill in required fields");
        } else if (Medicament.get(choiceBox.getValue().toString()) == 0) {
            String sql = "SELECT med.title, s.price, s.ACTUAL_BALANCE as count, s.storage_id " +
                    "FROM medicament med " +
                    "INNER JOIN storage s ON s.medicament_id = med.medicament_id";
            showResult(sql);
        } else {

            String sql = "SELECT med.title, s.price, s.ACTUAL_BALANCE as count, s.storage_id " +
                    "FROM medicament med " +
                    "INNER JOIN storage s ON s.medicament_id = med.medicament_id " +
                    "WHERE med.medicament_id = ?";

            showResult(sql, Medicament.get(choiceBox.getValue().toString()));
        }
    }

}
