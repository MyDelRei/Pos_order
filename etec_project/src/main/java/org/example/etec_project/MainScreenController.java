package org.example.etec_project;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;

public class MainScreenController {

    @FXML
    private AnchorPane Content_pane;

    @FXML
    private HBox PosBtn;

    @FXML
    private HBox ReturnBtn;

    @FXML
    private HBox InventoryBtn;
    @FXML
    private HBox ReportBtn;
    @FXML
    private HBox SettingBtn;
    @FXML
    private HBox LogoutBtn;

    @FXML
    private HBox DashBoardBtn;


    // Variable to hold the user role
    private String userRole;

    // Method to set the user role and enable/disable UI components accordingly
    public void setUserRole(String role) {
        this.userRole = role;
        disableSectionsBasedOnRole(role);
    }

    // Method to enable/disable UI elements based on user role
    private void disableSectionsBasedOnRole(String role) {
        if ("Cashier".equals(role)) {
            // Disable Inventory, Report, and Setting sections for Cashiers
            InventoryBtn.setDisable(true);
            ReportBtn.setDisable(true);
            SettingBtn.setDisable(true);
            DashBoardBtn.setDisable(true);
        } else if ("Admin".equals(role)) {
            // Enable everything for Admin
            DashBoardBtn.setDisable(false);
            InventoryBtn.setDisable(false);
            ReportBtn.setDisable(false);
            SettingBtn.setDisable(false);
        }
    }

    // Navigation Methods for buttons
    @FXML
    private void PosBtnClick() {
        loadForm("Pos.fxml");
    }

    @FXML
    private void InventoryBtnClick(){
        loadForm("inventory.fxml");
    }

    public void refundButtonClick(){
        loadForm("Refund.fxml");
    }




    @FXML
    private void settingBtnClick(){
        loadForm("Setting.fxml");
    }
    @FXML
    private void DashBoardBtnClick(){
        loadForm("Dashboard.fxml");
    };

    @FXML
    private void ReportBtnClick(){
        loadForm("Report.fxml");

    };

    // Method to load the form into the Content_pane
    public void loadForm(String formPath) {
        try {
            // Load the specified FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource(formPath));
            Parent newForm = loader.load();

            // Clear existing content and add new form
            Content_pane.getChildren().clear();
            Content_pane.getChildren().add(newForm);

            // Set anchors to fill the AnchorPane
            AnchorPane.setTopAnchor(newForm, 0.0);
            AnchorPane.setBottomAnchor(newForm, 0.0);
            AnchorPane.setLeftAnchor(newForm, 0.0);
            AnchorPane.setRightAnchor(newForm, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
            // You might want to add error handling like showing an alert
            System.out.println("Failed to load form: " + formPath);
        }
    }


    // Optional: If you want to handle the Logout functionality

    @FXML
    private void logout() { // Called when LogoutBtn is clicked (onAction="#logout")
        try {
            Stage currentStage = (Stage) LogoutBtn.getScene().getWindow();
            currentStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Parent loginRoot = loader.load();

            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(loginRoot));
            loginStage.setTitle("Login");
            loginStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
