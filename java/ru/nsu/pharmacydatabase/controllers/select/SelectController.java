package ru.nsu.pharmacydatabase.controllers.select;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import ru.nsu.pharmacydatabase.Main;
import ru.nsu.pharmacydatabase.utils.Connection;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface SelectController {
    final Connection connection = Main.getConnection();

    default void showAlert(String message, String comment) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Внимание");
        alert.setHeaderText(message);
        alert.setContentText(comment);
        alert.showAndWait();
    }

    default void showResult(String sql, Object... params) {
        try {

            PreparedStatement pstmt = connection.prepareStatement(sql);

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            ResultSet set = pstmt.executeQuery();

            if (set != null) {
                Stage stage = new Stage();
                FXMLLoader loader = new FXMLLoader();
                Parent root = null;
                try {
                    root = loader.load(getClass().getResourceAsStream(SelectTableController.fxml));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                SelectTableController tableController = loader.getController();
                tableController.set(set);
                assert root != null;
                stage.setScene(new Scene(root));
                stage.show();
            } else {
                showAlert("Ничего не найдено", "Попробуйте поменять параметры поиска");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Возникла ошибка при выполнении запроса: " + e.getMessage());
        }
    }

}
