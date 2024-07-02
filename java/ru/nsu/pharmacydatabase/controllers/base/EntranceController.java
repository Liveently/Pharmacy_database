package ru.nsu.pharmacydatabase.controllers.base;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import ru.nsu.pharmacydatabase.utils.AdminMain;
import ru.nsu.pharmacydatabase.utils.Connection;
import ru.nsu.pharmacydatabase.utils.DBInit;
import ru.nsu.pharmacydatabase.Main;

import java.io.IOException;
import java.sql.SQLException;

public class EntranceController {
    public final static String LOGIN_WINDOW_FXML = "/ru/nsu/pharmacydatabase/windows/entrance_window.fxml";
    private final Connection connection;

    @FXML
    private TextArea loginText;
    @FXML
    private PasswordField passwordText;

    public EntranceController() {
        connection = Main.getConnection();
    }

    @FXML
    public void loginButtonTapped(ActionEvent event) {
        // Получение текста из полей
        String login = loginText.getText();
        String password = passwordText.getText();

        // Проверка на пустые поля
        if (login.isEmpty() || password.isEmpty()) {
            showAlert("Login and password must not be empty");
            return;
        }

        try {
            // Попытка зарегистрировать соединение с базой данных
            connection.registerConnection(login, password);
        } catch (SQLException ex) {
            System.out.println("SQLException: error with connection to server");
            showAlert("Error with connection to server: " + ex.getMessage());
            return;
        } catch (ClassNotFoundException ex) {
            System.out.println("ClassNotFoundException: error with driver manager");
            showAlert("Error with driver manager: " + ex.getMessage());
            return;
        }

        try {
            loadAdminMainWindow(event);
        } catch (IOException ex) {
            System.out.println("IOException: error loading FXML");
            showAlert("Error loading the main window: " + ex.getMessage());
        } catch (ExceptionInInitializerError ex) {
            System.out.println("ExceptionInInitializerError: session is already exist");
            showAlert("Session is already exist: " + ex.getMessage());
        } catch (ClassCastException ex) {
            System.out.println("ClassCastException: unable to cast event source to Node");
            showAlert("Unexpected error occurred: " + ex.getMessage());
        }
    }

    private void loadAdminMainWindow(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/nsu/pharmacydatabase/windows/admin_main_window.fxml"));

        loader.setControllerFactory(controllerClass -> {
            if (controllerClass == AdminMain.class) {
                AdminMain controller = new AdminMain();
                controller.setUserRole(loginText.getText()); // Передача роли пользователя
                return controller;
            } else {
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Parent root = loader.load();

        // Получаем текущую сцену
        Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }


    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}