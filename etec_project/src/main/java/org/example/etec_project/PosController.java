package org.example.etec_project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.util.JRLoader;
import org.example.etec_project.models.OrderItem;
import org.example.etec_project.models.inventoryItem;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TableView;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource; // Add this import
import net.sf.jasperreports.view.JasperViewer;

import java.sql.Connection;
import java.sql.*;
import java.util.Date;

public class PosController {

    @FXML
    private Label lblTransaction;

    @FXML
    private TableView<OrderItem> orderListTable;
    @FXML
    private TableView<inventoryItem> InventoryTable;
    @FXML
    private TableColumn<inventoryItem, Void> Add;

    @FXML
    private TableColumn<inventoryItem, String> BarcodeCol;

    @FXML
    private TableColumn<inventoryItem, String> Exp_date_col;

    @FXML
    private TableColumn<inventoryItem, String> LocationCol;

    @FXML
    private TableColumn<inventoryItem, Double> PriceCol;


    @FXML
    private TableColumn<inventoryItem, String> Pro_name_col;

    @FXML
    private TableColumn<inventoryItem, Integer> QtyCol;

    @FXML
    private TableColumn<OrderItem, String> productname_orderlist_col;

    @FXML
    private TableColumn<OrderItem, Integer> qtyorderList_col;

    @FXML
    private TableColumn<OrderItem, Double> TotalorderListCol;

    @FXML
    private TableColumn<OrderItem, Void> PlusCol;

    @FXML
    private TableColumn<OrderItem, Void> deductCol;
    @FXML
    private Label DiscountLbl;

    @FXML
    private Label SubTotalLbl;

    @FXML
    private Label TotalLbl;


    @FXML
    public HBox DrinkBtn;

    @FXML
    public HBox FoodBtn;

    @FXML
    public HBox DairyProductBtn;

    @FXML
    public HBox allBtn;

    @FXML
    private TextField searchTxt;


    @FXML
    private TextField txtCard;

    @FXML
    private TextField txtCash;

    @FXML
    private TextField txtQR;

    @FXML
    private HBox btnConfirmPayment;


    @FXML
    private HBox btnAddDiscount;


    private ObservableList<inventoryItem> inventoryList = FXCollections.observableArrayList();
    private ObservableList<OrderItem> orderList = FXCollections.observableArrayList();
    private Connection conn = DBConnection.instance.getConnection();
    private PreparedStatement pst = null;
    private ResultSet rs = null;

    // Add SaleManager here
    private SaleManager saleManager = new SaleManager();

    @FXML
    public void initialize() {
        loadInventoryData();

        DrinkBtn.setOnMouseClicked(event -> loadInventoryData("inventory_drink"));

        // Event handler for the Food button
        FoodBtn.setOnMouseClicked(event -> loadInventoryData("inventory_food"));

        // Event handler for the Dairy Product button
        DairyProductBtn.setOnMouseClicked(event -> loadInventoryData("inventory_dairy_product"));

        // Event handler for the "All" button (to load all categories)
        allBtn.setOnMouseClicked(event -> loadInventoryData());

        searchTxt.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                searchProductByBarcode(newValue); // Call search method
            } else {
                loadInventoryData();  // Reload the inventory data if search is cleared
            }
        });


        Add.setCellFactory(param -> {
            TableCell<inventoryItem, Void> cell = new TableCell<>() {
                private final HBox pane = new HBox(10);
                private final Button btnAdd = new Button("Add");


                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {

                        btnAdd.getStyleClass().addAll("btnAdd","shadow");

                        pane.getChildren().clear();
                        pane.getChildren().addAll(btnAdd);


                        setGraphic(pane);

                        btnAdd.setOnAction(e -> handleAdd(getTableRow().getItem()));

                    }
                }
            };
            return cell;
        });
        productname_orderlist_col.setCellValueFactory(new PropertyValueFactory<>("productName"));
        qtyorderList_col.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        TotalorderListCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        // Bind the observable list to the order list table
        orderListTable.setItems(orderList);

        deductCol.setCellFactory(param -> {
            TableCell<OrderItem, Void> cell = new TableCell<>() {
                private final HBox pane = new HBox();
                private final Button btndeduct = new Button("―");

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        btndeduct.setPrefHeight(33);
                        btndeduct.setPrefWidth(33);
                        btndeduct.getStyleClass().addAll("btnAdd","shadow");

                        pane.getChildren().clear();
                        pane.getChildren().addAll(btndeduct);

                        setGraphic(pane);

                        btndeduct.setOnAction(e -> editdeduct(getTableRow().getItem()));

                    }
                }
            };
            return cell;
        });

        PlusCol.setCellFactory(param -> {
            TableCell<OrderItem, Void> cell = new TableCell<>() {
                private final HBox pane = new HBox(10);
                private final Button btnplus = new Button("+");

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        btnplus.setPrefHeight(33);
                        btnplus.setPrefWidth(33);
                        btnplus.getStyleClass().addAll("btnAdd","shadow");

                        pane.getChildren().clear();
                        pane.getChildren().addAll(btnplus);

                        setGraphic(pane);

                        btnplus.setOnAction(e -> editplus(getTableRow().getItem()));

                    }
                }
            };
            return cell;
        });



    }

    private void loadInventoryData() {
        String query = "SELECT * FROM inventory_display";  // Fetching data from view



        try {
            pst = conn.prepareStatement(query);
            rs = pst.executeQuery();

            while (rs.next()) {
                inventoryList.add(new inventoryItem(
                        rs.getString("product_name"),
                        rs.getString("location"),
                        rs.getInt("quantity"),
                        rs.getString("expire_date"),
                        rs.getString("barcode"),
                        rs.getDouble("price")
                ));
            }

            InventoryTable.setItems(inventoryList);

            // Mapping columns to model properties
            Pro_name_col.setCellValueFactory(new PropertyValueFactory<>("productName"));
            LocationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
            QtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            Exp_date_col.setCellValueFactory(new PropertyValueFactory<>("expireDate"));
            BarcodeCol.setCellValueFactory(new PropertyValueFactory<>("barcode"));
            PriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));


        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    private void loadInventoryData(String categoryView) {
        inventoryList.clear();
        String query = "SELECT * FROM " + categoryView;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                inventoryList.add(new inventoryItem(
                        rs.getString("name"),
                        rs.getString("location"),
                        rs.getInt("quantity"),
                        rs.getString("expire_date"),
                        rs.getString("barcode"),
                        rs.getDouble("price")
                ));
            }

            InventoryTable.setItems(inventoryList);

            Pro_name_col.setCellValueFactory(new PropertyValueFactory<>("productName"));
            LocationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
            QtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            Exp_date_col.setCellValueFactory(new PropertyValueFactory<>("expireDate"));
            BarcodeCol.setCellValueFactory(new PropertyValueFactory<>("barcode"));
            PriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleAdd(inventoryItem item) {


        String transactionNumber = generateTransactionNumber();

        // Update the label with the transaction number
        lblTransaction.setText("#" + transactionNumber);


        // If the item is not null, add it to the order list
        if (item != null) {
            // Create a new OrderItem with the default quantity (1) and its price
            OrderItem orderItem = new OrderItem(item.getProductName(), 1, item.getPrice());

            // Add the order item to the observable list
            orderList.add(orderItem);
            updateTotals();
        }
    }

    private String generateTransactionNumber() {
        String transactionNumber = null;
        String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String query = "SELECT MAX(transaction_number) FROM sale WHERE transaction_number LIKE '" + currentDate + "%'";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                String maxTransactionNumber = rs.getString(1);

                // If there is no existing transaction for today, start from 0001
                if (maxTransactionNumber == null) {
                    transactionNumber = currentDate + "0001";
                } else {
                    // Extract the sequence number part and increment by 1
                    String sequencePart = maxTransactionNumber.substring(8);
                    int nextSequence = Integer.parseInt(sequencePart) + 1;

                    // Format the sequence with leading zeros
                    transactionNumber = currentDate + String.format("%04d", nextSequence);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactionNumber;
    }

    public void editdeduct(OrderItem item) {
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            orderListTable.refresh();
            updateTotals();
        }
        else{
            orderList.remove(item);
            orderListTable.refresh();
            updateTotals();

        }
    }

    public void editplus(OrderItem item) {
        item.setQuantity(item.getQuantity() + 1);
        orderListTable.refresh();
        updateTotals();
    }

    private void updateTotals() {
        double VAT = 0.01;
        double subTotal = orderList.stream().mapToDouble(orderItem -> orderItem.getQuantity() * orderItem.getUnitPrice()).sum(); // Sum up all order item prices
        double vatAmount = subTotal * VAT;


        double discount = 0.00;  // Replace this with your actual discount calculation logic

        // Calculate the subtotal (total + VAT - discount)
        double total = subTotal + vatAmount - discount;

        SubTotalLbl.setText(String.format("$ %.2f",subTotal));
        TotalLbl.setText(String.format("$ %.2f", total));
        DiscountLbl.setText(String.format("$ %.2f", discount));
    }
    @FXML
    private void openDiscountForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("DiscountOverlay.fxml"));
            Parent root = loader.load();

            DiscountController discountController = loader.getController();
            discountController.settotal(Double.parseDouble(TotalLbl.getText().replace("$", "").trim()), this);


            // Get the current stage (POS screen)
            Stage posStage = (Stage) TotalLbl.getScene().getWindow();

            // Create a new Stage for overlay
            Stage overlayStage = new Stage();
            overlayStage.initOwner(posStage);

            // Center the overlay relative to POS screen
            overlayStage.setX(posStage.getX() + (posStage.getWidth() - 382) / 2);
            overlayStage.setY(posStage.getY() + (posStage.getHeight() - 279) / 2);

            Scene scene = new Scene(root);
            overlayStage.setScene(scene);
            overlayStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public double getSubtotal() {
        return orderList.stream()
                .mapToDouble(orderItem -> orderItem.getQuantity() * orderItem.getUnitPrice())
                .sum();
    }

    public void applyDiscount(double discount) {
        double VAT = 0.01;
        double subtotal = getSubtotal();
        double vatAmount = subtotal * VAT;
        double Total = subtotal + vatAmount - discount;

        SubTotalLbl.setText(String.format("$ %.2f",subtotal));
        TotalLbl.setText(String.format("$ %.2f", Total));
        DiscountLbl.setText(String.format("$ %.2f", discount));
    }


    private void searchProductByBarcode(String barcode) {
        inventoryList.clear();  // Clear the existing inventory list to load new results
        String query = "SELECT * FROM inventory_display WHERE barcode LIKE ?";  // Use LIKE for partial matches

        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, "%" + barcode + "%"); // Set the barcode parameter for search
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                inventoryList.add(new inventoryItem(
                        rs.getString("product_name"),
                        rs.getString("location"),
                        rs.getInt("quantity"),
                        rs.getString("expire_date"),
                        rs.getString("barcode"),
                        rs.getDouble("price")
                ));
            }

            InventoryTable.setItems(inventoryList);  // Update the table with the search results

            // Mapping columns to model properties again
            Pro_name_col.setCellValueFactory(new PropertyValueFactory<>("productName"));
            LocationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
            QtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            Exp_date_col.setCellValueFactory(new PropertyValueFactory<>("expireDate"));
            BarcodeCol.setCellValueFactory(new PropertyValueFactory<>("barcode"));
            PriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleConfirmPayment() {
        if (orderList.isEmpty()) {
            showAlert("Error", "No items in the order list to process.");
            return;
        }

        String transactionNumber = lblTransaction.getText().replace("#", "").trim();
        if (transactionNumber.isEmpty()) {
            transactionNumber = generateTransactionNumber();
            lblTransaction.setText("#" + transactionNumber);
        }

        // Get payment amounts from text fields
        double cashAmount = parsePaymentAmount(txtCash.getText());
        double qrAmount = parsePaymentAmount(txtQR.getText());
        double cardAmount = parsePaymentAmount(txtCard.getText());
        double totalPaid = cashAmount + qrAmount + cardAmount;
        double totalOrder = Double.parseDouble(TotalLbl.getText().replace("$", "").trim());

        // Validate payment
        if (totalPaid < totalOrder) {
            showAlert("Payment Error", "Total payment (" + String.format("%.2f", totalPaid) + ") is less than order total (" + String.format("%.2f", totalOrder) + ").");
            return;
        }

        // Process each order item
        double discount = Double.parseDouble(DiscountLbl.getText().replace("$", "").trim());
        double subTotal = Double.parseDouble(SubTotalLbl.getText().replace("$", "").trim());
        boolean allSuccessful = true;

        for (OrderItem item : orderList) {
            double itemTotal = (item.getQuantity() * item.getUnitPrice() * (subTotal - discount)) / subTotal;
            boolean success = saleManager.processSaleItem(transactionNumber, item.getProductName(), item.getQuantity(), 0.0, itemTotal);
            if (!success) {
                allSuccessful = false;
                break;
            }
        }

        // Process payments once if items were successful
        if (allSuccessful) {
            allSuccessful = saleManager.processPayments(transactionNumber, cashAmount, qrAmount, cardAmount);
        }

        if (allSuccessful) {
            generateReceipt(transactionNumber, cashAmount, qrAmount, cardAmount, totalPaid);
            showAlert("Success", "Payment processed successfully for transaction #" + transactionNumber);
            orderList.clear();
            orderListTable.refresh();
            updateTotals();
            txtCash.clear();
            txtQR.clear();
            txtCard.clear();
        } else {
            showAlert("Error", "Failed to process payment. Please try again.");
        }
    }

    private double parsePaymentAmount(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



    public void generateReceipt(String transactionNumber, double cashAmount, double qrAmount, double cardAmount, double totalPaid) {
        try {
            InputStream reportInputStream = getClass().getResourceAsStream("/org/example/etec_project/report/repoting.jasper");

            if (reportInputStream == null) {
                throw new JRException("Report file not found on classpath");
            }

            // Load the JasperReport from the input stream
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportInputStream);

            // Prepare parameters for static fields
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("TransNo", transactionNumber);
            parameters.put("Discount", Double.parseDouble(DiscountLbl.getText().replace("$", "").trim()));
            parameters.put("Payment", getPaymentMethodString(cashAmount, qrAmount, cardAmount));
            parameters.put("Paid", totalPaid);
            parameters.put("change", totalPaid - Double.parseDouble(TotalLbl.getText().replace("$", "").trim()));
            parameters.put("TotalA", TotalLbl.getText().replace("$", "").trim());

            // Convert ObservableList to List for JRBeanCollectionDataSource
            ObservableList<OrderItem> orderList = FXCollections.observableArrayList(this.orderList);

            // Manually put each field in parameters for custom reports
            for (OrderItem orderItem : orderList) {
                parameters.put("Name", orderItem.getProductName());
                parameters.put("qty",orderItem.getQuantity());
                parameters.put("Total", orderItem.getTotalPrice());
                System.out.println("Added to report: " + orderItem.getProductName() + ", " + orderItem.getQuantity() + ", " + orderItem.getTotalPrice());
            }

            // Convert ObservableList to Collection of Maps with wildcard type
            Collection<Map<String, ?>> mapCollection = new ArrayList<>();
            for (OrderItem item : orderList) {
                Map<String, Object> map = new HashMap<>();
                map.put("Name", item.getProductName());
                map.put("qty", item.getQuantity());
                map.put("Total", item.getTotalPrice());
                // Add summary fields to each map
                map.put("TransNo", transactionNumber);
                map.put("Discount", Double.parseDouble(DiscountLbl.getText().replace("$", "").trim()));
                map.put("Payment", getPaymentMethodString(cashAmount, qrAmount, cardAmount));
                map.put("Paid", totalPaid);
                map.put("change", totalPaid - Double.parseDouble(TotalLbl.getText().replace("$", "").trim()));
                map.put("TotalA", TotalLbl.getText().replace("$", "").trim());
                mapCollection.add(map);
            }

            // Debug data source
            System.out.println("Order item list size: " + mapCollection.size());

            // Create the data source from the map collection
            JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(mapCollection);

            // Fill the report with parameters and the data source
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            // View the report
            JasperViewer viewer = new JasperViewer(jasperPrint, false);
            viewer.setTitle("Receipt - Transaction #" + transactionNumber);
            viewer.setVisible(true);
            viewer.setExtendedState(JasperViewer.MAXIMIZED_BOTH);

        } catch (JRException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to generate receipt: " + e.getMessage());
        }
    }


    private String getPaymentMethodString(double cashAmount, double qrAmount, double cardAmount) {
        StringBuilder payment = new StringBuilder();
        if (cashAmount > 0) payment.append("Cash: $").append(String.format("%.2f", cashAmount));
        if (qrAmount > 0) {
            if (payment.length() > 0) payment.append(", ");
            payment.append("QR: $").append(String.format("%.2f", qrAmount));
        }
        if (cardAmount > 0) {
            if (payment.length() > 0) payment.append(", ");
            payment.append("Card: $").append(String.format("%.2f", cardAmount));
        }
        return payment.toString();
    }









}
