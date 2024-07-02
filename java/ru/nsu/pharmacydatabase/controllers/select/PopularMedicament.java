package ru.nsu.pharmacydatabase.controllers.select;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import ru.nsu.pharmacydatabase.utils.DBInit;

import java.net.URL;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class PopularMedicament implements SelectController, Initializable {
    @FXML
    public Button listButton;
    @FXML
    public ChoiceBox choiceBox;
    @FXML
    public ChoiceBox pageChoiceBox;


    private DBInit dbInit;
    private ObservableList<String> items = FXCollections.<String>observableArrayList();
    private ObservableList<String> itemsPage = FXCollections.<String>observableArrayList();
    private Map<String, Integer> MedicamentType;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbInit = new DBInit(connection);
        choiceBox.setItems(items);
        pageChoiceBox.setItems(itemsPage);
        try {
            ResultSet set = connection.executeQueryAndGetResult("select * from medicament_type");
            MedicamentType = new HashMap<>();
            items.clear();
            items.add("all");
            MedicamentType.put("all", 0);
            itemsPage.add("5");
            itemsPage.add("10");

            if (set != null) {
                while (set.next()) {
                    String name = set.getString(2);
                    Integer id = set.getInt(1);
                    MedicamentType.put(name, id);
                    items.add(name);
                }
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void listButtonTapped() {
        if (choiceBox.getSelectionModel().isEmpty() || pageChoiceBox.getSelectionModel().isEmpty()) {
            showAlert("empty!", "fields cannot be empty");
        } else if (MedicamentType.get(choiceBox.getValue().toString()) == 0 && pageChoiceBox.getValue().equals("5")) {
            String sql = "SELECT * " +
                    "FROM ( " +
                    "    SELECT tmp.*, rownum rn " +
                    "    FROM ( " +
                    "        SELECT title " +
                    "        FROM ( " +
                    "            SELECT med.title, COUNT(pr.med_id) AS c " +
                    "            FROM medicament med " +
                    "            INNER JOIN prescription pr ON pr.med_id = med.medicament_id " +
                    "            GROUP BY med.title " +
                    "            ORDER BY c DESC " +
                    "        ) " +
                    "    ) tmp " +
                    "    WHERE rownum <= 5 " +
                    ") " +
                    "WHERE rn > 0";
            showResult(sql);
        } else if (MedicamentType.get(choiceBox.getValue().toString()) == 0 && pageChoiceBox.getValue().equals("10")) {
            String sql = "SELECT * " +
                    "FROM ( " +
                    "    SELECT tmp.*, rownum rn " +
                    "    FROM ( " +
                    "        SELECT title " +
                    "        FROM ( " +
                    "            SELECT med.title, COUNT(pr.med_id) AS c " +
                    "            FROM medicament med " +
                    "            INNER JOIN prescription pr ON pr.med_id = med.medicament_id " +
                    "            GROUP BY med.title " +
                    "            ORDER BY c DESC " +
                    "        ) " +
                    "    ) tmp " +
                    "    WHERE rownum <= 10 " +
                    ") " +
                    "WHERE rn > 0";
            showResult(sql);
        } else if (pageChoiceBox.getValue().equals("5")){
            String sql = "SELECT * " +
                    "FROM ( " +
                    "    SELECT tmp.*, rownum rn " +
                    "    FROM ( " +
                    "        SELECT med.title " +
                    "        FROM medicament med " +
                    "        INNER JOIN prescription pr ON pr.med_id = med.medicament_id " +
                    "        INNER JOIN medicament_type mt ON mt.medicament_type_id = med.type_id " +
                    "        WHERE mt.type_name = ? " +
                    "        GROUP BY med.title " +
                    "        ORDER BY COUNT(pr.med_id) DESC " +
                    "    ) tmp " +
                    "    WHERE rownum <= 5 " +
                    ") " +
                    "WHERE rn > 0";

            showResult(sql, choiceBox.getValue().toString());
        } else if (pageChoiceBox.getValue().equals("10")){
            String sql = "SELECT * " +
                    "FROM ( " +
                    "    SELECT tmp.*, rownum rn " +
                    "    FROM ( " +
                    "        SELECT med.title " +
                    "        FROM medicament med " +
                    "        INNER JOIN prescription pr ON pr.med_id = med.medicament_id " +
                    "        INNER JOIN medicament_type mt ON mt.medicament_type_id = med.type_id " +
                    "        WHERE mt.type_name = ? " +
                    "        GROUP BY med.title " +
                    "        ORDER BY COUNT(pr.med_id) DESC " +
                    "    ) tmp " +
                    "    WHERE rownum <= 10 " +
                    ") " +
                    "WHERE rn > 0";

            showResult(sql, choiceBox.getValue().toString());
        }

    }

}
