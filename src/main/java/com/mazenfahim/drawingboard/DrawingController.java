package com.mazenfahim.drawingboard;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class DrawingController implements Initializable {
    @FXML
    public Button delete;
    @FXML
    public Label currentSlide;
    @FXML
    private Button add, next, previous;
    @FXML
    private Canvas canvas;

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
        Slide = new SlidesHandler();

        colorPicker.setValue(Color.BLACK);

        brushSize.valueProperty().addListener((obs, oldVal, newVal) -> gc.setLineWidth(newVal.doubleValue()));
        colorPicker.valueProperty().addListener((obs, oldVal, newVal) -> gc.setStroke(newVal));
        javafx.stage.Window.getWindows().addListener(
                (javafx.collections.ListChangeListener<javafx.stage.Window>) change -> {
                    while (change.next()) {
                        if (change.wasAdded()) {
                            for (javafx.stage.Window w : change.getAddedSubList()) {
                                if (w instanceof Stage && w != mainStackPane.getScene().getWindow()) {
                                    Stage dialog = (Stage) w;
                                    // When the dialog hides, bring main window back to front
                                    dialog.setOnHidden(e -> {
                                        Stage mainStage = (Stage) mainStackPane.getScene().getWindow();
                                        mainStage.toFront();
                                    });
                                }
                            }
                        }
                    }
                });
        clear.addEventHandler(MouseEvent.MOUSE_CLICKED,
                event -> gc.clearRect(0, 0, Slide.getCurrentCanvas().getWidth(), Slide.getCurrentCanvas().getHeight()));

        switchToCanvas(Slide.addSlide());
    }

    @FXML
    public void addSlide(ActionEvent actionEvent) {
        switchToCanvas(Slide.addSlide());
        updateCurrentSlide();
    }

    @FXML
    public void deleteSlide(ActionEvent actionEvent) {
        Slide.deleteSlide(mainStackPane);
        if (Slide.getCurrentCanvas() != null) {
            switchToCanvas(Slide.getCurrentCanvas());
            updateCurrentSlide();
        }
    }

    @FXML
    public void nextSlide(ActionEvent actionEvent) {
        switchToCanvas(Slide.forward());
        updateCurrentSlide();
    }

    @FXML
    public void previousSlide(ActionEvent actionEvent) {
        switchToCanvas(Slide.backward());
        updateCurrentSlide();
    }

    private void switchToCanvas(Canvas c) {
        canvas = c;
        bindingCanvas(canvas, mainStackPane);
        gc = canvas.getGraphicsContext2D();
        gc.setStroke(colorPicker.getValue());
        gc.setLineWidth(brushSize.getValue());
        setupCanvas(canvas);
    }

    private void setupCanvas(Canvas c) {
        c.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                gc.beginPath();
                gc.moveTo(event.getX(), event.getY());
                gc.stroke();
            }
        });
        c.setOnMouseDragged(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                gc.lineTo(e.getX(), e.getY());
                gc.stroke();
            }
        });
    }

    private void bindingCanvas(Canvas canvas, StackPane mainStackPane) {
        canvas.widthProperty().bind(mainStackPane.widthProperty());
        canvas.heightProperty().bind(mainStackPane.heightProperty());
        mainStackPane.getChildren().removeIf(node -> node instanceof Canvas);
        mainStackPane.getChildren().add(0, canvas);
    }

    private void updateCurrentSlide() {
        if (Slide.getCurrentCanvas() != null) {
            int currentSlideIndex = Slide.currentSlideIndex + 1;
            currentSlide.textProperty().bind(new SimpleIntegerProperty(currentSlideIndex).asString());
        }
    }
}