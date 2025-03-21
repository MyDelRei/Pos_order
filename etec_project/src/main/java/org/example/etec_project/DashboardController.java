package org.example.etec_project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DashboardController {
    @FXML
    private Label Sale_lbl;

    @FXML
    private Label inStock_lbl;

    @FXML
    private Label monthlySale_lbl;

    @FXML
    private Label product_lbl;

    @FXML
    private PieChart Product_sale_pie_chart;


    @FXML
    private AreaChart<String, Number> Monthly_Sale_report;


    private final Connection conn = DBConnection.instance.getConnection();
    private PreparedStatement pst = null;
    private ResultSet rs = null;

    @FXML
    public void initialize() {
        updateDashboard();
        loadProductSalesChart();
        loadMonthlySalesReport();
    }

    private void updateDashboard() {
        Sale_lbl.setText(getTotalSales() + " Sales");
        inStock_lbl.setText(getTotalStock() + " ");
        monthlySale_lbl.setText("$" + getMonthlySales());
        product_lbl.setText(getTotalProducts() + " ");
    }

    private int getTotalSales() {
        String query = "SELECT COUNT(sale_id) AS total_sales FROM sale";
        try {
            pst = conn.prepareStatement(query);
            rs = pst.executeQuery();
            return rs.next() ? rs.getInt("total_sales") : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            closeResources();
        }
    }

    private int getTotalStock() {
        String query = "SELECT SUM(quantity) AS total_stock FROM product_detail";
        try {
            pst = conn.prepareStatement(query);
            rs = pst.executeQuery();
            return rs.next() ? rs.getInt("total_stock") : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            closeResources();
        }
    }

    private double getMonthlySales() {
        String query = """
            SELECT SUM(si.total) AS total_sales_usd 
            FROM sale s
            JOIN sale_item si ON s.sale_id = si.sale_id
            WHERE MONTH(s.sale_date) = MONTH(CURRENT_DATE) 
            AND YEAR(s.sale_date) = YEAR(CURRENT_DATE)
        """;
        try {
            pst = conn.prepareStatement(query);
            rs = pst.executeQuery();
            return rs.next() ? rs.getDouble("total_sales_usd") : 0.0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        } finally {
            closeResources();
        }
    }

    private int getTotalProducts() {
        String query = "SELECT COUNT(DISTINCT product_id) AS total_products FROM product_detail";
        try {
            pst = conn.prepareStatement(query);
            rs = pst.executeQuery();
            return rs.next() ? rs.getInt("total_products") : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            closeResources();
        }
    }
    private void loadProductSalesChart() {
        String query = """
            SELECT p.name, SUM(si.total) AS total_sales
            FROM sale_item si
            JOIN product p ON si.product_id = p.product_id
            GROUP BY p.name
            ORDER BY total_sales DESC
        """;

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        try {
            pst = conn.prepareStatement(query);
            rs = pst.executeQuery();

            while (rs.next()) {
                String productName = rs.getString("name");
                double totalSales = rs.getDouble("total_sales");

                // Add data to the PieChart
                pieChartData.add(new PieChart.Data(productName, totalSales));
            }

            // Set the data to the PieChart
            Product_sale_pie_chart.setData(pieChartData);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }
    }

    private void loadMonthlySalesReport() {
        // Updated query to get sales for each month of the current year (January to December)
        String query = """
            SELECT 
                MONTH(sale_date) AS sale_month,
                SUM(si.total) AS total_sales
            FROM sale_item si
            JOIN sale s ON si.sale_id = s.sale_id
            WHERE YEAR(sale_date) = YEAR(CURDATE())
            GROUP BY MONTH(sale_date)
            ORDER BY sale_month;
        """;

        XYChart.Series<String, Number> salesSeries = new XYChart.Series<>();
        salesSeries.setName("Monthly Sales");

        try {
            pst = conn.prepareStatement(query);
            rs = pst.executeQuery();

            // Initialize total sales for each month to zero (for all 12 months)
            double[] monthlySales = new double[12];
            while (rs.next()) {
                int month = rs.getInt("sale_month") - 1; // Months are 1-12, but the array index is 0-11
                double totalSales = rs.getDouble("total_sales");
                monthlySales[month] = totalSales; // Store the total sales for the respective month
            }

            // Add data to the AreaChart Series for each month (1 to 12)
            for (int i = 0; i < 12; i++) {
                String monthName = getMonthName(i); // Get the name of the month (e.g., "January")
                salesSeries.getData().add(new XYChart.Data<>(monthName, monthlySales[i]));
            }

            // Add series to the AreaChart
            Monthly_Sale_report.getData().add(salesSeries);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }
    }

    private String getMonthName(int monthIndex) {
        String[] months = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        return months[monthIndex];
    }






    private void closeResources() {
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
