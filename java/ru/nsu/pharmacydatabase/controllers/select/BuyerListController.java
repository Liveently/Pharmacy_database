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

public class BuyerListController implements SelectController, Initializable {
    @FXML
    public Button listButton;
    @FXML
    public Button numButton;
    @FXML
    public TextField startDate;
    @FXML
    public TextField endDate;
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

    boolean checkDate(String date1, String date2) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        Date dateFormat1 = format.parse(date1);
        Date dateFormat2 = format.parse(date2);
        int result = dateFormat1.compareTo(dateFormat2);
        if (result == 0) {
            System.out.println("Date1 is equal to Date2");
            return false;
        } else if (result > 0) {
            System.out.println("Date1 is after Date2");
            return false;
        } else if (result < 0) {
            System.out.println("Date1 is before Date2");
            return true;
        }
        return false;
    }

    @FXML
    private void listButtonTapped() throws ParseException {
        if (startDate.getText().isEmpty() || endDate.getText().isEmpty()) {
            showAlert("empty!", "Fill in required fields");
        } else if (!checkDate(startDate.getText(), endDate.getText())) {
            showAlert("wrong data", "");
        } else if (choiceBoxMedicament.getValue().toString().equals("all") && choiceBoxMedicamentType.getValue().toString().equals("all")) {
            String sql = "SELECT pat.patient_firstname, pat.patient_surname " +
                    "FROM patient pat " +
                    "INNER JOIN prescription pr ON pr.patient_id = pat.patient_id " +
                    "INNER JOIN order_ ord ON ord.prescription_id = pr.prescription_id " +
                    "WHERE ord.start_date >= TO_DATE(?, 'DD-MM-YYYY') " +
                    "AND ord.start_date <= TO_DATE(?, 'DD-MM-YYYY') " +
                    "GROUP BY pat.PATIENT_ID, pat.PATIENT_FIRSTNAME, pat.PATIENT_SURNAME";

            showResult(sql, startDate.getText(), endDate.getText());

        } else if (choiceBoxMedicament.getValue().toString().equals("all") && !choiceBoxMedicamentType.getValue().toString().equals("all")) {
            String sql = "SELECT pat.patient_firstname, pat.patient_surname " +
                    "FROM patient pat " +
                    "INNER JOIN prescription pr ON pr.patient_id = pat.patient_id " +
                    "INNER JOIN order_ ord ON ord.prescription_id = pr.prescription_id " +
                    "INNER JOIN medicament med ON med.medicament_id = pr.med_id " +
                    "INNER JOIN medicament_type mt ON mt.medicament_type_id = med.type_id " +
                    "WHERE ord.start_date >= TO_DATE(?, 'DD-MM-YYYY') " +
                    "AND mt.type_name = ? " +
                    "AND ord.start_date <= TO_DATE(?, 'DD-MM-YYYY') " +
                    "GROUP BY pat.PATIENT_ID, pat.PATIENT_FIRSTNAME, pat.PATIENT_SURNAME";

            showResult(sql, startDate.getText(), choiceBoxMedicamentType.getValue().toString(), endDate.getText());
        } else if (!choiceBoxMedicament.getValue().toString().equals("all") && choiceBoxMedicamentType.getValue().toString().equals("all")) {
            String sql = "SELECT pat.patient_firstname, pat.patient_surname " +
                    "FROM patient pat " +
                    "INNER JOIN prescription pr ON pr.patient_id = pat.patient_id " +
                    "INNER JOIN order_ ord ON ord.prescription_id = pr.prescription_id " +
                    "INNER JOIN medicament med ON med.medicament_id = pr.med_id " +
                    "WHERE ord.start_date >= TO_DATE(?, 'DD-MM-YYYY') " +
                    "AND med.title = ? " +
                    "AND ord.start_date <= TO_DATE(?, 'DD-MM-YYYY') " +
                    "GROUP BY pat.PATIENT_ID, pat.PATIENT_FIRSTNAME, pat.PATIENT_SURNAME";

            showResult(sql, startDate.getText(), choiceBoxMedicament.getValue().toString(), endDate.getText());

        }
        else {
            String sql = "SELECT pat.patient_firstname, pat.patient_surname " +
                    "FROM patient pat " +
                    "INNER JOIN prescription pr ON pr.patient_id = pat.patient_id " +
                    "INNER JOIN order_ ord ON ord.prescription_id = pr.prescription_id " +
                    "INNER JOIN medicament med ON med.medicament_id = pr.med_id " +
                    "INNER JOIN medicament_type mt ON mt.medicament_type_id = med.type_id " +
                    "WHERE ord.start_date >= TO_DATE(?, 'DD-MM-YYYY') " +
                    "AND mt.type_name = ? " +
                    "AND med.title = ? " +
                    "AND ord.start_date <= TO_DATE(?, 'DD-MM-YYYY') " +
                    "GROUP BY pat.PATIENT_ID, pat.PATIENT_FIRSTNAME, pat.PATIENT_SURNAME";

            showResult(sql, startDate.getText(), choiceBoxMedicamentType.getValue().toString(), choiceBoxMedicament.getValue().toString(), endDate.getText());

        }
    }

    @FXML
    private void numButtonTapped() throws ParseException {
        if (startDate.getText().isEmpty() || endDate.getText().isEmpty()) {
            showAlert("empty!", "Fill in required fields");
        } else if (!checkDate(startDate.getText(), endDate.getText())) {
            showAlert("wrong data", "");
        } else if (choiceBoxMedicament.getValue().toString().equals("all") && choiceBoxMedicamentType.getValue().toString().equals("all")) {
            String sql = "SELECT COUNT(*) AS \"count of patients\" " +
                    "FROM patient pat " +
                    "INNER JOIN prescription pr ON pr.patient_id = pat.patient_id " +
                    "INNER JOIN order_ ord ON ord.prescription_id = pr.prescription_id " +
                    "WHERE ord.start_date >= TO_DATE(?, 'DD-MM-YYYY') " +
                    "AND ord.start_date <= TO_DATE(?, 'DD-MM-YYYY')";

            showResult(sql, startDate.getText(), endDate.getText());

        } else if (choiceBoxMedicament.getValue().toString().equals("all") && !choiceBoxMedicamentType.getValue().toString().equals("all")) {
            String sql = "SELECT COUNT(*) AS \"count of patients\" " +
                    "FROM patient pat " +
                    "INNER JOIN prescription pr ON pr.patient_id = pat.patient_id " +
                    "INNER JOIN order_ ord ON ord.prescription_id = pr.prescription_id " +
                    "INNER JOIN medicament med ON med.medicament_id = pr.med_id " +
                    "INNER JOIN medicament_type mt ON mt.medicament_type_id = med.type_id " +
                    "WHERE ord.start_date >= TO_DATE(?, 'DD-MM-YYYY') " +
                    "AND mt.type_name = ? " +
                    "AND ord.start_date <= TO_DATE(?, 'DD-MM-YYYY')";

            showResult(sql, startDate.getText(), choiceBoxMedicamentType.getValue().toString(), endDate.getText());

        } else if (!choiceBoxMedicament.getValue().toString().equals("all") && choiceBoxMedicamentType.getValue().toString().equals("all")) {
            String sql = "SELECT COUNT(*) AS \"count of patients\" " +
                    "FROM patient pat " +
                    "INNER JOIN prescription pr ON pr.patient_id = pat.patient_id " +
                    "INNER JOIN order_ ord ON ord.prescription_id = pr.prescription_id " +
                    "INNER JOIN medicament med ON med.medicament_id = pr.med_id " +
                    "INNER JOIN medicament_type mt ON mt.medicament_type_id = med.type_id " +
                    "WHERE ord.start_date >= TO_DATE(?, 'DD-MM-YYYY') " +
                    "AND med.title = ? " +
                    "AND ord.start_date <= TO_DATE(?, 'DD-MM-YYYY')";

            showResult(sql, startDate.getText(), choiceBoxMedicament.getValue().toString(), endDate.getText());

        } else {
            String sql = "SELECT COUNT(*) AS \"count of patients\" " +
                    "FROM patient pat " +
                    "INNER JOIN prescription pr ON pr.patient_id = pat.patient_id " +
                    "INNER JOIN order_ ord ON ord.prescription_id = pr.prescription_id " +
                    "INNER JOIN medicament med ON med.medicament_id = pr.med_id " +
                    "INNER JOIN medicament_type mt ON mt.medicament_type_id = med.type_id " +
                    "WHERE ord.start_date >= TO_DATE(?, 'DD-MM-YYYY') " +
                    "AND mt.type_name = ? " +
                    "AND med.title = ? " +
                    "AND ord.start_date <= TO_DATE(?, 'DD-MM-YYYY')";

            showResult(sql, startDate.getText(), choiceBoxMedicamentType.getValue().toString(), choiceBoxMedicament.getValue().toString(), endDate.getText());

        }

    }
}
