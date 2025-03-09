module org.example.etec_project {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.etec_project to javafx.fxml;
    exports org.example.etec_project;
}