package ru.nsu.pharmacydatabase.controllers.filter;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import ru.nsu.pharmacydatabase.Main;
import ru.nsu.pharmacydatabase.controllers.select.SelectTableController;
import ru.nsu.pharmacydatabase.utils.Connection;

import java.io.IOException;
import java.sql.ResultSet;

public interface FilterController {
    Connection connection = Main.getConnection();

    default void setListener(ChangeListener listener) {
    }

    default void showAlert(String message, String comment) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.setContentText(comment);
        alert.showAndWait();
    }

    default void showResult(String sql) {
        try {
            ResultSet set = connection.executeQueryAndGetResult(sql);
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
                showAlert("Ничего не найдено", "попробуйте поменять параметры поиска");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
