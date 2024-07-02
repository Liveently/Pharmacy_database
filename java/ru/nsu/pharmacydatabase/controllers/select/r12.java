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

public class r12 implements SelectController, Initializable {
    @FXML
    public Button listButton;
    @FXML
    public ChoiceBox choiceBoxMedicamentType;
    public ChoiceBox choiceBoxMedicament;

    private DBInit dbInit;
    private ObservableList<String> items = FXCollections.<String>observableArrayList();

    private ObservableList<String> itemsMedicament = FXCollections.<String>observableArrayList();
    private ObservableList<String> itemsMedicamentType = FXCollections.<String>observableArrayList();

    private Map<String, Integer> MedicamentType;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbInit = new DBInit(connection);
        choiceBoxMedicament.setItems(itemsMedicament);
        choiceBoxMedicamentType.setItems(itemsMedicamentType);

        try {
            ResultSet setMedicamentType = connection.executeQueryAndGetResult("select distinct * from medicament_type");
            MedicamentType = new HashMap<>();
            itemsMedicamentType.clear();
            itemsMedicamentType.add("all");
            MedicamentType.put("all", 0);
            if (setMedicamentType != null) {
                while (setMedicamentType.next()) {
                    String name = setMedicamentType.getString(2);
                    Integer id = setMedicamentType.getInt(1);
                    MedicamentType.put(name, id);
                    itemsMedicamentType.add(name);
                }
            }

            ResultSet setMedicament = connection.executeQueryAndGetResult("select distinct title from medicament");
            itemsMedicament.clear();
            itemsMedicament.add("all");
            if (setMedicament != null) {
                while (setMedicament.next()) {
                    itemsMedicament.add(setMedicament.getString(1));
                }
            }

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void listButtonTapped() {
        if (choiceBoxMedicament.getSelectionModel().isEmpty() || choiceBoxMedicamentType.getSelectionModel().isEmpty()) {
            showAlert("empty!", "fields cannot be empty");
        } else if (choiceBoxMedicament.getValue().equals("all") && choiceBoxMedicamentType.getValue().toString().equals("all")) {
            String sql = "SELECT patient_firstname, patient_surname, order_count  " +
                    "FROM ( " +
                    "    SELECT p.patient_firstname, p.patient_surname, COUNT(ord.order_id) AS order_count, " +
                    "           ROW_NUMBER() OVER (ORDER BY COUNT(ord.order_id) DESC) AS rn " +
                    "    FROM patient p " +
                    "    INNER JOIN prescription pr ON pr.patient_ID = p.patient_ID " +
                    "    INNER JOIN order_ ord ON ord.prescription_id = pr.prescription_id " +
                    "    GROUP BY p.patient_firstname, p.patient_surname " +
                    ") " +
                    "WHERE rn <= 10";
            showResult(sql);
        } else if (choiceBoxMedicament.getValue().toString().equals("all") && !choiceBoxMedicamentType.getValue().toString().equals("all")) {
            String sql = "SELECT patient_firstname, patient_surname, order_count  " +
                    "FROM ( " +
                    "    SELECT p.patient_firstname, p.patient_surname, COUNT(ord.order_id) AS order_count, " +
                    "           ROW_NUMBER() OVER (ORDER BY COUNT(ord.order_id) DESC) AS rn " +
                    "    FROM patient p " +
                    "    INNER JOIN prescription pr ON pr.patient_ID = p.patient_ID " +
                    "    INNER JOIN order_ ord ON ord.prescription_id = pr.prescription_id " +
                    "    INNER JOIN medicament med ON med.medicament_id = pr.med_id " +
                    "    INNER JOIN medicament_type mt ON mt.medicament_type_id = med.type_id " +
                    "    WHERE mt.type_name = ? " +
                    "    GROUP BY p.patient_firstname, p.patient_surname " +
                    ") " +
                    "WHERE rn <= 10";

            showResult(sql, choiceBoxMedicamentType.getValue().toString());
        }else if (!choiceBoxMedicament.getValue().toString().equals("all") && choiceBoxMedicamentType.getValue().toString().equals("all")) {
            String sql = "SELECT patient_firstname, patient_surname, order_count  " +
                    "FROM ( " +
                    "    SELECT p.patient_firstname, p.patient_surname, COUNT(ord.order_id) AS order_count, " +
                    "           ROW_NUMBER() OVER (ORDER BY COUNT(ord.order_id) DESC) AS rn " +
                    "    FROM patient p " +
                    "    INNER JOIN prescription pr ON pr.patient_ID = p.patient_ID " +
                    "    INNER JOIN order_ ord ON ord.prescription_id = pr.prescription_id " +
                    "    INNER JOIN medicament med ON med.medicament_id = pr.med_id " +
                    "    INNER JOIN medicament_type mt ON mt.medicament_type_id = med.type_id " +
                    "    WHERE med.title = ? " +
                    "    GROUP BY p.patient_firstname, p.patient_surname " +
                    ") " +
                    "WHERE rn <= 10";

            showResult(sql, choiceBoxMedicament.getValue().toString());
        }
        else {
            String sql = "SELECT patient_firstname, patient_surname, order_count  " +
                    "FROM ( " +
                    "    SELECT p.patient_firstname, p.patient_surname, COUNT(ord.order_id) AS order_count, " +
                    "           ROW_NUMBER() OVER (ORDER BY COUNT(ord.order_id) DESC) AS rn " +
                    "    FROM patient p " +
                    "    INNER JOIN prescription pr ON pr.patient_ID = p.patient_ID " +
                    "    INNER JOIN order_ ord ON ord.prescription_id = pr.prescription_id " +
                    "    INNER JOIN medicament med ON med.medicament_id = pr.med_id " +
                    "    INNER JOIN medicament_type mt ON mt.medicament_type_id = med.type_id " +
                    "    WHERE med.title = ? " +
                    "    AND mt.type_name = ? " +
                    "    GROUP BY p.patient_firstname, p.patient_surname " +
                    ") " +
                    "WHERE rn <= 10";

            showResult(sql, choiceBoxMedicament.getValue().toString(), choiceBoxMedicamentType.getValue().toString());

        }

    }


}