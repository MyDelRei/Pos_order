package org.example.etec_project;

import java.sql.*;
import java.time.LocalDateTime;

public class SaleManager {
    private Connection conn;

    public SaleManager() {
        this.conn = DBConnection.instance.getConnection();
    }

    /**
     * Process sale items and update inventory.
     */
    public boolean processSaleItem(String transactionID, String productName, int qty, double discount, double total) {
        PreparedStatement saleStmt = null;
        PreparedStatement saleItemStmt = null;
        PreparedStatement updateStmt = null;

        try {
            conn.setAutoCommit(false);

            // Step 1: Generate unique sale_id and insert if it doesn’t exist
            String saleID = getOrCreateSaleID(transactionID);
            if (saleID == null) {
                saleID = generateUniqueSaleID();
                String saleQuery = "INSERT INTO sale (sale_id, transaction_number, sale_date) VALUES (?, ?, ?)";
                saleStmt = conn.prepareStatement(saleQuery);
                saleStmt.setString(1, saleID);
                saleStmt.setString(2, transactionID);
                saleStmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                saleStmt.executeUpdate();
            }

            // Step 2: Insert into sale_item table if qty > 0
            String productID = getProductID(productName);
            if (productID == null) {
                throw new SQLException("Product not found: " + productName);
            }

            if (qty > 0) {
                String saleItemID = generateUniqueSaleItemID();
                String saleItemQuery = "INSERT INTO sale_item (sale_item_id, sale_id, product_id, quantity, discount, total) VALUES (?, ?, ?, ?, ?, ?)";
                saleItemStmt = conn.prepareStatement(saleItemQuery);
                saleItemStmt.setString(1, saleItemID);
                saleItemStmt.setString(2, saleID);
                saleItemStmt.setString(3, productID);
                saleItemStmt.setInt(4, qty);
                saleItemStmt.setDouble(5, discount);
                saleItemStmt.setDouble(6, total);
                saleItemStmt.executeUpdate();

                // Step 3: Update product_detail table
                String updateQuery = "UPDATE product_detail SET quantity = quantity - ? WHERE product_id = ? AND quantity >= ?";
                updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setInt(1, qty);
                updateStmt.setString(2, productID);
                updateStmt.setInt(3, qty);
                int rowsUpdated = updateStmt.executeUpdate();

                if (rowsUpdated == 0) {
                    throw new SQLException("Insufficient stock or product detail not found for: " + productName);
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error processing sale item: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
            return false;
        } finally {
            try {
                if (saleStmt != null) saleStmt.close();
                if (saleItemStmt != null) saleItemStmt.close();
                if (updateStmt != null) updateStmt.close();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    /**
     * Process payments based on textbox amounts.
     */
    public boolean processPayments(String transactionID, double cashAmount, double qrAmount, double cardAmount) {
        PreparedStatement saleStmt = null;
        PreparedStatement paymentStmt = null;

        try {
            conn.setAutoCommit(false);

            // Ensure sale record exists
            String saleID = getOrCreateSaleID(transactionID);
            if (saleID == null) {
                saleID = generateUniqueSaleID();
                String saleQuery = "INSERT INTO sale (sale_id, transaction_number, sale_date) VALUES (?, ?, ?)";
                saleStmt = conn.prepareStatement(saleQuery);
                saleStmt.setString(1, saleID);
                saleStmt.setString(2, transactionID);
                saleStmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                saleStmt.executeUpdate();
            }

            // Insert payment records based on textbox amounts
            if (cashAmount > 0) {
                String paymentID = generateUniquePaymentID();
                String paymentQuery = "INSERT INTO payment (payment_id, sale_id, payment_type, amount, status) VALUES (?, ?, ?, ?, ?)";
                paymentStmt = conn.prepareStatement(paymentQuery);
                paymentStmt.setString(1, paymentID);
                paymentStmt.setString(2, saleID);
                paymentStmt.setString(3, "cash");
                paymentStmt.setDouble(4, cashAmount);
                paymentStmt.setString(5, "paid");
                paymentStmt.executeUpdate();
            }

            if (qrAmount > 0) {
                String paymentID = generateUniquePaymentID();
                String paymentQuery = "INSERT INTO payment (payment_id, sale_id, payment_type, amount, status) VALUES (?, ?, ?, ?, ?)";
                paymentStmt = conn.prepareStatement(paymentQuery);
                paymentStmt.setString(1, paymentID);
                paymentStmt.setString(2, saleID);
                paymentStmt.setString(3, "qr");
                paymentStmt.setDouble(4, qrAmount);
                paymentStmt.setString(5, "paid");
                paymentStmt.executeUpdate();
            }

            if (cardAmount > 0) {
                String paymentID = generateUniquePaymentID();
                String paymentQuery = "INSERT INTO payment (payment_id, sale_id, payment_type, amount, status) VALUES (?, ?, ?, ?, ?)";
                paymentStmt = conn.prepareStatement(paymentQuery);
                paymentStmt.setString(1, paymentID);
                paymentStmt.setString(2, saleID);
                paymentStmt.setString(3, "credit_card");
                paymentStmt.setDouble(4, cardAmount);
                paymentStmt.setString(5, "paid");
                paymentStmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error processing payments: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
            return false;
        } finally {
            try {
                if (saleStmt != null) saleStmt.close();
                if (paymentStmt != null) paymentStmt.close();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    private String generateUniqueSaleID() {
        String prefix = "S";
        String query = "SELECT MAX(CAST(SUBSTRING(sale_id, 2) AS UNSIGNED)) FROM sale WHERE sale_id LIKE ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, prefix + "%");
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getObject(1) != null) {
                int maxNum = rs.getInt(1);
                return prefix + String.format("%08d", maxNum + 1);
            }
        } catch (SQLException e) {
            System.err.println("Error generating sale ID: " + e.getMessage());
        }
        return prefix + "00000001";
    }

    private String generateUniqueSaleItemID() {
        String prefix = "SI";
        String query = "SELECT MAX(CAST(SUBSTRING(sale_item_id, 3) AS UNSIGNED)) FROM sale_item WHERE sale_item_id LIKE ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, prefix + "%");
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getObject(1) != null) {
                int maxNum = rs.getInt(1);
                return prefix + String.format("%07d", maxNum + 1);
            }
        } catch (SQLException e) {
            System.err.println("Error generating sale item ID: " + e.getMessage());
        }
        return prefix + "0000001";
    }

    private String generateUniquePaymentID() {
        String prefix = "P";
        String query = "SELECT MAX(CAST(SUBSTRING(payment_id, 2) AS UNSIGNED)) FROM payment WHERE payment_id LIKE ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, prefix + "%");
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getObject(1) != null) {
                int maxNum = rs.getInt(1);
                return prefix + String.format("%08d", maxNum + 1);
            }
        } catch (SQLException e) {
            System.err.println("Error generating payment ID: " + e.getMessage());
        }
        return prefix + "00000001";
    }

    private String getOrCreateSaleID(String transactionID) {
        String query = "SELECT sale_id FROM sale WHERE transaction_number = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, transactionID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("sale_id");
            }
        } catch (SQLException e) {
            System.err.println("Error checking sale ID: " + e.getMessage());
        }
        return null;
    }

    private String getProductID(String productName) {
        String query = "SELECT product_id FROM product WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, productName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("product_id");
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving product ID: " + e.getMessage());
        }
        return null;
    }
}