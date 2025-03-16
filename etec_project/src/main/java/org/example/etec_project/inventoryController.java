package org.example.etec_project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.example.etec_project.models.Product;

import java.sql.*;

public class inventoryController {

    private Connection con;
    private PreparedStatement pst;
    private ResultSet rs;

    @FXML
    private TableView<Product> inventoryTable;

    @FXML
    private TableColumn<Product, String> product_name_col;

    @FXML
    private TableColumn<Product, Double> price_col;

    @FXML
    private TableColumn<Product, Integer> qty_col;

    @FXML
    private TableColumn<Product, String> barcode_col;

    @FXML
    private TableColumn<Product, String> Location_col;

    @FXML
    private HBox btnAll;

    @FXML
    private HBox btnDairy;

    @FXML
    private HBox btnDrink;

    @FXML
    private HBox btnFood;

    private ObservableList<Product> product = FXCollections.observableArrayList();

    public void initialize() {
        // Set up table columns
        product_name_col.setCellValueFactory(new PropertyValueFactory<>("productName"));
        price_col.setCellValueFactory(new PropertyValueFactory<>("price"));
        qty_col.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        barcode_col.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        Location_col.setCellValueFactory(new PropertyValueFactory<>("location"));

        fetchData();
    }

    public void fetchData() {
        product.clear();
        try {
            String sql = "SELECT * FROM inventory";

            con = DBConnection.instance.getConnection();
            pst = con.prepareStatement(sql);
            rs = pst.executeQuery();


            while (rs.next()) {
                product.add(new Product(
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
                        rs.getString("expire_date"),
                        rs.getString("barcode"),
                        rs.getString("location")
                ));
            }
            inventoryTable.setItems(product);
            inventoryTable.refresh();

        } catch (SQLException ex) {
            showErrorAlert("Data fetch failed: " + ex.getMessage());
        } finally {
            //closeResources();
        }
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Database Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeResources() {
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
            if (con != null) con.close();
        } catch (SQLException e) {
            showErrorAlert("Error closing resources: " + e.getMessage());
        }
    }
}

