module org.provapoo3 {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.provapoo3 to javafx.fxml;
    exports org.provapoo3;
    exports org.provapoo3.controller;
    opens org.provapoo3.controller to javafx.fxml;
}