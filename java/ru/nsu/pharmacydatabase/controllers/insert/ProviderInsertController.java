package ru.nsu.pharmacydatabase.controllers.insert;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableStringValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.nsu.pharmacydatabase.utils.DBInit;

import java.net.URL;
import java.util.ResourceBundle;

public class ProviderInsertController implements InsertController, Initializable {
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
    private TextField providerNameField;
    @FXML
    private TextField providerAddressField;
    @FXML
    private TextArea productsField;

    @Override
    public void setListener(ChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbInit = new DBInit(connection);
    }

    public void setMode(InsertMode mode) {

        insertMode = mode;
        if (mode == InsertMode.insert) {
            formTitleLabel.setText("Добавить поставщика");
            insertButton.setText("Добавить");
        } else {
            formTitleLabel.setText("Изменить поставщика");
            insertButton.setText("Изменить");
        }
    }

    public void setItem(String item) {
        this.item = item;
        insertButton.setText("Изменить");
        String providerName = DBInit.getSubstring(" PROVIDER_NAME=", "PROVIDER_NAME=", item);
        String providerAddress = DBInit.getSubstring(" PROVIDER_ADDRESS=", "PROVIDER_ADDRESS=", item);
        String products = DBInit.getSubstring(" PRODUCTS=", "PRODUCTS=", item);
        providerNameField.setText(providerName);
        providerAddressField.setText(providerAddress);
        productsField.setText(products);
    }

    public void insertButtonTapped(ActionEvent actionEvent) {
        if (providerNameField.getText().isEmpty() || providerAddressField.getText().isEmpty()|| productsField.getText().isEmpty()) {
            showAlert("empty!", "Fill in required fields");
        } else {
            String providerName = providerNameField.getText();
            String providerAddress = providerAddressField.getText();
            String products = productsField.getText();
            if (insertMode == InsertMode.insert) {
                dbInit.insertProvider(providerName, providerAddress, products);
            } else {
                int id = DBInit.getIdFrom(item);
                dbInit.updateProvider(id, providerName, providerAddress, products);
            }
            listener.changed(name, "", name);
            Stage stage = (Stage) insertButton.getScene().getWindow();
            stage.close();
        }

    }
}
