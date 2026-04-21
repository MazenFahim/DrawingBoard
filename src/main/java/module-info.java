module com.mazenfahim.drawingboard {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mazenfahim.drawingboard to javafx.fxml;
    exports com.mazenfahim.drawingboard;
}