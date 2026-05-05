module com.mazenfahim.drawingboard {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.kordamp.ikonli.javafx;

    opens com.mazenfahim.drawingboard to javafx.fxml;
    exports com.mazenfahim.drawingboard;
    exports com.mazenfahim.drawingboard.controller;
    opens com.mazenfahim.drawingboard.controller to javafx.fxml;
}