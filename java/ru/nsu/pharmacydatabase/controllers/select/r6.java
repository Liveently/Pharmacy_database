package ru.nsu.pharmacydatabase.controllers.select;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class r6 implements SelectController {
    @FXML
    public Button listButton;


    @FXML
    private void listButtonTapped() {
        String sql = "SELECT m.title AS medicament_title,  " +
                "       m.critical_rate AS critical_rate, " +
                "       COALESCE(SUM(s.actual_balance), 0) AS total_balance, " +
                "       mt.type_name AS medicament_type " +
                "FROM medicament m " +
                "LEFT JOIN storage s ON s.medicament_id = m.medicament_id  " +
                "LEFT JOIN medicament_type mt ON m.type_id = mt.medicament_type_id  " +
                "GROUP BY m.medicament_id, m.title, m.critical_rate, mt.type_name " +
                "HAVING COALESCE(SUM(s.actual_balance), 0) <= m.critical_rate ";
        showResult(sql);
    }


}
