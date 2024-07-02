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

public class r8 implements SelectController, Initializable {
    @FXML
    public Button listButton;
    @FXML
    public Button numButton;

    private DBInit dbInit;
    private ObservableList<String> items = FXCollections.<String>observableArrayList();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbInit = new DBInit(connection);
    }

    @FXML
    private void listButtonTapped() {

            String sql = "SELECT ord.order_id, med.title || '  (' || pat.PATIENT_FIRSTNAME || ' ' || pat.PATIENT_SURNAME || ')  ' as prescription, ord.is_ready, ord.is_received, ord.start_date, empl.PHARMACY_EMPLOYEE_FIRSTNAME || ' ' || empl.PHARMACY_EMPLOYEE_SURNAME as pharmacy_employee " +
                    "FROM order_ ord " +
                    "INNER JOIN PRESCRIPTION pr ON ord.PRESCRIPTION_id = pr.PRESCRIPTION_id " +
                    "INNER JOIN PATIENT pat ON pr.PATIENT_ID = pat.PATIENT_ID "+
                    "INNER JOIN PHARMACY_EMPLOYEE empl ON ord.PHARMACY_EMPLOYEE_ID = empl.PHARMACY_EMPLOYEE_ID "+
                    "INNER JOIN medicament med " +
                    "ON med.medicament_id = pr.med_id " +
                    "where ord.is_ready = 'NO'";

            showResult(sql);

    }

    @FXML
    private void numButtonTapped() {
            String sql = "select count(*) as \"count of orders\" " +
                    "FROM order_ ord " +
                    "INNER JOIN PRESCRIPTION pr ON ord.PRESCRIPTION_id = pr.PRESCRIPTION_id " +
                    "INNER JOIN PATIENT pat ON pr.PATIENT_ID = pat.PATIENT_ID "+
                    "INNER JOIN PHARMACY_EMPLOYEE empl ON ord.PHARMACY_EMPLOYEE_ID = empl.PHARMACY_EMPLOYEE_ID "+
                    "INNER JOIN medicament med " +
                    "ON med.medicament_id = pr.med_id " +
                    "where ord.is_ready = 'NO'";
            showResult(sql);

    }
}
