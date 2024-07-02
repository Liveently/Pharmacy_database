package ru.nsu.pharmacydatabase.controllers.select;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.stage.Stage;
import ru.nsu.pharmacydatabase.*;
import ru.nsu.pharmacydatabase.controllers.insert.InsertController;
import ru.nsu.pharmacydatabase.utils.Connection;
import ru.nsu.pharmacydatabase.utils.DBInit;
import ru.nsu.pharmacydatabase.controllers.insert.InsertMode;
import ru.nsu.pharmacydatabase.utils.Tables;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class TableController implements Initializable {
    public String tableName;
    private final Connection connection = Main.getConnection();
    private final LinkedList<TableColumn<Map, String>> columns = new LinkedList<>();
    private final ObservableList<Map<String, Object>> items = FXCollections.<Map<String, Object>>observableArrayList();
    private final LinkedList<String> columnNames = new LinkedList<>();
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
    private final SimpleDateFormat formatter2 = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
    private final int rowsPerPage = 8;

    @FXML
    private TableView tableView;
    @FXML
    private Button filterButton ;
    @FXML
    private Button updateButton;

    @FXML
    private Button insertButton;
    @FXML
    private Pagination pagination;

    private boolean canInsert = false;
    private boolean canUpdate = false;
    private boolean canRemove = false;

    @FXML
    private Button removeButton;

    public TableController() {
    }
    @FXML
    private void removeButtonTapped() { // нажали кнопку удаления
        Object itemToRemove = tableView.getSelectionModel().getSelectedItem();
        if (itemToRemove == null) {
            showAlert("Удаление невозможно", "Пожалуйста, выберите элемент для удаления.");
            return;
        }

        String item = itemToRemove.toString();
        int id = DBInit.getIdFrom(item);
        String deleteQuery;

        if (tableName.equals("ORDER_")) {
            deleteQuery = "DELETE FROM " + tableName + " WHERE " + tableName + "ID = " + id;
        } else {
            deleteQuery = "DELETE FROM " + tableName + " WHERE " + tableName + "_ID = " + id;
        }

        try {
            connection.delete(deleteQuery);

                tableView.getItems().remove(itemToRemove);
                loadData();

        } catch (SQLException e) {
            if (e.getMessage().contains("foreign key constraint")) {
                showAlert("Удаление невозможно", "Элемент не может быть удален из-за зависимостей.");
            } else if (e.getMessage().contains("insufficient privileges")) {
                showAlert("Ошибка удаления", "Недостаточно прав для удаления. Обратитесь к администратору.");
            } else {
                showAlert("Ошибка удаления", "Произошла ошибка при удалении элемента.");
            }
            e.printStackTrace();
        }
    }


    @FXML
    public void insertButtonTapped() {
        configureWindow(InsertMode.insert);
    } //открытие окна для вставки или удаления

    @FXML
    private void updateButtonTapped() {
        Object itemToUpdate = tableView.getSelectionModel().getSelectedItem();
        if (itemToUpdate == null) {
            showAlert("Изменение невозможно", "Пожалуйста, выберите элемент для обновления.");
            return;
        }
        configureWindow(InsertMode.update);
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) { //начальные значения для элементов интерфейса
        tableView.setItems(items);
        // Дополнительно инициализируем ваши кнопки


    }

    private Node createPage(int pageIndex) {

        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, items.size());

        // Проверяем, что fromIndex не превышает размер списка items
        if (fromIndex >= items.size() || fromIndex >= toIndex) {
            tableView.setItems(FXCollections.emptyObservableList()); // Устанавливаем пустой список
            return tableView;
        }

        // Создаем копию списка items для отображения на странице
        List<Map<String, Object>> itemsCopy = new ArrayList<>(items.subList(fromIndex, toIndex));
        tableView.setItems(FXCollections.observableList(itemsCopy));

        return tableView;


    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
        System.out.println("TABLE NAME: " + tableName);

    }

    public void configureWindow(InsertMode mode) {
        String windowName = ""; //файл FXML, который будет загружен для отображения окна.
        ChangeListener listener = (observable, oldValue, newValue) -> {
            try {
                loadData();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        };

        Tables tableType = Tables.getTableByName(tableName);
        windowName = tableType.getWindowName();
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        Parent root = null;
        try {
            root = loader.load(getClass().getResourceAsStream(windowName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        InsertController controller = loader.getController();
        controller.setListener(listener);
        controller.setMode(mode);

        if (mode == InsertMode.update) {
            Object itemToUpdate = tableView.getSelectionModel().getSelectedItem();
            String item = itemToUpdate.toString();
            controller.setItem(item);
            stage.setTitle("Update " + tableName);
        } else {
            stage.setTitle("Insert to " + tableName);
        }
        assert root != null;
        stage.setScene(new Scene(root));
        stage.show();
    } // создаёт окно для добавления/изменения







    public void checkPrivileges(String tableName) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // Проверка наличия соединения с базой данных
            if (connection == null) {
                System.out.println("Соединение с базой данных не установлено.");
                return;
            }

            // Проверка права на INSERT
            String insertPrivilegeQuery = "SELECT COUNT(*) FROM user_tab_privs WHERE table_name = ? AND privilege = 'INSERT'";
            pstmt = connection.prepareStatement(insertPrivilegeQuery);
            pstmt.setString(1, tableName.toUpperCase());
            rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                this.canInsert = true;
            }
            rs.close();
            pstmt.close();

            // Проверка права на UPDATE
            String updatePrivilegeQuery = "SELECT COUNT(*) FROM user_tab_privs WHERE table_name = ? AND privilege = 'UPDATE'";
            pstmt = connection.prepareStatement(updatePrivilegeQuery);
            pstmt.setString(1, tableName.toUpperCase());
            rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                this.canUpdate = true;
            }

            // Проверка права на DELETE
            String deletePrivilegeQuery = "SELECT COUNT(*) FROM user_tab_privs WHERE table_name = ? AND privilege = 'DELETE'";
            pstmt = connection.prepareStatement(deletePrivilegeQuery);
            pstmt.setString(1, tableName.toUpperCase());
            rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                this.canRemove = true;
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadData() throws SQLException { // Загружает данные из бд

        checkPrivileges(tableName);

        if (!tableName.equals("ORDER_")) { // Если таблица не "ORDER_", то кнопка фильтра отключается.
            filterButton.setDisable(true);
        }
        if (!canInsert) {
            insertButton.setDisable(true);
        }
        if (!canUpdate) {
            updateButton.setDisable(true);
        }
        if (!canRemove) {
            removeButton.setDisable(true);
        }



        items.clear();
        columns.clear();
        String operation;
        if (tableName.equals("ORDER_")) {
            operation = "SELECT ord.order_id, med.title || '  (' || pat.PATIENT_FIRSTNAME || ' ' || pat.PATIENT_SURNAME || ')  ' as prescription, ord.is_ready, ord.is_received, ord.start_date, empl.PHARMACY_EMPLOYEE_FIRSTNAME || ' ' || empl.PHARMACY_EMPLOYEE_SURNAME as pharmacy_employee " +
                    "FROM order_ ord " +
                    "INNER JOIN PRESCRIPTION pr ON ord.PRESCRIPTION_id = pr.PRESCRIPTION_id " +
                    "INNER JOIN PATIENT pat ON pr.PATIENT_ID = pat.PATIENT_ID "+
                    "INNER JOIN PHARMACY_EMPLOYEE empl ON ord.PHARMACY_EMPLOYEE_ID = empl.PHARMACY_EMPLOYEE_ID "+
                    "INNER JOIN medicament med " +
                    "ON med.medicament_id = pr.med_id";
        } else if (tableName.equals("PRESCRIPTION")) {
            operation = "select pr.prescription_id, med.title, pr.count, pr.direction_for_use, pr.diagnosis, doc.doctor_firstname || ' ' || doc.doctor_surname as doctor_name, pat.patient_firstname || ' ' || pat.patient_surname as patient_name " +
                    "from prescription pr " +
                    "INNER JOIN medicament med " +
                    "ON med.medicament_id = pr.med_id " +
                    "inner join doctor doc " +
                    "on doc.doctor_id = pr.doctor_id " +
                    "inner join patient pat " +
                    "on pat.patient_id = pr.patient_id";
        } else if (tableName.equals("REQUEST")) {
            operation = "SELECT request.request_id, provider.provider_name " +
                    "FROM request " +
                    "INNER JOIN provider " +
                    "ON provider.provider_id = request.provider_id";

        }  else if (tableName.equals("MEDICAMENT")) {
            operation = "select med.medicament_id, med.title, med.usage, med.volume, med.critical_rate, mt.type_name " +
                    "from medicament med " +
                    "inner join medicament_type mt" +
                    " on med.type_id = mt.medicament_type_id";
        } else if (tableName.equals("STORAGE"))  {
            operation = "select storage_id, med.title, shelf_life, actual_balance, price " +
                    "from storage " +
                    "inner join medicament med " +
                    "on med.medicament_id = storage.medicament_id ";
        } else if (tableName.equals("PREPARATION_TECHNOLOGY"))  {
            operation = "select preparation_technology_id, med.title,  method_of_preparation " +
                    "from PREPARATION_TECHNOLOGY " +
                    "inner join medicament med " +
                    "on med.medicament_id = medic_id ";
        } else if (tableName.equals("REQUEST_STRUCTURE")) {
            operation = "select req.REQUEST_STRUCTURE_ID, req.REQUEST_ID, pr.PROVIDER_NAME,  med.title,  req.amount " +
                    "from REQUEST_STRUCTURE req " +
                    " inner join REQUEST r " +
                    " on req.REQUEST_ID = r.REQUEST_ID " +

                    " inner join provider pr " +
                    " on r.PROVIDER_ID = pr.PROVIDER_ID " +

                    "inner join medicament med " +
                    "on med.medicament_id = req.med_id ";

        }else if (tableName.equals("MEDICAMENT_PREPARATION_RELATIONSHIP")) {
            operation = "select rel.RELATIONSHIP_ID,  pr.PREPARATION_TECHNOLOGY_ID || ' (' || med.title || ')' as PREPARATION_TECHNOLOGY,  m.title, rel.count " +
                    "from MEDICAMENT_PREPARATION_RELATIONSHIP rel " +
                    "INNER JOIN PREPARATION_TECHNOLOGY pr ON pr.PREPARATION_TECHNOLOGY_ID = rel.PREPARATION_TECHNOLOGY_ID " +
                    "inner join medicament med " +
                    "on med.medicament_id = pr.medic_id "+
                    "inner join medicament m " +
                    "on m.medicament_id = rel.medicament_id ";

        }
        else {
            operation = "SELECT * FROM " + tableName;
        }



        ResultSet set = connection.executeQueryAndGetResult(operation);
        ResultSetMetaData metaData = set.getMetaData();
        int columnSize = set.getMetaData().getColumnCount();
        try {
            for(int i = 1; i <= columnSize; i++) {
                String columnName = metaData.getColumnName(i);
                TableColumn<Map, String> column = new TableColumn<>(columnName);
                column.setCellValueFactory(new MapValueFactory<>(columnName));
                column.setMinWidth(40);
                columns.add(column);
                columnNames.add(columnName);
            }
            tableView.getColumns().setAll(columns);

            while (set.next()) {
                Map<String, Object> map = new HashMap<>();
                for(int j = 1; j <= columnSize; j++) {
                    String value = set.getString(j);
                    if (value == null) {
                        value = "";
                    }
                    try {
                        Date date = formatter.parse(value);
                        value = formatter2.format(date);
                    } catch (ParseException ignore) {
                    }
                    map.put(columnNames.get(j - 1), value);
                }
                items.add(map);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        int totalRows = items.size(); // Получаем общее количество записей из списка items

        // Рассчитываем общее количество страниц на основе количества записей и размера страницы
        int totalPages = (int) Math.ceil((double) totalRows / rowsPerPage);

        // Устанавливаем общее количество страниц в пагинации
        pagination.setPageCount(totalPages);

        pagination.setPageFactory(this::createPage);


    }


    @FXML
    public void filterButtonTapped () throws IOException {
        Stage stage = new Stage();
        InputStream inputStream = getClass().getResourceAsStream("/ru/nsu/pharmacydatabase/windows/filter/order_filter.fxml");
        Parent root = new FXMLLoader().load(inputStream);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}