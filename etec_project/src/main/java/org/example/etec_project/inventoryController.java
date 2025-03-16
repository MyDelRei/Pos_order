package org.example.etec_project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.example.etec_project.models.product;

import java.sql.*;

public class inventoryController {
    @FXML
    private TableView<product> InventoryTable;

    @FXML
    private TableColumn<product, String> Location_col;

    @FXML
    private TableColumn<product, String> barcode_col;

    @FXML
    private TableColumn<product, String> expire_date_col;

    @FXML
    private TableColumn<product, Double> price_col;

    @FXML
    private TableColumn<product, String> product_name_col;

    @FXML
    private TableColumn<product, Integer> qty_col;

    @FXML
    private HBox btnAll;

    @FXML
    private HBox btnDairyProduct;

    @FXML
    private HBox btnDrink;

    @FXML
    private HBox btnFood;

    private ObservableList<product> ProductList = FXCollections.observableArrayList();
    private Connection conn = DBConnection.instance.getConnection();
    private PreparedStatement pst = null;
    private ResultSet rs = null;

    @FXML
    public void initialize() {
        loadInventoryData(); // Load default data on initialization

        btnDrink.setOnMouseClicked(event -> loadInventoryData("inventory_drink"));

        // Event handler for the Food button
        btnFood.setOnMouseClicked(event -> loadInventoryData("inventory_food"));

        // Event handler for the Dairy Product button
        btnDairyProduct.setOnMouseClicked(event -> loadInventoryData("inventory_dairy_product"));

        // Event handler for the "All" button (to load all categories)
        btnAll.setOnMouseClicked(event -> loadInventoryData());

    }

    private void loadInventoryData() {
        String query = "SELECT * FROM inventory_display";  // Fetching data from view



        try {
            pst = conn.prepareStatement(query);
            rs = pst.executeQuery();

            while (rs.next()) {
                ProductList.add(new product(
                        rs.getString("product_name"),
                        rs.getString("location"),
                        rs.getInt("quantity"),
                        rs.getString("expire_date"),
                        rs.getString("barcode"),
                        rs.getDouble("price")
                ));
            }

            InventoryTable.setItems(ProductList);

            // Mapping columns to model properties
            product_name_col.setCellValueFactory(new PropertyValueFactory<>("productName"));
            qty_col.setCellValueFactory(new PropertyValueFactory<>("location"));
            price_col.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            barcode_col.setCellValueFactory(new PropertyValueFactory<>("expireDate"));
            Location_col.setCellValueFactory(new PropertyValueFactory<>("barcode"));
            expire_date_col.setCellValueFactory(new PropertyValueFactory<>("price"));


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
        ProductList.clear();
        String query = "SELECT * FROM " + categoryView;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                ProductList.add(new product(
                        rs.getString("name"),
                        rs.getString("location"),
                        rs.getInt("quantity"),
                        rs.getString("expire_date"),
                        rs.getString("barcode"),
                        rs.getDouble("price")
                ));
            }

            InventoryTable.setItems(ProductList);

            product_name_col.setCellValueFactory(new PropertyValueFactory<>("productName"));
            qty_col.setCellValueFactory(new PropertyValueFactory<>("location"));
            price_col.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            barcode_col.setCellValueFactory(new PropertyValueFactory<>("expireDate"));
            Location_col.setCellValueFactory(new PropertyValueFactory<>("barcode"));
            expire_date_col.setCellValueFactory(new PropertyValueFactory<>("price"));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
