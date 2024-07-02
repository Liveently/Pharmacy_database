package ru.nsu.pharmacydatabase.utils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import ru.nsu.pharmacydatabase.controllers.base.MainController;

import java.io.IOException;

public class AdminMain {
    @FXML
    public Button button;

    @FXML
    private Label roleLabel;
    private String userRole;

    @FXML
    public void getAll() {
        showSelectWindow(MainController.FXML);
        Stage stage = (Stage) button.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void initialize() {
        if (userRole != null) {
            if (userRole.equals("ADMIN")) {
                roleLabel.setText("Вы вошли как администратор");
            } else if (userRole.equals("PHARM")) {
                roleLabel.setText("   Вы вошли как фармацевт");
            } else if (userRole.equals("TEH")) {
                roleLabel.setText("     Вы вошли как технолог");
            }
            else {
                roleLabel.setText("Вы вошли как " + userRole);
            }
        }
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
    private void showSelectWindow(String name) {
        Stage primaryStage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        try {
            Parent root = loader.load(getClass().getResourceAsStream(name));
            primaryStage.setScene(new Scene(root));
        } catch (IOException ignored) {
        }
        primaryStage.show();
    }

}
