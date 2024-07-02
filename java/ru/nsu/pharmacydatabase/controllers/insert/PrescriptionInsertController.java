package ru.nsu.pharmacydatabase.controllers.insert;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.nsu.pharmacydatabase.controllers.select.TableController;
import ru.nsu.pharmacydatabase.utils.DBInit;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class PrescriptionInsertController implements InsertController, Initializable {
    private DBInit dbInit;
    private ChangeListener listener;
    private ObservableStringValue name = new SimpleStringProperty("");
    private InsertMode insertMode = InsertMode.insert;
    private String item;
    @FXML
    private Label formTitleLabel; // Added fx:id reference
    @FXML
    private Button insertButton;
    @FXML
    private TextField countField;
    @FXML
    private TextField directionForUseField;
    @FXML
    private TextField diagnosisField;
    @FXML
    private ChoiceBox doctorChoiceBox;
    @FXML
    private ChoiceBox patientChoiceBox;
    @FXML
    private ChoiceBox medicamentChoiceBox;
    @FXML
    private Button newButton;

    @Override
    public void setListener(ChangeListener listener) {
        this.listener = listener;
    }

    private ObservableList<String> itemsDoctor = FXCollections.<String>observableArrayList();
    private ObservableList<String> itemsPatient = FXCollections .<String>observableArrayList();
    private ObservableList<String> itemsMedicament = FXCollections .<String>observableArrayList();
    private Map<String, Integer> Doctor;
    private Map<String, Integer> Patient;
    private Map<String, Integer> Medicament;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbInit = new DBInit(connection);
        doctorChoiceBox.setItems(itemsDoctor);
        patientChoiceBox.setItems(itemsPatient);
        medicamentChoiceBox.setItems(itemsMedicament);
        try {
            ResultSet setDoctor = connection.executeQueryAndGetResult("select * from doctor");
            ResultSet setPatient = connection.executeQueryAndGetResult("select * from patient");
            ResultSet setMedicament = connection.executeQueryAndGetResult("select * from medicament");
            Doctor = new HashMap<>();
            itemsDoctor.clear();
            Patient = new HashMap<>();
            itemsPatient.clear();
            Medicament = new HashMap<>();
            itemsMedicament.clear();




            if (setDoctor != null) {
                while (setDoctor.next()) {
                    String firstName = setDoctor.getString(2);
                    String surName = setDoctor.getString(3);
                    Integer id = setDoctor.getInt(1);
                    Doctor.put(firstName + " " + surName, id);
                    itemsDoctor.add(firstName + " " + surName);
                }
            }
            if (setPatient != null) {
                while (setPatient.next()) {
                    String firstName = setPatient.getString(2);
                    String surName = setPatient.getString(3);
                    Integer id = setPatient.getInt(1);
                    Patient.put(firstName + " " + surName, id);
                    itemsPatient.add(firstName + " " + surName);
                }
            }

            if (setMedicament != null) {
                while (setMedicament.next()) {
                    String title = setMedicament.getString(2);
                    Integer id = setMedicament.getInt(1);
                    Medicament.put(title , id);
                    itemsMedicament.add(title);
                }
            }


        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateDoctorChoiceBox() {
        try {
            ResultSet setDoctor = connection.executeQueryAndGetResult("select * from doctor");
            Doctor = new HashMap<>();
            itemsDoctor.clear();
            if (setDoctor != null) {
                while (setDoctor.next()) {
                    String firstName = setDoctor.getString(2);
                    String surName = setDoctor.getString(3);
                    Integer id = setDoctor.getInt(1);
                    Doctor.put(firstName + " " + surName, id);
                    itemsDoctor.add(firstName + " " + surName);
                }
            }
            doctorChoiceBox.setItems(itemsDoctor);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setMode(InsertMode mode) {
        insertMode = mode;
        if (mode == InsertMode.insert) {
            formTitleLabel.setText("Добавить рецепт");
            insertButton.setText("Добавить");
        } else {
            formTitleLabel.setText("Изменить рецепт");
            insertButton.setText("Изменить");
        }
    }

    public void setItem(String item) {
        this.item = item;
        insertButton.setText("Изменить");
        String count = DBInit.getSubstring(" COUNT=", "COUNT=", item);
        String dirForUse = DBInit.getSubstring(" DIRECTION_FOR_USE=", "DIRECTION_FOR_USE=", item);
        String diagnosis = DBInit.getSubstring(" DIAGNOSIS=", "DIAGNOSIS=", item);
        String doctorName = DBInit.getSubstring(" DOCTOR_NAME=", "DOCTOR_NAME=", item);
        String patientName = DBInit.getSubstring(" PATIENT_NAME=", "PATIENT_NAME=", item);
        String tittle = DBInit.getSubstring(" TITLE=", "TITLE=", item);

        countField.setText(count);
        directionForUseField.setText(dirForUse);
        diagnosisField.setText(diagnosis);
        doctorChoiceBox.setValue(doctorName);
        patientChoiceBox.setValue(patientName);
        medicamentChoiceBox.setValue(tittle);
    }

    public void insertButtonTapped(ActionEvent actionEvent) throws SQLException {


        if (countField.getText().isEmpty() || diagnosisField.getText().isEmpty() || medicamentChoiceBox.getSelectionModel().isEmpty() || patientChoiceBox.getSelectionModel().isEmpty()) {
            showAlert("empty!", "Fill in required fields");

        } else {
            String count = countField.getText();
            int num = Integer.valueOf(count);
            String dirForUse = directionForUseField.getText();
            String diagnosis = diagnosisField.getText();
            String doctor = doctorChoiceBox.getValue().toString();
            int doctorId = Doctor.get(doctor);
            String patient = patientChoiceBox.getValue().toString();
            int patientId = Patient.get(patient);
            String medicament = medicamentChoiceBox.getValue().toString();
            int medId = Medicament.get(medicament);

            if (insertMode == InsertMode.insert) {
                dbInit.insertPrescription(num, dirForUse, diagnosis, doctorId, patientId, medId);
            } else {
                int id = DBInit.getIdFrom(item);
                dbInit.updatePrescription(id, num, dirForUse, diagnosis, doctorId, patientId, medId);
            }

            if (listener != null) {
                listener.changed(name, "", name);
            }
            Stage stage = (Stage) insertButton.getScene().getWindow();
            stage.close();
        }
    }


    public void newButtonTapped(ActionEvent event) throws IOException {

        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(getClass().getResourceAsStream("/ru/nsu/pharmacydatabase/windows/insert/new_doctor.fxml"));

        // Создание нового окна для добавления доктора
        Stage newDoctorStage = new Stage();
        newDoctorStage.setScene(new Scene(root));

        newDoctorStage.showAndWait();  // Дождаться закрытия окна


        // После закрытия окна обновляем данные в doctorChoiceBox
        updateDoctorChoiceBox();

    }
}
