package org.example.etec_project.models;

public class Report {
    private String sale_item_id;
    private String sale_id;
    private String transaction_number;
    private String product_name;
    private java.math.BigDecimal total;

    public Report(String sale_item_id, String sale_id, String transaction_number, String product_name, java.math.BigDecimal total) {
        this.sale_item_id = sale_item_id;
        this.sale_id = sale_id;
        this.transaction_number = transaction_number;
        this.product_name = product_name;
        this.total = total;
    }

    // Getters and Setters for the properties
    public String getSale_item_id() {
        return sale_item_id;
    }

    public void setSale_item_id(String sale_item_id) {
        this.sale_item_id = sale_item_id;
    }

    public String getSale_id() {
        return sale_id;
    }

    public void setSale_id(String sale_id) {
        this.sale_id = sale_id;
    }

    public String getTransaction_number() {
        return transaction_number;
    }

    public void setTransaction_number(String transaction_number) {
        this.transaction_number = transaction_number;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public java.math.BigDecimal getTotal() {
        return total;
    }

    public void setTotal(java.math.BigDecimal total) {
        this.total = total;
    }
}