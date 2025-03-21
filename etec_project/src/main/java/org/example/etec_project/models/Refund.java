package org.example.etec_project.models;

public class Refund {
    private String saleId;
    private String saleItemId;
    private String barcode;
    private String productName;
    private double total;
    private String transactionNumber;

    // Constructor
    public Refund(String saleId, String saleItemId, String barcode, String productName, double total, String transactionNumber) {
        this.saleId = saleId;
        this.saleItemId = saleItemId;
        this.barcode = barcode;
        this.productName = productName;
        this.total = total;
        this.transactionNumber = transactionNumber;
    }

    // Getters and setters
    public String getSaleId() { return saleId; }
    public String getSaleItemId() { return saleItemId; }
    public String getBarcode() { return barcode; }
    public String getProductName() { return productName; }
    public double getTotal() { return total; }
    public String getTransactionNumber() { return transactionNumber; }
}

