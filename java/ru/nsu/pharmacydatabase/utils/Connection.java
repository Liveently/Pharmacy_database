package ru.nsu.pharmacydatabase.utils;

import javafx.scene.control.Alert;

import java.sql.*;
import java.util.*;
import java.util.function.Function;

public class Connection {
    private java.sql.Connection connection;

    private static final String databaseURL = "localhost";

    private static final String defaultPort = "1522";


    private static final String dbName = "ord";
    private static final String driver = "oracle.jdbc.driver.OracleDriver";


    public Connection() {
    }

    public java.sql.Connection getConnection() {
        return connection;
    }

    public void registerConnection(String login, String password) throws SQLException, ClassNotFoundException { //регистрирует соединение - выведет сообщение о результате
        Class.forName(driver);
        Properties props = new Properties();
        props.setProperty("user", login);
        props.setProperty("password", password);
        createConnection();
        System.out.println("register connection to " + databaseURL + "...");
        connection = DriverManager.getConnection("jdbc:oracle:thin:@"+databaseURL +":"+defaultPort+ ":"+dbName, props);
        if (connection.isValid(1)) {
            System.out.println("success connection to " + databaseURL);
        } else {
            System.out.println("bad connection to " + databaseURL);
        }
    }

    private void createConnection() { //создает соединение
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        TimeZone timeZone = TimeZone.getTimeZone("GMT+7");
        TimeZone.setDefault(timeZone);
        Locale.setDefault(Locale.ENGLISH);
    }

    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
            System.out.println("connection was closed");
        } else {
            System.out.println("connection is not registered");
        }
    }

    public ResultSet executeQueryAndGetResult(String sql) { //Выполняет SQL-запрос и возвращает результат запроса в виде ResultSet
        createConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate(sql);
            return preparedStatement.executeQuery();
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    public void executeQuery(String sql) throws SQLException {  //Выполняет SQL-запрос без возврата результата.
        createConnection();
        try(PreparedStatement preStatement = connection.prepareStatement(sql)) {
            preStatement.executeUpdate(sql);
        }
    }

    public void insert(List<String> queryList) { //Выполняет список INSERT-запросов в базу данных.
        createConnection();
        for(String query : queryList) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.executeUpdate(query);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<List<String>> select(String sql) { //Выполняет SELECT-запрос и возвращает результат в виде списка списков строк.
        return select(sql, result -> {
            try {
                ArrayList<String> list = new ArrayList<>(1);
                list.add(result.getString(1));
                return list;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public List<List<String>> select(String sql, Function<ResultSet, List<String>> toString) { //Выполняет SELECT-запрос и применяет функцию toString к каждой строке результата.
        createConnection();
        List<List<String>> names = new LinkedList<>();
        try (Statement statement = connection.createStatement()) {
            ResultSet result = statement.executeQuery(sql);
            while (result.next()) {
                names.add(toString.apply(result));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return names;
    }


    public boolean delete(String sql) {
        // Выполняет DELETE-запрос для удаления данных из базы данных.
        createConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0; // Возвращаем true, если было затронуто хотя бы одно количество строк.
        } catch (SQLException e) {
            if (e.getMessage().contains("insufficient privileges")) {
                showAlert("Ошибка", "Недостаточно прав для выполнения запроса DELETE. Обратитесь к администратору.", Alert.AlertType.ERROR);
            } else {
                showAlert("Ошибка", "Произошла ошибка при выполнении запроса DELETE: " + e.getMessage(), Alert.AlertType.ERROR);
            }
            return false;
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {

        PreparedStatement pstmt = connection.prepareStatement(sql);

        // Возвращаем созданный PreparedStatement
        return pstmt;
    }


}