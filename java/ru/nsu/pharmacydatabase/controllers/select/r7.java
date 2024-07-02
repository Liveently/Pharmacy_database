package ru.nsu.pharmacydatabase.controllers.select;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import ru.nsu.pharmacydatabase.utils.DBInit;

import java.net.URL;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class r7 implements SelectController, Initializable {
    @FXML
    public Button listButton;
    @FXML
    public ChoiceBox choiceBox;
    private DBInit dbInit;
    private ObservableList<String> items = FXCollections.<String>observableArrayList();
    private Map<String, Integer> MedicamentType;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbInit = new DBInit(connection);
        choiceBox.setItems(items);

        try {
            ResultSet set = connection.executeQueryAndGetResult("select * from medicament_type");
            MedicamentType = new HashMap<>();
            items.clear();
            items.add("all");
            MedicamentType.put("all", 0);
            if (set != null) {
                while (set.next()) {
                    String name = set.getString(2);
                    Integer id = set.getInt(1);
                    MedicamentType.put(name, id);
                    items.add(name);
                }
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void listButtonTapped() {
        if (MedicamentType.get(choiceBox.getValue().toString()) == 0) {
            String sql = "SELECT m.title AS medicament_title,  " +
                    "       m.critical_rate AS critical_rate, " +
                    "       COALESCE(SUM(s.actual_balance), 0) AS total_balance, " +
                    "       mt.type_name AS medicament_type " +
                    "FROM medicament m " +
                    "LEFT JOIN storage s ON s.medicament_id = m.medicament_id  " +
                    "LEFT JOIN medicament_type mt ON m.type_id = mt.medicament_type_id  " +
                    "GROUP BY m.medicament_id, m.title, m.critical_rate, mt.type_name " +
                    "HAVING COALESCE(SUM(s.actual_balance), 0) <= m.critical_rate ";

            showResult(sql);
        } else {
            String sql = "SELECT m.title AS medicament_title, " +
                    "       m.critical_rate AS critical_rate, " +
                    "       COALESCE(SUM(s.actual_balance), 0) AS total_balance, " +
                    "       mt.type_name AS medicament_type " +
                    "FROM medicament m " +
                    "LEFT JOIN storage s ON s.medicament_id = m.medicament_id " +
                    "LEFT JOIN medicament_type mt ON m.type_id = mt.medicament_type_id " +
                    "WHERE mt.type_name = ? " +
                    "GROUP BY m.medicament_id, m.title, m.critical_rate, mt.type_name " +
                    "HAVING COALESCE(SUM(s.actual_balance), 0) <= m.critical_rate";

            showResult(sql, choiceBox.getValue().toString());
        }
    }



}
