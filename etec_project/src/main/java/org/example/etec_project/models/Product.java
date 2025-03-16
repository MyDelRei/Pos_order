package org.example.etec_project.models;

public class Product {
    private String productName;
    private double price;
    private int quantity;
    private String expireDate;
    private String barcode;
    private String location;

    // Constructor
    public Product(String productName, double price, int quantity, String expireDate,
                   String barcode, String location) {
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.expireDate = expireDate;
        this.barcode = barcode;
        this.location = location;
    }

    // Getters
    public String getProductName() {
        return productName;
    }

    public double getPrice() {
        return price;
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

    public String getLocation() {
        return location;
    }

    // Setters
    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
