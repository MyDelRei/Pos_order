package org.example.etec_project.models;

public class inventoryItem {
    private String productName;
    private String location;
    private int quantity;
    private String expireDate;
    private String barcode;
    private double price;

    // Constructor
    public inventoryItem(String productName, String location, int quantity, String expireDate, String barcode, double price) {
        this.productName = productName;
        this.location = location;
        this.quantity = quantity;
        this.expireDate = expireDate;
        this.barcode = barcode;
        this.price = price;
    }

    // Getters (MUST be public for JavaFX PropertyValueFactory)
    public String getProductName() {
        return productName;
    }

    public String getLocation() {
        return location;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public String getBarcode() {
        return barcode;
    }

    public double getPrice() {
        return price;
    }
}

