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
import javafx.stage.Stage;
import ru.nsu.pharmacydatabase.utils.DBInit;

import java.net.URL;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class RequestInsertController implements InsertController, Initializable {
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
    private ChoiceBox providerIdField;

    private ObservableList<String> itemsProvider = FXCollections.<String>observableArrayList();
    private Map<String, Integer> Provider;

    @Override
    public void setListener(ChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbInit = new DBInit(connection);
        providerIdField.setItems(itemsProvider);
        try {
            ResultSet setProvider = connection.executeQueryAndGetResult("select * from provider");
            Provider = new HashMap<>();
            itemsProvider.clear();
            if (setProvider != null) {
                while (setProvider.next()) {
                    String name = setProvider.getString(2);
                    Integer id = setProvider.getInt(1);
                    Provider.put(name, id);
                    itemsProvider.add(name);
                }
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    public void setMode(InsertMode mode) {
        insertMode = mode;
        if (mode == InsertMode.insert) {
            formTitleLabel.setText("Добавить заявку");
            insertButton.setText("Добавить");
        } else {
            formTitleLabel.setText("Изменить заявку");
            insertButton.setText("Изменить");
        }
    }

    public void setItem(String item) {
        this.item = item;
        insertButton.setText("Изменить");
        String providerName = DBInit.getSubstring(" PROVIDER_NAME=", "PROVIDER_NAME=", item);
        providerIdField.setValue(providerName);
    }

    public void insertButtonTapped(ActionEvent actionEvent) {
        int providerId = Provider.get(providerIdField.getValue().toString());
        if (providerIdField.getSelectionModel().isEmpty()) {
            showAlert("empty!", "Fill in required fields");
        } else {
            if (insertMode == InsertMode.insert) {
                dbInit.insertRequest(providerId);
            } else {
                int id = DBInit.getIdFrom(item);
                dbInit.updateRequest(id, providerId);
            }
            listener.changed(name, "", name);
            Stage stage = (Stage) insertButton.getScene().getWindow();
            stage.close();
        }

    }
}
