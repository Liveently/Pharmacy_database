package ru.nsu.pharmacydatabase.controllers.select;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class r9 implements SelectController {
    @FXML
    public Button listButton;
    @FXML
    public Button numButton;

    @FXML
    private void listButtonTapped() {
        String sql = "SELECT med.title , mpr.count AS required_count, pt.preparation_technology_id, mm.title as preparation_technology, pt.method_of_preparation " +
                "FROM order_ ord " +
                "INNER JOIN prescription pr ON ord.prescription_id = pr.prescription_id " +
                "INNER JOIN medicament mm ON pr.med_id = mm.medicament_id " +
                "INNER JOIN preparation_technology pt ON pt.medic_id = mm.medicament_id " +
                "INNER JOIN medicament_preparation_relationship mpr ON pt.preparation_technology_id = mpr.preparation_technology_id " +
                "INNER JOIN medicament med ON mpr.medicament_id = med.medicament_id " +
                "WHERE ord.is_ready = 'NO' ";
        showResult(sql);
    }

    @FXML
    private void numButtonTapped() {
        String sql = "SELECT SUM(mpr.count) AS total_required_count " +
                "FROM order_ ord " +
                "INNER JOIN prescription pr ON ord.prescription_id = pr.prescription_id " +
                "INNER JOIN medicament mm ON pr.med_id = mm.medicament_id " +
                "INNER JOIN preparation_technology pt ON pt.medic_id = mm.medicament_id " +
                "INNER JOIN medicament_preparation_relationship mpr ON pt.preparation_technology_id = mpr.preparation_technology_id " +
                "INNER JOIN medicament med ON mpr.medicament_id = med.medicament_id " +
                "WHERE ord.is_ready = 'NO' " +
                "GROUP BY med.title, pt.preparation_technology_id, mm.title, pt.method_of_preparation ";
        showResult(sql);
    }
}
