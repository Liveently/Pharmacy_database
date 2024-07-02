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

public class PatientsWaitingToOrder implements SelectController, Initializable {
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
            String sql = "SELECT p.patient_firstname, p.patient_surname, med.title AS missing_medicament, pt.METHOD_OF_PREPARATION AS preparation_technology " +
                    "FROM order_ ord " +
                    "INNER JOIN prescription pr ON ord.prescription_id = pr.prescription_id " +
                    "INNER JOIN patient p ON pr.patient_id = p.patient_id " +
                    "INNER JOIN preparation_technology pt ON pt.medic_id = pr.med_id " +
                    "INNER JOIN medicament_preparation_relationship mpr ON pt.preparation_technology_id = mpr.preparation_technology_id " +
                    "INNER JOIN medicament med ON mpr.medicament_id = med.medicament_id " +
                    "LEFT JOIN storage s ON s.medicament_id = med.medicament_id " +
                    "WHERE ord.is_ready = 'NO' AND s.medicament_id IS NULL ";

            showResult(sql);
        } else {
            String sql = "SELECT p.patient_firstname, p.patient_surname, med.title AS missing_medicament, pt.METHOD_OF_PREPARATION AS preparation_technology " +
                    "FROM order_ ord " +
                    "INNER JOIN prescription pr ON ord.prescription_id = pr.prescription_id " +
                    "INNER JOIN patient p ON pr.patient_id = p.patient_id " +
                    "INNER JOIN preparation_technology pt ON pt.medic_id = pr.med_id " +
                    "INNER JOIN medicament mm ON mm.medicament_id = pr.med_id " +
                    "INNER JOIN medicament_type mt ON mt.medicament_type_id = mm.type_id " +
                    "INNER JOIN medicament_preparation_relationship mpr ON pt.preparation_technology_id = mpr.preparation_technology_id " +
                    "INNER JOIN medicament med ON mpr.medicament_id = med.medicament_id " +
                    "LEFT JOIN storage s ON s.medicament_id = med.medicament_id " +
                    "WHERE ord.is_ready = ? AND s.medicament_id IS NULL " +
                    "AND mt.type_name = ?";

            showResult(sql, "NO", choiceBox.getValue().toString());
        }
    }

    @FXML
    private void numButtonTapped() {
        if (MedicamentType.get(choiceBox.getValue().toString()) == 0) {
            String sql = "select count(*) as \"count of patients\" " +
                    "FROM order_ ord " +
                    "INNER JOIN prescription pr ON ord.prescription_id = pr.prescription_id " +
                    "INNER JOIN patient p ON pr.patient_id = p.patient_id " +
                    "INNER JOIN preparation_technology pt ON pt.medic_id = pr.med_id " +
                    "INNER JOIN medicament_preparation_relationship mpr ON pt.preparation_technology_id = mpr.preparation_technology_id " +
                    "INNER JOIN medicament med ON mpr.medicament_id = med.medicament_id " +
                    "LEFT JOIN storage s ON s.medicament_id = med.medicament_id " +
                    "WHERE ord.is_ready = 'NO' AND s.medicament_id IS NULL ";
            showResult(sql);
        } else {
            String sql = "SELECT COUNT(*) AS \"count of patients\" " +
                    "FROM order_ ord " +
                    "INNER JOIN prescription pr ON ord.prescription_id = pr.prescription_id " +
                    "INNER JOIN patient p ON pr.patient_id = p.patient_id " +
                    "INNER JOIN preparation_technology pt ON pt.medic_id = pr.med_id " +
                    "INNER JOIN medicament mm ON mm.medicament_id = pr.med_id " +
                    "INNER JOIN medicament_type mt ON mt.medicament_type_id = mm.type_id " +
                    "INNER JOIN medicament_preparation_relationship mpr ON pt.preparation_technology_id = mpr.preparation_technology_id " +
                    "INNER JOIN medicament med ON mpr.medicament_id = med.medicament_id " +
                    "LEFT JOIN storage s ON s.medicament_id = med.medicament_id " +
                    "WHERE ord.is_ready = ? AND s.medicament_id IS NULL " +
                    "AND mt.type_name = ?";

            showResult(sql, "NO", choiceBox.getValue().toString());

        }
    }
}
