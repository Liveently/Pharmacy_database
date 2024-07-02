package ru.nsu.pharmacydatabase.controllers.insert;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.nsu.pharmacydatabase.utils.DBInit;

import java.net.URL;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MedicamentInsertController implements InsertController, Initializable {

    @FXML
    public TextField titleField;
    @FXML
    public TextField usageField;
    @FXML
    public TextField timeField;

    @FXML
    public TextField criticalRateField;


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
    private ChoiceBox typeIdField;

    private ObservableList<String> itemsType = FXCollections.<String>observableArrayList();
    private Map<String, Integer> Type;

    @Override
    public void setListener(ChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbInit = new DBInit(connection);
        typeIdField.setItems(itemsType);
        try {
            ResultSet setType = connection.executeQueryAndGetResult("select * from medicament_type");
            Type = new HashMap<>();
            itemsType.clear();
            if (setType != null) {
                while (setType.next()) {
                    String name = setType.getString(2);
                    Integer id = setType.getInt(1);
                    Type.put(name, id);
                    itemsType.add(name);
                }
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    public void setMode(InsertMode mode) {
        insertMode = mode;
        if (mode == InsertMode.insert) {
            formTitleLabel.setText("Добавить лекарство");
            insertButton.setText("Добавить");
        } else {
            formTitleLabel.setText("Изменить лекарство");
            insertButton.setText("Изменить");
        }
    }

    public void setItem(String item) {
        this.item = item;
        String title = DBInit.getSubstring(" TITLE=", "TITLE=", item);
        String usage = DBInit.getSubstring(" USAGE=", "USAGE=", item);
        String time = DBInit.getSubstring(" PRODUCTION_TIME=", "PRODUCTION_TIME=", item);
        String type = DBInit.getSubstring(" TYPE_NAME=", "TYPE_NAME=", item);

        String rate = DBInit.getSubstring(" CRITICAL_RATE=", "CRITICAL_RATE=", item);

        criticalRateField.setText(rate);

        titleField.setText(title);
        usageField.setText(usage);
        timeField.setText(time);
        typeIdField.setValue(type);
    }

    public void insertButtonTapped(ActionEvent actionEvent) {
        int typeId = Type.get(typeIdField.getValue().toString());
        if (titleField.getText().isEmpty() || usageField.getText().isEmpty() || criticalRateField.getText().isEmpty()|| timeField.getText().isEmpty() || typeIdField.getSelectionModel().isEmpty()) {
            showAlert("empty!", "Fill in required fields");
        } else {
            String count = timeField.getText();
            int num = Integer.valueOf(count);
            int rate = Integer.valueOf(criticalRateField.getText());

            if (num<=0)  showAlert("", "Production time must be greater than 0");
            if (insertMode == InsertMode.insert) {


                dbInit.insertMedicament(titleField.getText(), usageField.getText(), num, rate, typeId);


            }else {
                    int id = DBInit.getIdFrom(item);
                dbInit.updateMedicament(id, titleField.getText(), usageField.getText(), num, rate, typeId);
            }
            listener.changed(name, "", name);
            Stage stage = (Stage) insertButton.getScene().getWindow();
            stage.close();
        }
    }
}
