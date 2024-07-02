package ru.nsu.pharmacydatabase.controllers.select;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import ru.nsu.pharmacydatabase.requests.Requests;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ChoiceController implements Initializable {
    public static final ObservableList data = FXCollections.observableArrayList();
    @FXML
    private ListView<String> selectsListView;

    public ChoiceController() {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectsListView.setItems(data);
        configureTableView();
        selectsListView.setOnMouseClicked(event -> {
            System.out.println("clicked on " + selectsListView.getSelectionModel().getSelectedItem());
            Requests req = Requests.getRequestByName(selectsListView.getSelectionModel().getSelectedItem());
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader();
            Parent root = null;
            try {
                root = loader.load(getClass().getResourceAsStream(req.getWindowName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert root != null;
            stage.setScene(new Scene(root));
            stage.show();
        });
    }

    private void configureTableView() {
        data.clear();
        Requests[] possibleValues = Requests.r1.getDeclaringClass().getEnumConstants();
        for(Requests value:  possibleValues) {
            String name = value.getName();
            data.add(name);
        }
    }
}
