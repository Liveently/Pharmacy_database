package ru.nsu.pharmacydatabase.controllers.select;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ReceiveController implements SelectController {
    @FXML
    public Button listButton;
    @FXML
    public Button numButton;

    @FXML
    private void listButtonTapped() {
        String sql = "select patient_firstname, patient_surname, patient_birthdate, patient_phone_number, patient_address, registration_date " +
                "from patient pat " +
                "inner join prescription pr " +
                "on pr.PATIENT_ID = pat.patient_id " +
                "inner join order_ ord " +
                "on pr.PRESCRIPTION_ID = ord.PRESCRIPTION_ID " +
                "where ord.is_ready = 'YES' and ord.is_received = 'NO' " +
                "order by patient_firstname, patient_surname";
        showResult(sql);
    }

    @FXML
    private void numButtonTapped() {
        String sql = "select count(*) as \"count of patients\" " +
                "from patient pat " +
                "inner join prescription pr " +
                "on pr.PATIENT_ID = pat.patient_id " +
                "inner join order_ ord " +
                "on pr.PRESCRIPTION_ID = ord.PRESCRIPTION_ID " +
                "where ord.is_ready = 'YES' and ord.is_received = 'NO' " +
                "order by patient_firstname, patient_surname";
        showResult(sql);
    }
}
