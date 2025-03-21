package org.example.etec_project;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Dialog;
import javafx.geometry.Pos;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private PasswordField txtPassword;

    @FXML
    public TextField txtUsername;

    private Connection conn = DBConnection.instance.getConnection();
    private PreparedStatement pst = null;
    private ResultSet rs = null;

    public void loginUser() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both username and password.");
            return;
        }

        String query = "SELECT * FROM User WHERE username = ? AND password = ?";

        try {
            pst = conn.prepareStatement(query);
            pst.setString(1, username);
            pst.setString(2, password);
            rs = pst.executeQuery();

            if (rs.next()) {
                // Get the role of the user from the result set
                String role = rs.getString("role");

                // Show success alert and pass role to next screen
                showAlert("Success", "Login successful!");
                loadDashboard(role); // Pass role to the next screen
            } else {
                showAlert("Error", "Invalid username or password.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database error: " + e.getMessage());
        } finally {
            closeResources();
        }
    }

    private void showAlert(String title, String message) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.initOwner(txtUsername.getScene().getWindow());

        Label label = new Label(message);
        label.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");

        VBox vbox = new VBox(label);
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);

        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        dialog.showAndWait();
    }

    private void loadDashboard(String role) {
        try {
            // Ensure the path to MainScreen.fxml is correct
            String fxmlPath = "MainScreen.fxml"; // Adjust if necessary
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

            // Check if FXMLLoader was successful in loading the FXML file
            if (loader.getLocation() == null) {
                throw new IOException("FXML file not found: " + fxmlPath);
            }

            Parent root = loader.load();

            // Get the controller of the MainScreen and set the role


            // Set up the stage and scene
            Stage stage = (Stage) txtUsername.getScene().getWindow(); // Get current window (login window)
            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard");

            // Maximize the window (dock/taskbar remains visible)
            stage.setMaximized(true); // This ensures the window fills the screen but respects OS UI (dock/taskbar)

            // Optional: Set minimum size to prevent resizing too small
            stage.setMinWidth(800);  // Adjust as needed
            stage.setMinHeight(600); // Adjust as needed

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load dashboard: " + e.getMessage());
        }
    }

    //    private void loadDashboard(String role) {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainScreen.fxml"));
//            Parent root = loader.load();
//            MainScreenController controller = loader.getController();
//            if (controller != null) {
//                controller.setUserRole(role); // Pass the role to the MainScreenController
//            }
//            Stage stage = (Stage) txtUsername.getScene().getWindow(); // Get current window
//            stage.setScene(new Scene(root));
//            stage.setTitle("Dashboard");
//            stage.setFullScreen(true); // Enable full-screen mode
//            stage.show();
//        } catch (IOException e) {
//            e.printStackTrace();
//            showAlert("Error", "Failed to load dashboard.");
//        }
//    }

    private void closeResources() {
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
