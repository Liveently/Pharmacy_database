

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
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class StorageInsertController implements InsertController, Initializable {
    @FXML
    public TextField ShelfLifeField;
    @FXML
    public TextField balanceField;
    @FXML
    public TextField priceField;
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
    private ChoiceBox medIdField;

    private ObservableList<String> itemsMedicament = FXCollections.<String>observableArrayList();
    private Map<String, Integer> Medicament;

    @Override
    public void setListener(ChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbInit = new DBInit(connection);
        medIdField.setItems(itemsMedicament);
        try {
            ResultSet setMedicament = connection.executeQueryAndGetResult("select * from medicament");
            Medicament = new HashMap<>();
            itemsMedicament.clear();
            if (setMedicament != null) {
                while (setMedicament.next()) {
                    String name = setMedicament.getString(2);
                    Integer id = setMedicament.getInt(1);
                    Medicament.put(name, id);
                    itemsMedicament.add(name);
                }
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    public void setMode(InsertMode mode) {
        insertMode = mode;
        if (mode == InsertMode.insert) {
            formTitleLabel.setText("Добавить партию");
            insertButton.setText("Добавить");
        } else {
            formTitleLabel.setText("Изменить партию");
            insertButton.setText("Изменить");
        }
    }

    public void setItem(String item) {
        this.item = item;
        String medName = DBInit.getSubstring(" TITLE=", "TITLE=", item);
        medIdField.setValue(medName);


        String rate = DBInit.getSubstring(" SHELF_LIFE=", "SHELF_LIFE=", item);
        String balance = DBInit.getSubstring(" ACTUAL_BALANCE=", "ACTUAL_BALANCE=", item);
        String price = DBInit.getSubstring(" PRICE=", "PRICE=", item);
        ShelfLifeField.setText(rate);
        balanceField.setText(balance);
        priceField.setText(price);

    }

    public void insertButtonTapped(ActionEvent actionEvent) {
        int medId = Medicament.get(medIdField.getValue().toString());
        if (medIdField.getSelectionModel().isEmpty() || ShelfLifeField.getText().isEmpty()
                || balanceField.getText().isEmpty() || priceField.getText().isEmpty()) {
            showAlert("empty!", "Fill in required fields");
        } else {



            int balance = Integer.valueOf(balanceField.getText());

            double price = Double.valueOf(priceField.getText());


            if (insertMode == InsertMode.insert) {
                dbInit.insertStorage(medId, ShelfLifeField.getText(), balance, price);
            } else {
                int id = DBInit.getIdFrom(item);
                dbInit.updateStorage(id, medId, ShelfLifeField.getText(), balance, price);
            }
            listener.changed(name, "", name);
            Stage stage = (Stage) insertButton.getScene().getWindow();
            stage.close();
        }

    }
}



