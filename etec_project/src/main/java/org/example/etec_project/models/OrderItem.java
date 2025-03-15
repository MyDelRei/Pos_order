package org.example.etec_project.models;

public class OrderItem {
    private String productName;
    private int quantity;
    private double unitPrice;
    private double totalPrice;

    public OrderItem(String productName, int quantity, double price) {
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = price;
        this.totalPrice = quantity * price;
    }

    // Getters and setters
    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.totalPrice = this.quantity * this.unitPrice; // Update total price based on unit price
    }
}
