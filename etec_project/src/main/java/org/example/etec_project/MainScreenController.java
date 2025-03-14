package org.example.etec_project;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class MainScreenController {

    @FXML
    private AnchorPane Content_pane;

    @FXML
    private HBox PosBtn;

    @FXML
    private void PosBtnClick() {
        loadForm("Pos.fxml");
    }

    @FXML
    private void InventoryBtnClick(){
        loadForm("inventory.fxml");
    }



    private void loadForm(String formPath) {
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

}