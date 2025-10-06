module org.provapoo3 {
    requires javafx.controls;
    requires javafx.fxml;

    exports org.provapoo3;
    exports org.provapoo3.controller;
    exports org.provapoo3.model;
    
    opens org.provapoo3 to javafx.fxml;
    opens org.provapoo3.controller to javafx.fxml;
    opens org.provapoo3.model to javafx.base;
}
