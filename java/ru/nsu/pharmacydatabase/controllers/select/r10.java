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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class r10 implements SelectController, Initializable {
    @FXML
    public Button listButton;
    @FXML
    public Button numButton;

    @FXML
    public ChoiceBox choiceBoxMedicamentType;
    @FXML
    public ChoiceBox choiceBoxMedicament;

    private DBInit dbInit;
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
    private void listButtonTapped() throws ParseException {
        if (choiceBoxMedicament.getSelectionModel().isEmpty() || choiceBoxMedicamentType.getSelectionModel().isEmpty()) {
            showAlert("empty!", "Fill in required fields");
        } else if (choiceBoxMedicament.getValue().equals("all") && choiceBoxMedicamentType.getValue().toString().equals("all")) {
            String sql = "SELECT DISTINCT pt.PREPARATION_TECHNOLOGY_ID, med.title, pt.METHOD_OF_PREPARATION " +
                    "FROM order_ ord " +
                    "INNER JOIN prescription pr ON pr.PRESCRIPTION_ID = ord.PRESCRIPTION_ID " +
                    "INNER JOIN medicament med ON med.medicament_id = pr.med_id " +
                    "INNER JOIN preparation_technology pt ON pt.MEDIC_ID = med.medicament_id " +
                    "WHERE ord.is_ready = 'NO' AND ord.IS_RECEIVED = 'NO'";


            showResult(sql);
        } else if (choiceBoxMedicament.getValue().toString().equals("all") && !choiceBoxMedicamentType.getValue().toString().equals("all")) {
            String sql = "SELECT DISTINCT pt.PREPARATION_TECHNOLOGY_ID, med.title, pt.METHOD_OF_PREPARATION " +
                    "FROM order_ ord " +
                    "INNER JOIN prescription pr ON pr.PRESCRIPTION_ID = ord.PRESCRIPTION_ID " +
                    "INNER JOIN medicament med ON med.medicament_id = pr.med_id " +
                    "INNER JOIN medicament_type mt ON mt.medicament_type_id = med.type_id " +
                    "INNER JOIN preparation_technology pt ON pt.MEDIC_ID = med.medicament_id " +
                    "WHERE ord.is_ready = ? AND ord.IS_RECEIVED = ? " +
                    "AND mt.type_name = ?";

            showResult(sql, "NO", "NO", choiceBoxMedicamentType.getValue().toString());
        } else if (!choiceBoxMedicament.getValue().toString().equals("all") && choiceBoxMedicamentType.getValue().toString().equals("all")) {
            String sql = "SELECT DISTINCT pt.PREPARATION_TECHNOLOGY_ID, med.title, pt.METHOD_OF_PREPARATION " +
                    "FROM order_ ord " +
                    "INNER JOIN prescription pr ON pr.PRESCRIPTION_ID = ord.PRESCRIPTION_ID " +
                    "INNER JOIN medicament med ON med.medicament_id = pr.med_id " +
                    "INNER JOIN medicament_type mt ON mt.medicament_type_id = med.type_id " +
                    "INNER JOIN preparation_technology pt ON pt.MEDIC_ID = med.medicament_id " +
                    "WHERE ord.is_ready = ? AND ord.IS_RECEIVED = ? " +
                    "AND med.title = ?";

            showResult(sql, "NO", "NO", choiceBoxMedicament.getValue().toString());
        }
        else {
            String sql = "SELECT DISTINCT pt.PREPARATION_TECHNOLOGY_ID, med.title, pt.METHOD_OF_PREPARATION " +
                    "FROM order_ ord " +
                    "INNER JOIN prescription pr ON pr.PRESCRIPTION_ID = ord.PRESCRIPTION_ID " +
                    "INNER JOIN medicament med ON med.medicament_id = pr.med_id " +
                    "INNER JOIN medicament_type mt ON mt.medicament_type_id = med.type_id " +
                    "INNER JOIN preparation_technology pt ON pt.MEDIC_ID = med.medicament_id " +
                    "WHERE ord.is_ready = ? AND ord.IS_RECEIVED = ? " +
                    "AND med.title = ? AND mt.type_name = ?";

            showResult(sql, "NO", "NO", choiceBoxMedicament.getValue().toString(), choiceBoxMedicamentType.getValue().toString());

        }


    }
}

