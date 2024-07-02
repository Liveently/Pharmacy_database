package ru.nsu.pharmacydatabase.controllers.insert;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableStringValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.nsu.pharmacydatabase.utils.DBInit;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ResourceBundle;

public class PatientInsertController implements InsertController, Initializable {
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
    private TextField firstnameField;
    @FXML
    private TextField surnameField;
    @FXML
    private TextField birthdateField;
    @FXML
    private TextField phoneNumberField;
    @FXML
    private TextField addressField;
    @FXML
    private  TextField registrationDateField;

    @Override
    public void setListener(ChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbInit = new DBInit(connection);
        registrationDateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
    }

    public void setMode(InsertMode mode) {
        insertMode = mode;
        if (mode == InsertMode.insert) {
            formTitleLabel.setText("Добавить пациента");
            insertButton.setText("Добавить");
        } else {
            formTitleLabel.setText("Изменить пациента");
            insertButton.setText("Изменить");
        }
    }

    public void executeProcedure() throws SQLException {
        String sql = "declare " +
                "    l_min_date date; " +
                "    l_max_date date; " +
                "    l_default_date date; " +
                "begin " +
                "    get_const_values(l_min_date, l_max_date, l_default_date); " +
                "    dbms_output.put_line(l_min_date); " +
                "    dbms_output.put_line(l_max_date); " +
                "    dbms_output.put_line(l_default_date); " +
                "end;";
        ResultSet res = connection.executeQueryAndGetResult(sql);
    }


    public void setItem(String item) {
        this.item = item;
        String firstname = DBInit.getSubstring(" PATIENT_FIRSTNAME=", "PATIENT_FIRSTNAME=", item);
        String surname = DBInit.getSubstring(" PATIENT_SURNAME=", "PATIENT_SURNAME=", item);
        String birthdate = DBInit.getSubstring(" PATIENT_BIRTHDATE=", "PATIENT_BIRTHDATE=", item);
        String phoneNumber = DBInit.getSubstring(" PATIENT_PHONE_NUMBER=", "PATIENT_PHONE_NUMBER=", item);
        String address = DBInit.getSubstring(" PATIENT_ADDRESS=", "PATIENT_ADDRESS=", item);
        String registrationDate = DBInit.getSubstring(" REGISTRATION_DATE=", "REGISTRATION_DATE=", item);
        firstnameField.setText(firstname);
        surnameField.setText(surname);
        birthdateField.setText(birthdate);
        phoneNumberField.setText(phoneNumber);
        addressField.setText(address);
        registrationDateField.setText(registrationDate);
    }

    boolean checkDate(String date1, String date2) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        Date dateFormat1 = format.parse(date1);
        Date dateFormat2 = format.parse(date2);
        int result = dateFormat1.compareTo(dateFormat2);
        return result <= 0;
    }

    private boolean isAfterToday(String date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        Date registrationDate = format.parse(date);
        Date today = new Date();
        return registrationDate.after(today);
    }

    public void insertButtonTapped(ActionEvent actionEvent) throws ParseException, SQLException {
        executeProcedure();
        String firstname = firstnameField.getText();
        String surname = surnameField.getText();
        String birthdate = birthdateField.getText();
        String phoneNumber = phoneNumberField.getText();
        String address = addressField.getText();
        String registrationDate = registrationDateField.getText();


        if (firstnameField.getText().isEmpty() || surnameField.getText().isEmpty() || birthdateField.getText().isEmpty() || phoneNumberField.getText().isEmpty() || addressField.getText().isEmpty() || registrationDateField.getText().isEmpty()) {
            showAlert("empty!", "Fill in required fields");}

        else if (!checkDateFormat(birthdate) || !checkDateFormat(registrationDate)) {
            showAlert("invalid date format", "Date format should be dd-MM-yyyy");
        }

        else if (isAfterToday(registrationDate)) {
            showAlert("invalid registration date", "Registration date cannot be later than today");
        }

         else if (!checkDate(birthdate, registrationDate)) {
            showAlert("wrong data", "date of registration cannot be greater than the date of birth");
        }
         else if (!(phoneNumber.length() == 11 && phoneNumber.charAt(0) == '8')) {
            showAlert("invalid format", "The telephone number must have 11 digits and start with the digit 8");
        } else {
            if (insertMode == InsertMode.insert) {
                dbInit.insertPatient(firstname, surname, birthdate, phoneNumber, address, registrationDate);
            } else {
                int id = DBInit.getIdFrom(item);
                dbInit.updatePatient(id, firstname, surname, birthdate, phoneNumber, address, registrationDate);
            }
            listener.changed(name, "", name);
            Stage stage = (Stage) insertButton.getScene().getWindow();
            stage.close();
        }

    }

    private boolean checkDateFormat(String date) {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        format.setLenient(false);
        try {
            format.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
