package ru.nsu.pharmacydatabase.controllers.base;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import ru.nsu.pharmacydatabase.controllers.select.TableController;
import ru.nsu.pharmacydatabase.utils.Connection;
import ru.nsu.pharmacydatabase.utils.DBInit;
import ru.nsu.pharmacydatabase.Main;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class MainController implements Initializable { //обрабатывает главное окно приложения
    public final static String FXML = "/ru/nsu/pharmacydatabase/windows/main_window.fxml";
    public final static String REQ_FXML = "/ru/nsu/pharmacydatabase/windows/choice_window.fxml";
    private final Connection connection;
    private final DBInit dbInit;
    public static final ObservableList data = FXCollections.observableArrayList();

    @FXML
    private ListView<String> tableListView; //названия таблиц из бд - просмотр и создание таблиц внутри бд
    public MainController() {
        connection = Main.getConnection();
        dbInit = new DBInit(connection);
//        FXMLLoader loader = new FXMLLoader();
//        Parent root = null;
//        try {
//            root = loader.load(getClass().getResourceAsStream(REQ_FXML));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Stage stage = new Stage();
//        assert root != null;
//        stage.setScene(new Scene(root));
//        stage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tableListView.setItems(data);
        configureTableView();
        tableListView.setOnMouseClicked(event -> {
            System.out.println("clicked on " + tableListView.getSelectionModel().getSelectedItem());
            Stage stage = new Stage();
            stage.setTitle(tableListView.getSelectionModel().getSelectedItem());

            FXMLLoader loader = new FXMLLoader();
            Parent root = null;
            try {
                root = loader.load(getClass().getResourceAsStream("/ru/nsu/pharmacydatabase/windows/table_window.fxml"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            TableController tableController = loader.getController();
            String parameter = tableListView.getSelectionModel().getSelectedItem();
            tableController.setTableName(parameter);
            try {
                tableController.loadData();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            assert root != null;
            stage.setScene(new Scene(root));
            stage.show();
        });
    }

    @FXML
    private void dropButtonTapped() throws SQLException {
        dbInit.clear();
        configureTableView();
    }
    @FXML
    private void createButtonTapped() throws SQLException {
        dbInit.init();
        configureTableView();
    }

    private void configureTableView() {
        try {
            data.clear();
            ResultSet set = connection.executeQueryAndGetResult("SELECT table_name " +
                                                                      "FROM all_tables " +
                                                                        "WHERE owner = 'TEST' ");
            if (set != null) {
                while (set.next()) {
                    String name = set.getString(1);
                    data.add(name);
                }
            }
            tableListView.refresh();
        } catch(SQLException throwable) {
            throwable.printStackTrace();
        }
    }
}
