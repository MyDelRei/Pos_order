package org.example.etec_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.etec_project.models.Refund;

import java.sql.*;

public class RefundController {

    @FXML
    private TableView<Refund> RefundTable;

    @FXML
    private TableColumn<Refund, String> Sale_id_col;

    @FXML
    private TableColumn<Refund, String> Sale_item_id_col;

    @FXML
    private TableColumn<Refund, String> Total_col;

    @FXML
    private TableColumn<Refund, String> Transaction_col;

    @FXML
    private TableColumn<Refund, String> barcode_col;

    @FXML
    private TableColumn<Refund, String> product_col;

    @FXML
    private TableColumn<Refund, Void> Return_button_col;  // Column for refund button

    @FXML
    private TextField searchTxt;  // The TextField for user input

    private Connection conn = DBConnection.instance.getConnection();

    public void initialize() {
        // Set up the table columns
        Sale_id_col.setCellValueFactory(new PropertyValueFactory<>("saleId"));
        Sale_item_id_col.setCellValueFactory(new PropertyValueFactory<>("saleItemId"));
        Total_col.setCellValueFactory(new PropertyValueFactory<>("total"));
        Transaction_col.setCellValueFactory(new PropertyValueFactory<>("transactionNumber"));
        barcode_col.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        product_col.setCellValueFactory(new PropertyValueFactory<>("productName"));

        // Set up the Return button column with HBox for styling
        Return_button_col.setCellFactory(param -> {
            TableCell<Refund, Void> cell = new TableCell<>() {
                private final HBox pane = new HBox(10);
                private final Button btnRefund = new Button("Refund");

                {
                    btnRefund.setOnAction((ActionEvent event) -> {
                        Refund refund = getTableView().getItems().get(getIndex());
                        handleRefund(refund);  // Trigger refund logic for the clicked row
                    });
                    btnRefund.setStyle("-fx-background-color: #FF6347; -fx-text-fill: white;");  // Optional styling
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        pane.getChildren().setAll(btnRefund);
                        setGraphic(pane);
                    }
                }
            };
            return cell;
        });

        // Fetch data when the controller is initialized
        fetchRefundData("");
    }

    // Method to search and fetch data based on the search input
    @FXML
    private void handleSearch(ActionEvent event) {
        String searchTerm = searchTxt.getText().trim(); // Get the text from the search field
        fetchRefundData(searchTerm);  // Pass the search term to the data fetching method
    }

    // Method to fetch refund data with optional search criteria
    private void fetchRefundData(String searchTerm) {
        String query = "SELECT si.sale_id, si.sale_item_id, si.total, s.transaction_number, pd.barcode, p.name AS product_name " +
                "FROM sale_item si " +
                "JOIN sale s ON si.sale_id = s.sale_id " +
                "JOIN product_detail pd ON si.product_id = pd.product_id " +
                "JOIN product p ON pd.product_id = p.product_id " +
                "WHERE si.total > 0";

        if (!searchTerm.isEmpty()) {
            query += " AND (si.sale_id LIKE ? OR s.transaction_number LIKE ?)";
        }

        ObservableList<Refund> refundList = FXCollections.observableArrayList();

        try {
            PreparedStatement pst = conn.prepareStatement(query);
            if (!searchTerm.isEmpty()) {
                pst.setString(1, "%" + searchTerm + "%");  // Search by sale_id
                pst.setString(2, "%" + searchTerm + "%");  // Search by transaction_number
            }

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String saleId = rs.getString("sale_id");
                String saleItemId = rs.getString("sale_item_id");
                double total = rs.getDouble("total");
                String transactionNumber = rs.getString("transaction_number");
                String barcode = rs.getString("barcode");
                String productName = rs.getString("product_name");

                Refund refund = new Refund(saleId, saleItemId, barcode, productName, total, transactionNumber);
                refundList.add(refund);
            }

            RefundTable.setItems(refundList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleRefund(Refund refund) {
        // Add the refund logic:
        // - Insert a refund record
        // - Update the sale item's status to 'refunded'
        // - Add refund payment details
        try {
            // Start by getting the necessary details about the sale item and product
            String getSaleItemDetailsQuery = "SELECT si.product_id, si.quantity, si.total, si.sale_id " +
                    "FROM sale_item si WHERE si.sale_item_id = ?";
            PreparedStatement pstDetails = conn.prepareStatement(getSaleItemDetailsQuery);
            pstDetails.setString(1, refund.getSaleItemId());
            ResultSet rs = pstDetails.executeQuery();

            if (rs.next()) {
                String productId = rs.getString("product_id");
                int quantity = rs.getInt("quantity");
                double totalRefundAmount = rs.getDouble("total");
                String saleId = rs.getString("sale_id");

                // Step 1: Insert the refund into the return_refund table
                String insertRefundQuery = "INSERT INTO return_refund (product_id, quantity, reason, status) VALUES (?, ?, ?, ?)";
                PreparedStatement pstRefund = conn.prepareStatement(insertRefundQuery);
                pstRefund.setString(1, productId);
                pstRefund.setInt(2, quantity); // Refund the same quantity
                pstRefund.setString(3, "Customer Request");
                pstRefund.setString(4, "pending"); // Refund is pending initially
                pstRefund.executeUpdate();

                // Step 2: Get the last inserted return_id
                String getLastInsertedReturnIdQuery = "SELECT LAST_INSERT_ID()";
                PreparedStatement pstGetLastId = conn.prepareStatement(getLastInsertedReturnIdQuery);
                ResultSet rsLastId = pstGetLastId.executeQuery();
                int lastInsertedReturnId = -1;
                if (rsLastId.next()) {
                    lastInsertedReturnId = rsLastId.getInt(1);
                }

                // Step 3: Remove the sale item (sale_item table)
                String deleteSaleItemQuery = "DELETE FROM sale_item WHERE sale_item_id = ?";
                PreparedStatement pstDeleteSaleItem = conn.prepareStatement(deleteSaleItemQuery);
                pstDeleteSaleItem.setString(1, refund.getSaleItemId());
                pstDeleteSaleItem.executeUpdate();

                // Step 4: Refund quantity back to the product_detail
                String updateProductDetailQuery = "UPDATE product_detail SET quantity = quantity + ? WHERE product_id = ?";
                PreparedStatement pstUpdateProductDetail = conn.prepareStatement(updateProductDetailQuery);
                pstUpdateProductDetail.setInt(1, quantity); // Refund the same quantity
                pstUpdateProductDetail.setString(2, productId);
                pstUpdateProductDetail.executeUpdate();

                // Step 5: Get the payment type (from the payment table using sale_id)


                // Step 6: Update the payment amount to deduct the refunded amount
                String updatePaymentQuery = "UPDATE payment SET amount = amount - ? WHERE sale_id = ?";
                PreparedStatement pstUpdatePayment = conn.prepareStatement(updatePaymentQuery);
                pstUpdatePayment.setDouble(1, totalRefundAmount); // Deduct the refund amount
                pstUpdatePayment.setString(2, saleId);
                pstUpdatePayment.executeUpdate();



                // Step 7: Proceed with the refund payment and insert refund payment details
                String insertReturnPaymentQuery = "INSERT INTO return_payment (return_id, payment_type, amount) VALUES (?, ?, ?)";
                PreparedStatement pstInsertReturnPayment = conn.prepareStatement(insertReturnPaymentQuery);
                pstInsertReturnPayment.setInt(1, lastInsertedReturnId); // Use the last inserted return ID
                pstInsertReturnPayment.setString(2, "cash"); // Payment type (cash/qr)
                pstInsertReturnPayment.setDouble(3, totalRefundAmount); // Refund amount
                pstInsertReturnPayment.executeUpdate();

                // Step 8: Update the refund status to 'done' (processed)
                String updateRefundStatusQuery = "UPDATE return_refund SET status = 'approved' WHERE return_id = ?";
                PreparedStatement pstUpdateRefundStatus = conn.prepareStatement(updateRefundStatusQuery);
                pstUpdateRefundStatus.setInt(1, lastInsertedReturnId); // Use the last inserted return ID
                pstUpdateRefundStatus.executeUpdate();

                // Log the successful refund process
                System.out.println("Refund processed for Sale Item ID: " + refund.getSaleItemId());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Log any errors for debugging
            System.out.println("Error processing refund: " + e.getMessage());
        }
    }








}
