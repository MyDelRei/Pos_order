module org.example.etec_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires net.sf.jasperreports.core;


    opens org.example.etec_project to javafx.fxml;
    opens org.example.etec_project.models to javafx.base;
    exports org.example.etec_project;
}