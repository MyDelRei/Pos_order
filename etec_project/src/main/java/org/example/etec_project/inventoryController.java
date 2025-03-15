package org.example.etec_project;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;

public class inventoryController {

    @FXML
    private TableColumn<String, String> Location_col;

    @FXML
    private TableColumn<String, String> barcode_col;

    @FXML
    private TableColumn<String, String> price_col;

    @FXML
    private TableColumn<String, String> product_name_col;

    @FXML
    private TableColumn<String, String> qty_c;

    @FXML
    private TableColumn<?, ?> category_col;

}
