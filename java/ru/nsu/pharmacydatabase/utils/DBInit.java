package ru.nsu.pharmacydatabase.utils;

import javafx.scene.control.Alert;
import ru.nsu.pharmacydatabase.utils.Connection;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DBInit {
    private static final String[] tableNamesArray = {
                    "Doctor.sql",
                    "medicament_preparation_relationship.sql",
                    "Medicament.sql",
                    "Medicament_type.sql",
                    "Order_.sql",
                    "Patient.sql",
                    "Pharmacy_employee.sql",
                    "Preparation_technology.sql",
                    "Prescription.sql",
                    "Provider.sql",
                    "Request.sql",
                    "Request_structure.sql",
                    "Storage.sql"
            };

    private final Connection connection;
    private final List<String> tablesName;

    public DBInit(Connection connection) {
        this.connection = connection;
        tablesName = new LinkedList<>();
        tablesName.addAll(Arrays.asList(tableNamesArray));
    }

    public void clear() throws SQLException {
        System.out.println("..drop table..");
        dropTables();
        System.out.println("..drop sequences..");
        dropSequences();
    }

    public void init() throws SQLException {
        if (!isDatabaseEmpty()) {
            showAlert("Database is not empty. Please reset the database first.");
            return;
        }
        System.out.println("..creating table..");
        createTables();
        createTriggers();
        System.out.println("..creating sequences..");
        createSequences();
        insertInfo();
        createProcedure();
        System.out.println("..adding base information..");
        System.out.println("-----database successfully created-----");
    }

    private boolean isDatabaseEmpty() throws SQLException {
        String checkTablesQuery = "SELECT COUNT(*) FROM user_tables";
        try (PreparedStatement preparedStatement = connection.getConnection().prepareStatement(checkTablesQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                int tableCount = resultSet.getInt(1);
                return tableCount == 0;
            }
        }
        return false;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Database Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static int getIdFrom(String item) {  //возвращиет число из id
        Integer id = Integer.valueOf(getSubstring(" ID=", "ID=", item));
        return id.intValue();
    }

    public static String getSubstring(String start1, String start2, String item) {
        String start = start1;
        int substringStartIndex = item.indexOf(start);
        if (substringStartIndex < 0) {
            start = start2;
            substringStartIndex = item.indexOf(start);
        }

        int endIndex = item.indexOf(',', substringStartIndex);

        int nextIndex = endIndex + 1;

        while (nextIndex < item.length() && Character.isWhitespace(item.charAt(nextIndex))) {
            nextIndex++;
        }
        if (nextIndex < item.length() && Character.isDigit(item.charAt(nextIndex))) {
            int nextCommaIndex = item.indexOf(',', nextIndex);
            if (nextCommaIndex != -1) {
                endIndex = nextCommaIndex;
            }
        }


        if (endIndex < 0) {
            endIndex = item.indexOf('}', substringStartIndex);
        }

        return item.substring(substringStartIndex + start.length(), endIndex);
    }

    private void execute(List<String> queries) {
        for (String query: queries) {
            try {
                connection.executeQuery(query);
            } catch (SQLIntegrityConstraintViolationException ignored) {
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void createTriggers() throws SQLException {
        String trigger1 = "CREATE OR REPLACE TRIGGER types " +
                "before INSERT ON medicament_type " +
                "FOR each row " +
                "BEGIN  " +
                "SELECT sq_medicament_type.NEXTVAL  " +
                "INTO :new.medicament_type_id  FROM dual; " +
                "END;";
        connection.executeQuery(trigger1);
    }

    public void createProcedure() throws SQLException {
        String sql = "create or replace procedure get_const_values( " +
                "min_date out date, " +
                "max_date out date, " +
                "default_date out date " +
                ") " +
                "is " +
                "begin " +
                "min_date := to_date('01-01-1800', 'dd-mm-yyyy'); " +
                "max_date := to_date('01-01-4021', 'dd-mm-yyyy'); " +
                "default_date := sysdate; " +
                "end;";
        connection.executeQuery(sql);
        String sql1 = "create or replace procedure get_min_time( " +
                "min_date out date " +
                ") " +
                "is " +
                "begin " +
                " min_date := to_date('01-01-1800', 'dd-mm-yyyy'); " +
                " end;";
        connection.executeQuery(sql1);
    }


    public void createIndex(String tableName, String columnName, String indexName) {
        PreparedStatement preparedStatement = null;
        String sqlCreateIndex = "CREATE INDEX " + indexName +
                " ON " + tableName + " (" + columnName + ") ";
        try {
            preparedStatement = connection.getConnection().prepareStatement(sqlCreateIndex);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Can't create index: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close PreparedStatement: " + e.getMessage());
                }
            }
        }
    }

    public void dropTables() {
        dropTable("order_");
        dropTable("pharmacy_employee");
        dropTable("medicament_preparation_relationship");
        dropTable("preparation_technology");
        dropTable("request_structure");
        dropTable("storage");
        dropTable("medicament");
        dropTable("prescription");
        dropTable("doctor");
        dropTable("patient");
        dropTable("medicament_type");
        dropTable("request");
        dropTable("provider");
    }




    public void dropTable(String tableName) {
        PreparedStatement preparedStatement = null;
        String checkTableExists = "SELECT table_name FROM user_tables WHERE table_name = ?";
        String sqlDropTable = "DROP TABLE " + tableName + " CASCADE CONSTRAINTS";

        try {
            preparedStatement = connection.getConnection().prepareStatement(checkTableExists);
            preparedStatement.setString(1, tableName.toUpperCase());
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                // Таблица существует, пытаемся удалить
                preparedStatement = connection.getConnection().prepareStatement(sqlDropTable);
                preparedStatement.executeUpdate();
                System.out.println(tableName + " table dropped successfully.");
            } else {
                System.out.println("Table " + tableName + " does not exist.");
            }
        } catch (SQLException e) {
            System.err.println("Can't drop " + tableName + " table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void dropSequences() {
        dropSeq("sq_order");
        dropSeq("sq_pharmacy_employee");
        dropSeq("sq_medicament_preparation_relationship");
        dropSeq("sq_preparation_technology");
        dropSeq("sq_request_structure");
        dropSeq("sq_storage");
        dropSeq("sq_medicament");
        dropSeq("sq_prescription");
        dropSeq("sq_doctor");
        dropSeq("sq_patient");
        dropSeq("sq_medicament_type");
        dropSeq("sq_request");
        dropSeq("sq_provider");
    }

    public void dropSeq(String seqName) {
        String sqlDropSeq = "DROP SEQUENCE " + seqName;
        String checkSeqExists = "SELECT sequence_name FROM user_sequences WHERE sequence_name = ?";

        try (PreparedStatement checkStatement = connection.getConnection().prepareStatement(checkSeqExists)) {
            checkStatement.setString(1, seqName.toUpperCase());
            ResultSet rs = checkStatement.executeQuery();

            if (rs.next()) {
                // Последовательность существует, пытаемся удалить
                try (PreparedStatement dropStatement = connection.getConnection().prepareStatement(sqlDropSeq)) {
                    dropStatement.executeUpdate();
                    System.out.println(seqName + " sequence dropped successfully.");
                } catch (SQLException e) {
                    System.err.println("Can't drop " + seqName + " sequence: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("Sequence " + seqName + " does not exist.");
            }
        } catch (SQLException e) {
            System.err.println("Error checking existence of " + seqName + " sequence: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void createTables() {
        PreparedStatement preparedStatement = null;

        String sqlCreateProviderTable = "CREATE TABLE provider ( " +
                "provider_id NUMBER(10) PRIMARY KEY, " +
                "provider_name VARCHAR(128) NOT NULL, " +
                "provider_address VARCHAR(128) NOT NULL, " +
                "products VARCHAR(512) NOT NULL " +
                ")";
        try {
            preparedStatement = connection.getConnection().prepareStatement(sqlCreateProviderTable);
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            System.out.println("can't create provider table");
            throwables.printStackTrace();
        }

        String sqlCreateRequestTable = "CREATE TABLE request ( " +
                "request_id NUMBER(10) PRIMARY KEY, " +
                "provider_id NUMBER(10) NOT NULL, " +
                "CONSTRAINT provider_id FOREIGN KEY (provider_id) REFERENCES provider(provider_id) " +
                ")";
        try {
            preparedStatement = connection.getConnection().prepareStatement(sqlCreateRequestTable);
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            System.out.println("can't create request table");
            throwables.printStackTrace();
        }

        String sqlCreateMedicamentTypeTable = "CREATE TABLE medicament_type ( " +
                "medicament_type_id NUMBER(10) PRIMARY KEY, " +
                "type_name VARCHAR(128) NOT NULL " +
                ")";
        try {
            preparedStatement = connection.getConnection().prepareStatement(sqlCreateMedicamentTypeTable);
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            System.out.println("can't create medicament_type table");
            throwables.printStackTrace();
        }

        String sqlCreatePatientTable = "CREATE TABLE patient ( " +
                "patient_id NUMBER(10) PRIMARY KEY, " +
                "patient_firstname VARCHAR(128) NOT NULL , " +
                "patient_surname VARCHAR(128) NOT NULL , " +
                "patient_birthdate DATE, " +
                "patient_phone_number VARCHAR(11) UNIQUE NOT NULL , " +
                "patient_address VARCHAR(128) NOT NULL, " +
                "registration_date DATE DEFAULT sysdate, " +
                "CONSTRAINT patient_birthdate CHECK (patient_birthdate < registration_date) " +
                ")";
        try {
            preparedStatement = connection.getConnection().prepareStatement(sqlCreatePatientTable);
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            System.out.println("can't create patient table");
            throwables.printStackTrace();
        }

        String sqlCreateDoctorTable = "CREATE TABLE doctor ( " +
                "doctor_id NUMBER(10) PRIMARY KEY, " +
                "doctor_firstname VARCHAR(128) NOT NULL , " +
                "doctor_surname VARCHAR(128) NOT NULL " +
                ")";
        try {
            preparedStatement = connection.getConnection().prepareStatement(sqlCreateDoctorTable);
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            System.out.println("can't create doctor table");
            throwables.printStackTrace();
        }



        String sqlCreateMedicamentTable = "CREATE TABLE medicament ( " +
                "medicament_id NUMBER(10) PRIMARY KEY, " +
                "title VARCHAR(128) NOT NULL , " +
                "usage VARCHAR(128) NOT NULL , " +
                "volume NUMBER(10), " +
                "CONSTRAINT volume CHECK (volume > 1), " +
                "critical_rate NUMBER(10) NOT NULL ," +
                "type_id NUMBER(10) NOT NULL, " +
                "CONSTRAINT type_id FOREIGN KEY (type_id) REFERENCES medicament_type(medicament_type_id) " +
                   ")";
        try {
            preparedStatement = connection.getConnection().prepareStatement(sqlCreateMedicamentTable);
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            System.out.println("can't create medicament table");
            throwables.printStackTrace();
        }

        String sqlCreatePrescriptionTable = "CREATE TABLE prescription ( " +
                "prescription_id NUMBER(10) PRIMARY KEY, " +
                "count NUMBER(10), " +
                "direction_for_use VARCHAR(128), " +
                "diagnosis VARCHAR(128) NOT NULL, " +
                "doctor_id NUMBER(10) NOT NULL, " +
                "patient_id NUMBER(10) NOT NULL, " +
                "med_id NUMBER(10) NOT NULL, " +
                "CONSTRAINT med_id FOREIGN KEY (med_id) REFERENCES medicament(medicament_id), " +
                "CONSTRAINT patient_id FOREIGN KEY (patient_id) REFERENCES patient(patient_id), " +
                "CONSTRAINT doctor_id FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id) " +
                ")";
        try {
            preparedStatement = connection.getConnection().prepareStatement(sqlCreatePrescriptionTable);
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            System.out.println("can't create prescription table");
            throwables.printStackTrace();
        }

        String sqlCreateStorageTable = "CREATE TABLE storage ( " +
                "storage_id NUMBER(10) PRIMARY KEY, " +
                "medicament_id NUMBER(10) NOT NULL, " +
                "shelf_life DATE NOT NULL , " +
                "actual_balance NUMBER(10) NOT NULL , " +
                "price NUMERIC(10, 2) NOT NULL , " +
                "CONSTRAINT medicament_id FOREIGN KEY (medicament_id) REFERENCES medicament(medicament_id) " +
                ")";
        try {
            preparedStatement = connection.getConnection().prepareStatement(sqlCreateStorageTable);
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            System.out.println("can't create storage table");
            throwables.printStackTrace();
        }

        String sqlCreateRequestStructureTable = "CREATE TABLE request_structure ( " +
                "request_structure_id NUMBER(10) PRIMARY KEY, " +
                "med_id NUMBER(10) NOT NULL, " +
                "CONSTRAINT fk_med_id FOREIGN KEY (med_id) REFERENCES medicament(medicament_id), " +
                "request_id NUMBER(10) NOT NULL, " +
                "CONSTRAINT request_id FOREIGN KEY (request_id) REFERENCES request(request_id), " +
                "amount NUMBER(10) NOT NULL " +
                ")";
        try {
            preparedStatement = connection.getConnection().prepareStatement(sqlCreateRequestStructureTable);
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            System.out.println("can't create request_structure table");
            throwables.printStackTrace();
        }

        String sqlCreatePreparationTechnologyTable = "CREATE TABLE preparation_technology ( " +
                "preparation_technology_id NUMBER(10) PRIMARY KEY, " +
                "medic_id NUMBER(10) NOT NULL, " +
                "CONSTRAINT medic_id FOREIGN KEY (medic_id) REFERENCES medicament(medicament_id), " +
                "method_of_preparation VARCHAR(128) NOT NULL " +
                ")";
        try {
            preparedStatement = connection.getConnection().prepareStatement(sqlCreatePreparationTechnologyTable);
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            System.out.println("can't create preparation_technology table");
            throwables.printStackTrace();
        }



        String sqlCreateMedicamentPreparationRelationship = "CREATE TABLE medicament_preparation_relationship ( " +
                "                relationship_id NUMBER(10) PRIMARY KEY, " +
                "                medicament_id NUMBER(10) NOT NULL, " +
                "                preparation_technology_id NUMBER(10) NOT NULL, " +
                "                count NUMBER(10) NOT NULL, " +
                "                CONSTRAINT medicament_fk FOREIGN KEY (medicament_id) REFERENCES medicament(medicament_id), " +
                "                CONSTRAINT preparation_fk FOREIGN KEY (preparation_technology_id) REFERENCES preparation_technology(preparation_technology_id) " +
                "               , CONSTRAINT unique_medicament_preparation UNIQUE (medicament_id, preparation_technology_id) " +
                "        )";
        try {
            preparedStatement = connection.getConnection().prepareStatement(sqlCreateMedicamentPreparationRelationship);
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            System.out.println("can't create Medicament_preparation_relationship table");
            throwables.printStackTrace();
        }



        String sqlCreatePharmacyEmployeeTable = "CREATE TABLE pharmacy_employee ( " +
                "pharmacy_employee_id NUMBER(10) PRIMARY KEY, " +
                "pharmacy_employee_firstname VARCHAR(128) NOT NULL , " +
                "pharmacy_employee_surname VARCHAR(128) NOT NULL " +
                ")";
        try {
            preparedStatement = connection.getConnection().prepareStatement(sqlCreatePharmacyEmployeeTable);
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            System.out.println("can't create pharmacy_employee table");
            throwables.printStackTrace();
        }

        String sqlCreateOrderTable = "CREATE TABLE order_ ( " +
                "order_id NUMBER(10) PRIMARY KEY, " +
                "prescription_id NUMBER(10) NOT NULL, " +
                "CONSTRAINT prescription_fk FOREIGN KEY (prescription_id) REFERENCES prescription(prescription_id), " +
                "is_ready VARCHAR(128) NOT NULL, " +
                "CONSTRAINT is_ready_chk CHECK (is_ready IN('YES', 'NO')), " +
                "is_received VARCHAR(128) NOT NULL, " +
                "CONSTRAINT is_received_chk CHECK (is_received IN('YES', 'NO')), " +
                "start_date DATE DEFAULT sysdate, " +
                "pharmacy_employee_id NUMBER(10) NOT NULL, " +
                "CONSTRAINT pharmacy_employee_fk FOREIGN KEY (pharmacy_employee_id) REFERENCES pharmacy_employee(pharmacy_employee_id) " +
                ")";
        try {
            preparedStatement = connection.getConnection().prepareStatement(sqlCreateOrderTable);
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            System.out.println("can't create order_ table");
            throwables.printStackTrace();
        }
    }

    public void createSequences() throws SQLException {
        String sqlCreateProviderSequence = "CREATE SEQUENCE sq_provider " +
                "MINVALUE 1 " +
                "START WITH 1 " +
                "INCREMENT BY 1 " +
                "NOMAXVALUE ";
        String sqlCreateRequestSequence = "CREATE SEQUENCE sq_request " +
                "MINVALUE 1 " +
                "START WITH 1 " +
                "INCREMENT BY 1 " +
                "NOMAXVALUE";
        String sqlCreateMedicamentTypeSequence = "CREATE SEQUENCE sq_medicament_type " +
                "MINVALUE 1 " +
                "START WITH 1 " +
                "INCREMENT BY 1 " +
                "NOMAXVALUE";
        String sqlCreatePatientSequence = "CREATE SEQUENCE sq_patient " +
                "MINVALUE 1 " +
                "START WITH 1 " +
                "INCREMENT BY 1 " +
                "NOMAXVALUE";
        String sqlCreateDoctorSequence = "CREATE SEQUENCE sq_doctor " +
                "MINVALUE 1 " +
                "START WITH 1 " +
                "INCREMENT BY 1 " +
                "NOMAXVALUE";
        String sqlCreatePrescriptionSequence = "CREATE SEQUENCE sq_prescription " +
                "MINVALUE 1 " +
                "START WITH 1 " +
                "INCREMENT BY 1 " +
                "NOMAXVALUE";
        String sqlCreateMedicamentSequence = "CREATE SEQUENCE sq_medicament " +
                "MINVALUE 1 " +
                "START WITH 1 " +
                "INCREMENT BY 1 " +
                "NOMAXVALUE";
        String sqlCreateStorageSequence = "CREATE SEQUENCE sq_storage " +
                "MINVALUE 1 " +
                "START WITH 1 " +
                "INCREMENT BY 1 " +
                "NOMAXVALUE";
        String sqlCreateRequestStructureSequence = "CREATE SEQUENCE sq_request_structure " +
                "MINVALUE 1 " +
                "START WITH 1 " +
                "INCREMENT BY 1 " +
                "NOMAXVALUE";
        String sqlCreatePreparationTechnologySequence = "CREATE SEQUENCE sq_preparation_technology " +
                "MINVALUE 1 " +
                "START WITH 1 " +
                "INCREMENT BY 1 " +
                "NOMAXVALUE";
        String sqlCreateMedicamentPreparationRelationshipSequence = "CREATE SEQUENCE sq_medicament_preparation_relationship " +
                "MINVALUE 1 " +
                "START WITH 1 " +
                "INCREMENT BY 1 " +
                "NOMAXVALUE";
        String sqlCreatePharmacyEmployeeSequence = "CREATE SEQUENCE sq_pharmacy_employee " +
                "MINVALUE 1 " +
                "START WITH 1 " +
                "INCREMENT BY 1 " +
                "NOMAXVALUE";
        String sqlCreateOrderSequence = "CREATE SEQUENCE sq_order " +
                "MINVALUE 1 " +
                "START WITH 1 " +
                "INCREMENT BY 1 " +
                "NOMAXVALUE";

        PreparedStatement provider = connection.getConnection().prepareStatement(sqlCreateProviderSequence);
        PreparedStatement request = connection.getConnection().prepareStatement(sqlCreateRequestSequence);
        PreparedStatement medicamentType = connection.getConnection().prepareStatement(sqlCreateMedicamentTypeSequence);
        PreparedStatement patient = connection.getConnection().prepareStatement(sqlCreatePatientSequence);
        PreparedStatement doctor = connection.getConnection().prepareStatement(sqlCreateDoctorSequence);
        PreparedStatement prescription = connection.getConnection().prepareStatement(sqlCreatePrescriptionSequence);
        PreparedStatement medicament = connection.getConnection().prepareStatement(sqlCreateMedicamentSequence);
        PreparedStatement storage = connection.getConnection().prepareStatement(sqlCreateStorageSequence);
        PreparedStatement requestStructure = connection.getConnection().prepareStatement(sqlCreateRequestStructureSequence);
        PreparedStatement preparationTechnology = connection.getConnection().prepareStatement(sqlCreatePreparationTechnologySequence);
        PreparedStatement pharmacyEmployee = connection.getConnection().prepareStatement(sqlCreatePharmacyEmployeeSequence);
        PreparedStatement order = connection.getConnection().prepareStatement(sqlCreateOrderSequence);
        PreparedStatement medicamentPreparationRelationship = connection.getConnection().prepareStatement(sqlCreateMedicamentPreparationRelationshipSequence);

        provider.executeUpdate();
        request.executeUpdate();
        medicamentType.executeUpdate();
        patient.executeUpdate();
        doctor.executeUpdate();
        prescription.executeUpdate();
        medicament.executeUpdate();
        storage.executeUpdate();
        requestStructure.executeUpdate();
        preparationTechnology.executeUpdate();
        pharmacyEmployee.executeUpdate();
        order.executeUpdate();
        medicamentPreparationRelationship.executeUpdate();
    }

    public void insertInfo() {
        insertProvider("Главфарм", "Московская, 25", "Абуцел; Авиа-море; Агри; Адаптол; Адвантан; Аевит; Бронхикум; Виброцил; Диазолин");
        insertProvider("Биоритм", "Черемуховая, 15", "Кромицил; Димедрол; Преднизолон; Лоратадин; Давзолин");
        insertProvider("Интермедсервис", "Малая Калужская 15/16", "ПАРАЦЕТ; ПАЦЕТАМОЛ АВЕКСИМА; ПАРАЦЕТАМОЛ ВЕЛФАРМ; ПАРАЦЕТАМОЛ ДЕТСКИЙ; Декстрометорфан; ПАРАЦЕТАМОЛ МЕДИСОРБ");
        insertProvider("Гарант", "Морской проспект, 22", "Пабал; Панкреатин; Назол; Найсулид; Найз; Нанопласт; Бронхиред");
        insertProvider("Медицина Альба", "Лунная, 19", "КАВИНТАЗОЛ; КАВИНТОН; КАВИНТОН КОМФОРТЕ; КАВИНТОН ФОРТЕ; КАВИТ ЮНИОР; КАГОЦЕЛ; КАДМИУМ МЕТАЛЛИКУМ; КАДМИУМ СУЛЬФУРИКУМ");

        insertRequest(1);
        insertRequest(3);
        insertRequest(5);
        insertRequest(1);
        insertRequest(2);
        insertRequest(3);
        insertRequest(4);
        insertRequest(4);
        insertRequest(3);

        insertMedicamentType("Аллергия");
        insertMedicamentType("Витимины");
        insertMedicamentType("Дерматология");
        insertMedicamentType("ЖКТ");
        insertMedicamentType("Нервная система");
        insertMedicamentType("Обезболивающие");
        insertMedicamentType("Противовирусные");
        insertMedicamentType("Простуда и грипп");

        insertPatient("Василий", "Иванов", "16-12-1995", "81936481735", "Пирогова, 19", "17-01-2024");
        insertPatient("Анастасия", "Кудина", "16-10-1989", "88295610821", "Брестская, 9", "20-04-2024");
        insertPatient("Александра", "Баранова", "19-05-1999", "81209067890", "Ленина, 15", "29-04-2024");
        insertPatient("Николай", "Ремезов", "06-01-2000", "88295618201", "Морская, 91", "30-03-2024");
        insertPatient("Петр", "Дворников", "16-08-1960", "89125618201", "Терешкова, 29", "18-03-2024");
        insertPatient("Василиса", "Сидникова", "26-08-1989", "89128163201", "Пирога, 15", "28-04-2024");
        insertPatient("Иван", "Шишкин", "17-09-1998", "81234567890", "Комсомольская, 4", "28-04-2024");

        insertDoctor("Борис", "Петров");
        insertDoctor("Кирилл", "Лукомский");
        insertDoctor("Евгений", "Бочаров");
        insertDoctor("Марина", "Александрова");
        insertDoctor("Вероника", "Дым");

        insertMedicament("Назол", "внутрь", 36, 10, 8);
        insertMedicament("Аевит", "внутрь", 24, 5, 2);
        insertMedicament("Пантенол", "для наружнего применения", 72, 10, 3);
        insertMedicament("Граммидин", "внутрь", 72, 5, 8);
        insertMedicament("Найз", "для наружнего применения", 72, 7, 6);
        insertMedicament("Найз", "для наружнего применения", 72, 7, 6);
        insertMedicament("Фестал", "внутрь", 72, 16, 4);
        insertMedicament("Бепантен", "для наружнего применения", 32, 10, 3);
        insertMedicament("Зодак", "для наружнего применения", 32, 15, 1);
        insertMedicament("Супрастин", "внутрь", 72, 11, 1);
        insertMedicament("Супрастин", "внутрь", 72, 5, 1);
        insertMedicament("Зодак", "для наружнего применения", 32, 10, 1);
        insertMedicament("Кларитин", "для наружнего применения", 32, 12, 1);


        insertPrescription(2, "после еды, внутрь", "ангина", 1, 1, 1);
        insertPrescription(1, "для наружнего применения", "ушиб", 2, 4, 5);
        insertPrescription(1, "для наружнего применения", "ушиб", 2, 2, 3);
        insertPrescription(3, "во время еды, внутрь", "аллергия", 3, 3, 1);
        insertPrescription(2, "для наружнего применения", "порез", 4, 5, 6);

        insertStorage(1, "01-12-2024", 6, 711.0);
        insertStorage(2, "01-01-2025", 0, 191.0);
        insertStorage(3, "01-01-2025", 18, 891.0);
        insertStorage(4, "01-01-2025", 20, 789.0);
        insertStorage(5, "01-01-2025", 10, 509.0);
        insertStorage(6, "01-01-2025", 11, 98.0);
        insertStorage(7, "01-01-2025", 9, 100.0);

        insertRequestStructure(1, 1, 10);
        insertRequestStructure(2, 4, 10);

        insertPreparationTechnology(1, "технология приготовления 1");
        insertPreparationTechnology(2, "технология приготовления 2");
        insertPreparationTechnology(3, "технология приготовления 3");
        insertPreparationTechnology(4, "технология приготовления 4");
        insertPreparationTechnology(5, "технология приготовления 5");


        insertPharmacyEmployee("Мария", "Константинова");

        insertOrder(1, "YES", "NO", "28-04-2024", 1);
        insertOrder(2, "NO", "NO", "20-04-2024", 1);
        insertOrder(3, "NO", "YES", "25-04-2024", 1);
        insertOrder(4, "YES", "YES", "25-04-2024", 1);


    }



    public void insertProvider(String provider_name, String provider_address, String products) {
        PreparedStatement preparedStatement = null;
        String sqlInsertProviderTable = "INSERT INTO provider " +
                "VALUES (sq_provider.NEXTVAL, ?, ?, ?)";
        try {
            preparedStatement = connection.getConnection().prepareStatement(sqlInsertProviderTable);
            preparedStatement.setString(1, provider_name);
            preparedStatement.setString(2, provider_address);
            preparedStatement.setString(3, products);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1031 || e.getErrorCode() == 1045) {
                showAlert("Недостаточно прав для выполнения операции. Обратитесь к администратору.");
            } else {
                showAlert( "Произошла ошибка при выполнении операции: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    public void insertRequest(int providerId) {
        PreparedStatement preparedStatement = null;
        String sqlInsertRequestTable = "INSERT INTO request(request_id, provider_id) VALUES (sq_request.NEXTVAL, ?)";
        try {
            preparedStatement = connection.getConnection().prepareStatement(sqlInsertRequestTable);
            preparedStatement.setInt(1, providerId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1031 || e.getErrorCode() == 1045) {
                showAlert("Недостаточно прав для выполнения операции. Обратитесь к администратору.");
            } else {
                showAlert("Произошла ошибка при выполнении операции: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }




    public void insertMedicamentType(String typeName) {
        PreparedStatement preparedStatement = null;
        String sqlInsertMedicamentType = "INSERT INTO medicament_type(type_name) " +
                "VALUES (?)";
        try {
            preparedStatement = connection.getConnection().prepareStatement(sqlInsertMedicamentType);
            preparedStatement.setString(1, typeName);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1031 || e.getErrorCode() == 1045) {
                showAlert("Недостаточно прав для выполнения операции. Обратитесь к администратору.");
            } else {
                showAlert("Произошла ошибка при выполнении операции: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    public void insertPatient(String name, String surname, String birthDate, String phoneNumber, String address, String regDate) {
        PreparedStatement preparedStatement = null;
        String sqlInsertPatient = "INSERT INTO patient " +
                "(patient_id, patient_firstname, patient_surname, patient_birthdate, patient_phone_number, patient_address, registration_date) " +
                "VALUES (sq_patient.NEXTVAL, ?, ?, to_date(?, 'DD-MM-YYYY'), ?, ?, to_date(?, 'DD-MM-YYYY'))";
        try {
            preparedStatement = connection.getConnection().prepareStatement(sqlInsertPatient);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, surname);
            preparedStatement.setString(3, birthDate);
            preparedStatement.setString(4, phoneNumber);
            preparedStatement.setString(5, address);
            preparedStatement.setString(6, regDate);
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            if (throwables.getErrorCode() == 1031 || throwables.getErrorCode() == 1045) {
                showAlert("Недостаточно прав для выполнения операции. Обратитесь к администратору.");
            } else {
                showAlert("Произошла ошибка при выполнении операции: " + throwables.getMessage());
            }
            throwables.printStackTrace();
        }
    }


    public void insertDoctor(String name, String surname) {
        PreparedStatement preparedStatement = null;
        String sqlInsertDoctor = "INSERT INTO doctor (doctor_id, doctor_firstname, doctor_surname) VALUES (sq_doctor.NEXTVAL, ?, ?)";
        try {
            preparedStatement = connection.getConnection().prepareStatement(sqlInsertDoctor);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, surname);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1031 || e.getErrorCode() == 1045) {
                showAlert("Недостаточно прав для выполнения операции. Обратитесь к администратору.");
            } else {
                showAlert("Произошла ошибка при выполнении операции: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }


    public void insertPrescription(int count, String usage, String diagnosis, int doctorId, int patientId, int medId) {
        PreparedStatement preparedStatement = null;
        try {
            String sqlInsertPrescription = "INSERT INTO prescription " +
                    "(prescription_id, count, DIRECTION_FOR_USE, diagnosis, doctor_id, patient_id, med_id) " +
                    "VALUES (sq_prescription.NEXTVAL, ?, ?, ?, ?, ?, ?)";

            preparedStatement = connection.getConnection().prepareStatement(sqlInsertPrescription);
            preparedStatement.setInt(1, count);
            preparedStatement.setString(2, usage);
            preparedStatement.setString(3, diagnosis);
            preparedStatement.setInt(4, doctorId);
            preparedStatement.setInt(5, patientId);
            preparedStatement.setInt(6, medId);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1031 || e.getErrorCode() == 1045) {
                showAlert("Недостаточно прав для выполнения операции. Обратитесь к администратору.");
            } else {
                showAlert("Произошла ошибка при выполнении операции: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    public void insertMedicament(String title, String usage, int prodTime, int critical_rate, int typeId) {
        PreparedStatement preparedStatement = null;

        String sqlInsertMedicament = "INSERT INTO medicament " +
                "(medicament_id, title, usage, volume, critical_rate, type_id) " +
                "VALUES (sq_medicament.NEXTVAL, ?, ?, ?, ?, ?)";
        try {
            preparedStatement = connection.getConnection().prepareStatement(sqlInsertMedicament);
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, usage);
            preparedStatement.setInt(3, prodTime);
            preparedStatement.setInt(4, critical_rate);
            preparedStatement.setInt(5, typeId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1031 || e.getErrorCode() == 1045) {
                showAlert("Недостаточно прав для выполнения операции. Обратитесь к администратору.");
            } else {
                showAlert("Произошла ошибка при выполнении операции: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }


    public void insertStorage(int medicamentId, String shelfLife, int actualBalance, double price) {
        PreparedStatement preparedStatement = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date parsedShelfLife = new Date(sdf.parse(shelfLife).getTime());

            String sqlInsertStorage = "INSERT INTO storage " +
                    "(storage_id, medicament_id, shelf_life, actual_balance, price) " +
                    "VALUES (sq_storage.NEXTVAL, ?, ?, ?, ?)";

            preparedStatement = connection.getConnection().prepareStatement(sqlInsertStorage);
            preparedStatement.setInt(1, medicamentId);
            preparedStatement.setDate(2, parsedShelfLife);
            preparedStatement.setInt(3, actualBalance);
            preparedStatement.setDouble(4, price);

            preparedStatement.executeUpdate();
        } catch (SQLException  e) {
            if (e.getErrorCode() == 1031 || e.getErrorCode() == 1045) {
                showAlert("Недостаточно прав для выполнения операции. Обратитесь к администратору.");
            } else {
                showAlert("Произошла ошибка при выполнении операции: " + e.getMessage());
            }
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    public void insertRequestStructure(int medicamentId, int requestId, int amount) {
        PreparedStatement preparedStatement = null;
        String sqlInsertRequestStructure = "INSERT INTO request_structure " +
                "VALUES (sq_request_structure.NEXTVAL, " + medicamentId + ", " + requestId + ", " + amount + ")";
        try {
            preparedStatement = connection.getConnection().prepareStatement(sqlInsertRequestStructure);
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            System.out.println("can't insert into request_structure table");
            throwables.printStackTrace();
        }
    }

    public void insertPreparationTechnology(int medicamentId, String method) {

        PreparedStatement preparedStatement = null;
        String sqlInsertPreparationTechnology = "INSERT INTO preparation_technology " +
                "VALUES (sq_preparation_technology.NEXTVAL, " + medicamentId + ", '" + method + "')";
        try {
            preparedStatement = connection.getConnection().prepareStatement(sqlInsertPreparationTechnology);
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            System.out.println("can't insert into preparation_technology table");
            throwables.printStackTrace();
        }
    }


    public void insertPharmacyEmployee(String name, String surname) {
        PreparedStatement preparedStatement = null;
        try {
            String sqlInsertPharmacyEmployee = "INSERT INTO pharmacy_employee " +
                    "(PHARMACY_EMPLOYEE_ID, PHARMACY_EMPLOYEE_FIRSTNAME, PHARMACY_EMPLOYEE_SURNAME) " +
                    "VALUES (sq_pharmacy_employee.NEXTVAL, ?, ?)";

            preparedStatement = connection.getConnection().prepareStatement(sqlInsertPharmacyEmployee);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, surname);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1031 || e.getErrorCode() == 1045) {
                showAlert("Недостаточно прав для выполнения операции. Обратитесь к администратору.");
            } else {
                showAlert("Произошла ошибка при выполнении операции: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

//    public void insertOrder(int prId, String isReady, String isReceived, String startDate, int emplId) {
//        PreparedStatement preparedStatement = null;
//        startDate = "to_date('" + startDate + "', 'DD-MM-YYYY')";
//        String sqlInsertOrder = "INSERT INTO order_ " +
//                "VALUES (sq_order.NEXTVAL, " + prId + ", '" + isReady + "', '" + isReceived + "', " + startDate + ", " + emplId + ")";
//
//        try {
//            preparedStatement = connection.getConnection().prepareStatement(sqlInsertOrder);
//            preparedStatement.executeUpdate();
//        } catch (SQLException throwables) {
//            System.out.println("can't insert into order table");
//            throwables.printStackTrace();
//        }
//    }


    public void insertOrder(int prId, String isReady, String isReceived, String startDate, int emplId) {
        PreparedStatement preparedStatement = null;
        String sqlInsertOrder = "INSERT INTO order_ " +
                "VALUES (sq_order.NEXTVAL, ?, ?, ?, TO_DATE(?, 'DD-MM-YYYY'), ?)";
        try {
            preparedStatement = connection.getConnection().prepareStatement(sqlInsertOrder);
            preparedStatement.setInt(1, prId);
            preparedStatement.setString(2, isReady);
            preparedStatement.setString(3, isReceived);
            preparedStatement.setString(4, startDate);
            preparedStatement.setInt(5, emplId);

            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            if (throwables.getErrorCode() == 1031 || throwables.getErrorCode() == 1045) {
                showAlert("Недостаточно прав для выполнения операции. Обратитесь к администратору.");
            } else {
                System.out.println("can't insert into order table");
                throwables.printStackTrace();
            }
        }
    }


    public void updateProvider(int id, String provider_name, String provider_address, String products) {
        String sql = "UPDATE provider SET " +
                "provider_name = '" + provider_name + "', provider_address = '" + provider_address + "', products='" + products + "' " +
                "WHERE provider_id = "+ id;
        List<String> provider = new LinkedList<>();
        provider.add(sql);
        connection.insert(provider);
        System.out.println("UPDATE provider");
    }

    public void updateRequest(int id, int providerId) {
        String sql = "UPDATE request SET " +
                "provider_id = " + providerId +
                " WHERE request_id = "+ id;
        List<String> request = new LinkedList<>();
        request.add(sql);
        connection.insert(request);
        System.out.println("UPDATE request");
    }

    public void updateMedicamentType(int id, String typeName) {
        PreparedStatement preparedStatement = null;
        try {
            String sql = "UPDATE medicament_type SET " +
                    "type_name = ? " +
                    " WHERE medicament_type_id = ?";

            preparedStatement = connection.getConnection().prepareStatement(sql);
            preparedStatement.setString(1, typeName);
            preparedStatement.setInt(2, id);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1031 || e.getErrorCode() == 1045) {
                showAlert("Недостаточно прав для выполнения операции. Обратитесь к администратору.");
            } else {
                showAlert("Произошла ошибка при выполнении операции: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    public void updatePatient(int id, String name, String surname, String birthDate, String phoneNumber, String address, String regDate) {
        PreparedStatement preparedStatement = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date parsedBirthDate = new Date(sdf.parse(birthDate).getTime());
            Date parsedRegDate = new Date(sdf.parse(regDate).getTime());

            String sql = "UPDATE patient SET " +
                    "patient_firstname = ?, patient_surname = ?, patient_birthdate = ?, patient_phone_number = ?, patient_address = ?, registration_date = ? " +
                    "WHERE patient_id = ?";

            preparedStatement = connection.getConnection().prepareStatement(sql);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, surname);
            preparedStatement.setDate(3, parsedBirthDate);
            preparedStatement.setString(4, phoneNumber);
            preparedStatement.setString(5, address);
            preparedStatement.setDate(6, parsedRegDate);
            preparedStatement.setInt(7, id);

            preparedStatement.executeUpdate();
            System.out.println("UPDATE patient");
        } catch (SQLException | ParseException e) {
            if (e instanceof SQLException) {
                SQLException sqlException = (SQLException) e;
                if (sqlException.getErrorCode() == 1031 || sqlException.getErrorCode() == 1045) {
                    showAlert("Недостаточно прав для выполнения операции. Обратитесь к администратору.");
                } else {
                    showAlert("Произошла ошибка при выполнении операции: " + sqlException.getMessage());
                }
                sqlException.printStackTrace();
            } else if (e instanceof ParseException) {
                showAlert("Неправильный формат даты: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void updateDoctor(int id, String name, String surname) {
        PreparedStatement preparedStatement = null;
        try {
            String sql = "UPDATE doctor SET " +
                    "doctor_firstname = ?, doctor_surname = ? " +
                    "WHERE doctor_id = ?";

            preparedStatement = connection.getConnection().prepareStatement(sql);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, surname);
            preparedStatement.setInt(3, id);

            preparedStatement.executeUpdate();
            System.out.println("UPDATE doctor");
        } catch (SQLException e) {
            if (e.getErrorCode() == 1031 || e.getErrorCode() == 1045) {
                showAlert("Недостаточно прав для выполнения операции. Обратитесь к администратору.");
            } else {
                showAlert("Произошла ошибка при выполнении операции: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    public void updateOrder(int id, int prId, String isReady, String isReceived, String startDate, int emplID) {
        PreparedStatement preparedStatement = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date parsedStartDate = new Date(sdf.parse(startDate).getTime());

            String sql = "UPDATE order_ SET " +
                    "PRESCRIPTION_ID = ?, is_ready = ?, is_received = ?, start_date = ?, pharmacy_employee_id = ? " +
                    "WHERE order_id = ?";

            preparedStatement = connection.getConnection().prepareStatement(sql);
            preparedStatement.setInt(1, prId);
            preparedStatement.setString(2, isReady);
            preparedStatement.setString(3, isReceived);
            preparedStatement.setDate(4, parsedStartDate);
            preparedStatement.setInt(5, emplID);
            preparedStatement.setInt(6, id);

            preparedStatement.executeUpdate();
            System.out.println("UPDATE order");
        } catch (SQLException | ParseException e) {
            if (e instanceof SQLException) {
                SQLException sqlException = (SQLException) e;
                if (sqlException.getErrorCode() == 1031 || sqlException.getErrorCode() == 1045) {
                    showAlert("Недостаточно прав для выполнения операции. Обратитесь к администратору.");
                } else {
                    showAlert("Произошла ошибка при выполнении операции: " + sqlException.getMessage());
                }
                sqlException.printStackTrace();
            } else if (e instanceof ParseException) {
                showAlert("Неправильный формат даты: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void updatePrescription(int id, int count, String usage, String diagnosis, int doctorId, int patientId, int medId) {
        PreparedStatement preparedStatement = null;
        try {
            String sql = "UPDATE prescription SET " +
                    "count = ?, direction_for_use = ?, diagnosis = ?, doctor_id = ?, patient_id = ?, med_id = ? " +
                    "WHERE prescription_id = ?";

            preparedStatement = connection.getConnection().prepareStatement(sql);
            preparedStatement.setInt(1, count);
            preparedStatement.setString(2, usage);
            preparedStatement.setString(3, diagnosis);
            preparedStatement.setInt(4, doctorId);
            preparedStatement.setInt(5, patientId);
            preparedStatement.setInt(6, medId);
            preparedStatement.setInt(7, id);

            preparedStatement.executeUpdate();
            System.out.println("UPDATE prescription");
        } catch (SQLException e) {
            if (e.getErrorCode() == 1031 || e.getErrorCode() == 1045) {
                showAlert("Недостаточно прав для выполнения операции. Обратитесь к администратору.");
            } else {
                showAlert("Произошла ошибка при выполнении операции: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }


    public void updatePreparationTechnology(int id, int medId, String recipe) {
        String sql = "UPDATE preparation_technology SET " +
                "medic_id = '" + medId + "', method_of_preparation = '" + recipe + "' " +
                " WHERE PREPARATION_TECHNOLOGY_id = " + id;
        List<String> preparation_technology = new LinkedList<>();
        preparation_technology.add(sql);
        connection.insert(preparation_technology);
        System.out.println("UPDATE preparation_technology");

    }


    public void updateMedicament(int id, String title, String usage, int time, int rate, int typeId) {
        PreparedStatement preparedStatement = null;
        try {
            String sql = "UPDATE medicament SET " +
                    "title = ?, " +
                    "usage = ?, " +
                    "volume = ?, " +
                    "critical_rate = ?, " +
                    "type_id = ? " +
                    "WHERE medicament_id = ?";

            preparedStatement = connection.getConnection().prepareStatement(sql);
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, usage);
            preparedStatement.setInt(3, time);
            preparedStatement.setInt(4, rate);
            preparedStatement.setInt(5, typeId);
            preparedStatement.setInt(6, id);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1031 || e.getErrorCode() == 1045) {
                showAlert("Недостаточно прав для выполнения операции. Обратитесь к администратору.");
            } else {
                showAlert("Произошла ошибка при выполнении операции: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    public void updatePharmacyEmployee(int id, String name, String surname) {
        PreparedStatement preparedStatement = null;
        try {
            String sql = "UPDATE pharmacy_employee SET " +
                    "PHARMACY_EMPLOYEE_firstname = ?, PHARMACY_EMPLOYEE_surname = ? " +
                    "WHERE PHARMACY_EMPLOYEE_id = ?";

            preparedStatement = connection.getConnection().prepareStatement(sql);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, surname);
            preparedStatement.setInt(3, id);

            preparedStatement.executeUpdate();
            System.out.println("UPDATE employee");
        } catch (SQLException e) {
            if (e.getErrorCode() == 1031 || e.getErrorCode() == 1045) {
                showAlert("Недостаточно прав для выполнения операции. Обратитесь к администратору.");
            } else {
                showAlert("Произошла ошибка при выполнении операции: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }


    public void updateStorage(int id, int medId, String shelf_life, int balance, double price) {
        PreparedStatement preparedStatement = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date parsedShelfLife = new Date(sdf.parse(shelf_life).getTime());

            String sql = "UPDATE storage SET " +
                    "MEDICAMENT_ID = ?, ACTUAL_BALANCE = ?, " +
                    "PRICE = ?, SHELF_LIFE = ? " +
                    " WHERE STORAGE_ID = ?";

            preparedStatement = connection.getConnection().prepareStatement(sql);
            preparedStatement.setInt(1, medId);
            preparedStatement.setInt(2, balance);
            preparedStatement.setDouble(3, price);
            preparedStatement.setDate(4, parsedShelfLife);
            preparedStatement.setInt(5, id);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1031 || e.getErrorCode() == 1045) {
                showAlert("Недостаточно прав для выполнения операции. Обратитесь к администратору.");
            } else {
                showAlert("Произошла ошибка при выполнении операции: " + e.getMessage());
            }
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    public void updateRequestStructure(int id, int medId, int reqId, int balance) {


        String sql = "UPDATE REQUEST_STRUCTURE SET " +
                "MED_ID = " + medId + ", AMOUNT = " + balance + ", " +
                "REQUEST_ID = " + reqId + " " +
                " WHERE REQUEST_STRUCTURE_ID = " + id;

        List<String> storage = new LinkedList<>();
        storage.add(sql);
        connection.insert(storage);
        System.out.println("UPDATE REQUEST_STRUCTURE");

    }

    public void insertMedicamentPreparationRelationship(int tehId, int medId, int balance) {
        PreparedStatement preparedStatement = null;
        String sqlInsertRequestStructure = "INSERT INTO MEDICAMENT_PREPARATION_RELATIONSHIP " +
                "VALUES (sq_medicament_preparation_relationship.NEXTVAL, " + medId + ", " + tehId + ", " + balance + ")";
        try {
            preparedStatement = connection.getConnection().prepareStatement(sqlInsertRequestStructure);
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            System.out.println("can't insert into request_structure table");
          //  throwables.printStackTrace();
        }

    }

    public void updateMedicamentPreparationRelationship(int id, int tehId, int medId, int balance) {

        String sql = "UPDATE MEDICAMENT_PREPARATION_RELATIONSHIP SET " +
                "MEDICAMENT_ID = " + medId + ", COUNT = " + balance + ", " +
                "PREPARATION_TECHNOLOGY_ID = " + tehId + " " +
                "WHERE RELATIONSHIP_ID = " + id;

        List<String> storage = new LinkedList<>();
        storage.add(sql);
        connection.insert(storage);
        System.out.println("UPDATE MEDICAMENT_PREPARATION_RELATIONSHIP");
    }
}




