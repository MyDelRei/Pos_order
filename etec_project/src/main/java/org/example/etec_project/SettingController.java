package org.example.etec_project;


import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingController {
    @FXML
    private ComboBox<String> cmbRole;

    @FXML
    private TextField txtpassword;

    @FXML
    private TextField txtusername;

    private Connection conn = DBConnection.instance.getConnection();
    private PreparedStatement pst = null;
    private ResultSet rs = null;

    @FXML
    public void initializeComboBox() {
        cmbRole.getItems().addAll("Cashier", "Admin");
    }

    @FXML
    public void initialize() {
        initializeComboBox();
    }

    public void insertUser() {
        String username = txtusername.getText().trim();
        String password = txtpassword.getText().trim();
        String role = cmbRole.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        try {
            // Check if the username already exists
            String checkQuery = "SELECT COUNT(*) FROM User WHERE username = ?";
            pst = conn.prepareStatement(checkQuery);
            pst.setString(1, username);
            rs = pst.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                showAlert("Error", "Username already exists. Please choose a different one.");
                return;
            }

            // Insert the new user
            String query = "INSERT INTO User(username, password, role) VALUES (?, ?, ?)";
            pst = conn.prepareStatement(query);
            pst.setString(1, username);
            pst.setString(2, password);
            pst.setString(3, role);

            int result = pst.executeUpdate();
            if (result > 0) {
                showAlert("Success", "User added successfully.");
                clearFields();
            } else {
                showAlert("Error", "Failed to add user.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database error: " + e.getMessage());
        } finally {
            closeResources();
        }
    }

    private void showAlert(String title, String message) {
        Stage alertStage = new Stage();
        alertStage.initModality(Modality.APPLICATION_MODAL);
        alertStage.initOwner(txtusername.getScene().getWindow());

        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(20));

        Label label = new Label(message);
        label.setStyle("-fx-font-size: 14px;");

        Button okButton = new Button("OK");
        okButton.setOnAction(e -> alertStage.close());

        vbox.getChildren().addAll(label, okButton);

        Scene scene = new Scene(vbox);
        alertStage.setScene(scene);
        alertStage.setTitle(title);
        alertStage.setResizable(false);
        alertStage.showAndWait();
    }


    private void clearFields() {
        txtusername.clear();
        txtpassword.clear();
        cmbRole.getSelectionModel().clearSelection();
    }

    private void closeResources() {
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
