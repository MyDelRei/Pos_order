package org.example.etec_project;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class DiscountController {

    @FXML
    private Pane btnConfirm;

    @FXML
    private TextField txtDiscount;

    @FXML
    private TextField txtSubTotal;

    private double total;
    private PosController posController;

    public void settotal(double total, PosController posController) {
        this.total = total;
        this.posController = posController;
        txtSubTotal.setText(String.format("$ %.2f", total));
    }

    @FXML
    private void applyDiscount() {
        try {
            double discount = Double.parseDouble(txtDiscount.getText());
            if (discount > total) {
                txtSubTotal.setText("Discount exceeds subtotal!");
                return;
            }
            posController.applyDiscount(discount);
            closeOverlay();
        } catch (NumberFormatException e) {
            txtSubTotal.setText("Invalid number!");
        }


    }

    private void closeOverlay() {
        Stage stage = (Stage) btnConfirm.getScene().getWindow();
        stage.close();
    }


}
