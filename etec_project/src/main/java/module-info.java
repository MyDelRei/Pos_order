module org.example.etec_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;


    opens org.example.etec_project to javafx.fxml;
    exports org.example.etec_project;
}