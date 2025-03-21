package org.example.etec_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.print.PrinterJob;
import java.math.BigDecimal;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.etec_project.models.Refund;
import org.example.etec_project.models.Report;
import org.w3c.dom.Text;

public class ReportController {

    // Using DBConnection instance to get the connection
    private final Connection conn = DBConnection.instance.getConnection();
    private PreparedStatement pst = null;
    private ResultSet rs = null;



    @FXML
    private PieChart refund_product_chart;

    @FXML
    private BarChart<String, Integer> Report_saledayByHour_chart;

    @FXML
    private TableColumn<Report, Void> Print_col;

    @FXML
    private TableColumn<Report, ?> Sale_item_id_col;

    @FXML
    private TableColumn<Report, ?> Transaction_col;

    @FXML
    private TableColumn<Report, ?> product_col;




    @FXML
    private TableView<Report> reportTbl;

    @FXML
    private TableColumn<Report, ?> saleid_col;

    @FXML
    private TableColumn<Report, ?> total_col;

    @FXML
    private HBox refund_product_item;


    @FXML
    public void initialize() {
        Sale_item_id_col.setCellValueFactory(new PropertyValueFactory<>("sale_item_id"));
        saleid_col.setCellValueFactory(new PropertyValueFactory<>("sale_id"));
        Transaction_col.setCellValueFactory(new PropertyValueFactory<>("transaction_number"));
        product_col.setCellValueFactory(new PropertyValueFactory<>("product_name"));
        total_col.setCellValueFactory(new PropertyValueFactory<>("total"));

        // Fetch the data for the top 10 sold items this week and update the chart
        ObservableList<XYChart.Data<String, Integer>> data = getTopSoldItemsThisWeek();

        // Set the data into the chart
        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        series.setName("Top Selling Products This Week");
        series.getData().addAll(data);

        Report_saledayByHour_chart.getData().add(series);

        updateRefundProductChart();
        updateSalesReport();
    }

    public ObservableList<XYChart.Data<String, Integer>> getTopSoldItemsThisWeek() {
        ObservableList<XYChart.Data<String, Integer>> data = FXCollections.observableArrayList();

        String query = "SELECT si.product_id, p.name, SUM(si.quantity) AS total_sold " +
                "FROM sale_item si " +
                "JOIN product p ON si.product_id = p.product_id " +
                "JOIN sale s ON si.sale_id = s.sale_id " +
                "WHERE s.sale_date >= CURDATE() - INTERVAL (DAYOFWEEK(CURDATE()) - 1) DAY " +
                "AND s.sale_date < CURDATE() - INTERVAL (DAYOFWEEK(CURDATE()) - 7) DAY " +
                "GROUP BY si.product_id " +
                "ORDER BY total_sold DESC " +
                "LIMIT 10";

        try {
            // Prepare the query
            pst = conn.prepareStatement(query);

            // Execute the query and get the result set
            rs = pst.executeQuery();

            // Process the result set
            while (rs.next()) {
                String productName = rs.getString("name");
                int totalSold = rs.getInt("total_sold");

                // Add data to the chart
                data.add(new XYChart.Data<>(productName, totalSold));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources in the finally block to ensure they are closed even if an exception occurs
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return data;
    }

    public void updateRefundProductChart() {
        // SQL Query to get the refunded products and their quantities
        String query = "SELECT p.name, SUM(rr.quantity) AS total_refunded " +
                "FROM return_refund rr " +
                "JOIN product p ON rr.product_id = p.product_id " +
                "WHERE rr.status = 'approved' " +
                "GROUP BY p.name " +
                "ORDER BY total_refunded DESC";

        try {
            // Prepare the query
            pst = conn.prepareStatement(query);

            // Execute the query and get the result set
            rs = pst.executeQuery();

            // Clear the existing data in the pie chart
            refund_product_chart.getData().clear();

            // Process the result set and add data to the PieChart
            while (rs.next()) {
                String productName = rs.getString("name");
                int totalRefunded = rs.getInt("total_refunded");

                // Create a PieChart.Data object for each refunded product
                PieChart.Data data = new PieChart.Data(productName, totalRefunded);

                // Add the data to the PieChart
                refund_product_chart.getData().add(data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources in the finally block to ensure they are closed even if an exception occurs
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public void updateSalesReport() {
        // SQL Query to fetch the sales data
        String query = "SELECT si.sale_item_id, si.sale_id, s.transaction_number, p.name AS product_name, si.total " +
                "FROM sale_item si " +
                "JOIN product p ON si.product_id = p.product_id " +
                "JOIN sale s ON si.sale_id = s.sale_id " +
                "ORDER BY si.sale_item_id";

        try {
            pst = conn.prepareStatement(query);
            rs = pst.executeQuery();

            // Clear existing data from the table
            reportTbl.getItems().clear();

            // Process the result set and add data to the table
            while (rs.next()) {
                // Create an object representing each row (SaleItemReport object)
                Report report = new Report(
                        rs.getString("sale_item_id"),
                        rs.getString("sale_id"),
                        rs.getString("transaction_number"),
                        rs.getString("product_name"),
                        rs.getBigDecimal("total")
                );

                // Add the report object to the table
                reportTbl.getItems().add(report);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


}
