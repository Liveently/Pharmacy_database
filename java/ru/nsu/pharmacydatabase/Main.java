package ru.nsu.pharmacydatabase;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.nsu.pharmacydatabase.controllers.base.EntranceController;
import ru.nsu.pharmacydatabase.utils.Connection;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class Main extends Application { // главная точка входа в приложение
    private static final Connection connection = new Connection(); // отвечает за соединение с БД

    public static void main(String[] args) {
        launch(args);
    } // запускается приложение

    @Override
    public void start(Stage stage) { // создается окно
        stage.setTitle("PHARMACY"); // устанавливается заголовок
        Locale.setDefault(new Locale("ru", "RU"));

        try {
            Parent root = loadFXML(EntranceController.LOGIN_WINDOW_FXML);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showError("Ошибка загрузки окна", e.getMessage());
        }
    }

    @Override
    public void stop() { // закрытие приложения
        try {
            connection.close();
        } catch (Exception e) {
            showError("Ошибка закрытия соединения", e.getMessage());
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    private Parent loadFXML(String fxmlPath) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(fxmlPath)) {
            if (inputStream == null) {
                throw new IOException("FXML файл не найден: " + fxmlPath);
            }
            return new FXMLLoader().load(inputStream);
        }
    }

    private void showError(String title, String message) {
        System.err.println(title + ": " + message);
    }
}