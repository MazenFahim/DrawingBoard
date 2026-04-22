package com.mazenfahim.drawingboard;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

public class DrawingController implements Initializable {
    @FXML
    public Button delete;
    @FXML
    private Button add, next, previous;
    @FXML
    private Canvas canvas;
    @FXML
    ToolBar toolBar;
    @FXML
    ColorPicker colorPicker;
    @FXML
    Slider brushSize;
    @FXML
    Button clear;
    @FXML
    StackPane mainStackPane;

    private GraphicsContext gc;

    SlidesHandler Slide;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        canvas.widthProperty().bind(mainStackPane.widthProperty());
        canvas.heightProperty().bind(mainStackPane.heightProperty());

        mainStackPane.setAlignment(toolBar, Pos.TOP_CENTER);

        gc = canvas.getGraphicsContext2D();

        colorPicker.setValue(Color.BLACK);

        gc.setStroke(colorPicker.getValue());
        gc.setLineWidth(brushSize.getValue());

        brushSize.valueProperty().addListener((obs, oldVal, newVal) -> {
            gc.setLineWidth(newVal.doubleValue());
        });

        canvas.setOnMousePressed(event -> {
            if(event.getButton() == MouseButton.PRIMARY) {
                gc.beginPath();
                gc.moveTo(event.getX(), event.getY());
                gc.stroke();
            }
        });

        canvas.setOnMouseDragged(e -> {
            if(e.getButton() == MouseButton.PRIMARY) {
                gc.lineTo(e.getX(), e.getY());
                gc.stroke();
            }
        });

        clear.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        });

        colorPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            gc.setStroke(newVal);
        });

    }

    @FXML
    public void addSlide(ActionEvent actionEvent) {
        Slide = new SlidesHandler();
    }

    @FXML
    public void deleteSlide(ActionEvent actionEvent) {
        Slide.deleteSlide(mainStackPane);
    }
    @FXML
    public void nextSlide(ActionEvent actionEvent) {
        Slide.forward();
    }
    @FXML
    public void previousSlide(ActionEvent actionEvent) {
        Slide.backward();
    }
}
