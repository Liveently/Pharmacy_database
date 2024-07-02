package ru.nsu.pharmacydatabase.controllers.insert;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ru.nsu.pharmacydatabase.utils.DBInit;

import java.net.URL;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class OrderInsertController implements InsertController, Initializable {
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
    private ChoiceBox choiceBoxPrescription;
    @FXML
    private ChoiceBox isReadyField;
    @FXML
    private ChoiceBox isReceivedField;
    @FXML
    private TextField startDateField;
    @FXML
    private ChoiceBox employeeChoiceBox;

    private ObservableList<String> itemsPrescription = FXCollections .<String>observableArrayList();
    private ObservableList<String> itemsIsReady = FXCollections .<String>observableArrayList();
    private ObservableList<String> itemsIsReceive = FXCollections .<String>observableArrayList();
    private ObservableList<String> itemsEmployee = FXCollections.<String>observableArrayList();
    private Map<String, Integer> Prescription;
    private Map<String, Integer> Employee;

    @Override
    public void setListener(ChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbInit = new DBInit(connection);
        startDateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        employeeChoiceBox.setItems(itemsEmployee);
        choiceBoxPrescription.setItems(itemsPrescription);
        isReadyField.setItems(itemsIsReady);
        isReceivedField.setItems(itemsIsReceive);
        try {

            ResultSet setEmployee = connection.executeQueryAndGetResult("select * from pharmacy_employee");


            ResultSet setPrescription = connection.executeQueryAndGetResult("SELECT pr.PRESCRIPTION_ID, med.title, pat.PATIENT_FIRSTNAME, pat.PATIENT_SURNAME " +
                                                                                "FROM PRESCRIPTION pr " +
                                                                                "INNER JOIN Medicament med ON pr.med_id = med.medicament_id " +
                                                                                "INNER JOIN PATIENT pat ON pr.PATIENT_ID = pat.PATIENT_ID");
       
       
            Prescription = new HashMap<>();
            itemsPrescription.clear();
            Employee = new HashMap<>();
            itemsEmployee.clear();

            itemsIsReady.clear();
            itemsIsReady.add("YES");
            itemsIsReady.add("NO");
            itemsIsReceive.clear();
            itemsIsReceive.add("YES");
            itemsIsReceive.add("NO");


            if (setEmployee != null) {
                while (setEmployee.next()) {
                    String firstName = setEmployee.getString(2);
                    String surName = setEmployee.getString(3);
                    Integer id = setEmployee.getInt(1);
                    Employee.put(firstName + " " + surName, id);
                    itemsEmployee.add(firstName + " " + surName);
                }
            }

            if (setPrescription != null) {
                while (setPrescription.next()) {
                    Integer id = setPrescription.getInt(1);
                    String med = setPrescription.getString(2);
                    String name = setPrescription.getString(3);
                    String surname = setPrescription.getString(4);
                    Prescription.put(med + "  (" + name + " " + surname + ")  ", id);
                    itemsPrescription.add(med + "  (" + name + " " + surname + ")  ");
                }
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    public void setMode(InsertMode mode) {
        insertMode = mode;
        if (mode == InsertMode.insert) {
            formTitleLabel.setText("Добавить заказ");
            insertButton.setText("Добавить");
        } else {
            formTitleLabel.setText("Изменить заказ");
            insertButton.setText("Изменить");
        }
    }

    public void setItem(String item) {

        this.item = item;
        insertButton.setText("Изменить");
        String Prescription = DBInit.getSubstring(" PRESCRIPTION=", "PRESCRIPTION=", item);
        String isReady = DBInit.getSubstring(" IS_READY=", "IS_READY=", item);
        String isReceived = DBInit.getSubstring(" IS_RECEIVED=", "IS_RECEIVED=", item);
        String startDate = DBInit.getSubstring(" START_DATE=", "START_DATE=", item);

        String Employeer = DBInit.getSubstring(" PHARMACY_EMPLOYEE=", "PHARMACY_EMPLOYEE=", item);


        choiceBoxPrescription.setValue(Prescription);
        employeeChoiceBox.setValue(Employeer);

        isReadyField.setValue(isReady);
        isReceivedField.setValue(isReceived);
        startDateField.setText(startDate);
    }

    public void insertButtonTapped(ActionEvent actionEvent) {
        int PrescriptionId = Prescription.get(choiceBoxPrescription.getValue().toString());

        int employeeId = Employee.get(employeeChoiceBox.getValue().toString());

        String isReady = isReadyField.getValue().toString();
        String isReceived = isReceivedField.getValue().toString();
        String startDate = startDateField.getText();

        if (choiceBoxPrescription.getSelectionModel().isEmpty() || isReceivedField.getSelectionModel().isEmpty() ||
                isReadyField.getSelectionModel().isEmpty() || startDateField.getText().isEmpty() || employeeChoiceBox.getSelectionModel().isEmpty()) {
            showAlert("empty!", "Fill in required fields");
        } if (isReady.equals("NO") && isReceived.equals("YES")) {
            showAlert("Wrong data", "the order cannot be received if it is not yet ready");
        } else {
            if (insertMode == InsertMode.insert) {
                dbInit.insertOrder(PrescriptionId, isReady, isReceived, startDate, employeeId);
            } else {
                int id = DBInit.getIdFrom(item);
                dbInit.updateOrder(id, PrescriptionId, isReady, isReceived, startDate, employeeId);
            }
            listener.changed(name, "", name);
            Stage stage = (Stage) insertButton.getScene().getWindow();
            stage.close();
        }
    }

}
