package com.mazenfahim.drawingboard;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
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
    public HBox toolBar;
    @FXML
    private Button eraser;
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
    @FXML
    Slider eraserSize;
    @FXML
    Button toggleBg;


    private GraphicsContext gc;
    private boolean eraserActive = false;
    private boolean darkMode = false;

    SlidesHandler Slide;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Slide = new SlidesHandler();

        colorPicker.setValue(Color.BLACK);
        brushSize.valueProperty().addListener((obs, oldVal, newVal) -> gc.setLineWidth(newVal.doubleValue()));
        colorPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            eraserActive = false;
            if (eraser != null) eraser.setStyle("");
            gc.setStroke(newVal);
            gc.setLineWidth(brushSize.getValue());
        });
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
                event -> clearCanvas()
        );

        eraserSize.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (eraserActive) gc.setLineWidth(newVal.doubleValue());
        });

        switchToCanvas(Slide.addSlide());

        mainStackPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
                    switch (event.getCode()) {
                        case RIGHT -> { switchToCanvas(Slide.forward()); updateCurrentSlide(); event.consume(); }
                        case LEFT  -> { switchToCanvas(Slide.backward()); updateCurrentSlide(); event.consume(); }
                        case C -> { clearCanvas(); event.consume(); }
                        default    -> {}
                    }
                });
            }
        });
    }

    @FXML
    public void addSlide() {
        switchToCanvas(Slide.addSlide());
        updateCurrentSlide();
    }

    @FXML
    public void deleteSlide() {
        Slide.deleteSlide(mainStackPane);
        if (Slide.getCurrentCanvas() != null) {
            switchToCanvas(Slide.getCurrentCanvas());
            updateCurrentSlide();
        }
    }

    @FXML
    public void nextSlide() {
        switchToCanvas(Slide.forward());
        updateCurrentSlide();
    }

    @FXML
    public void previousSlide() {
        switchToCanvas(Slide.backward());
        updateCurrentSlide();
    }

    @FXML
    public void toggleEraser() {
        eraserActive = !eraserActive;
        if (eraserActive) {
            eraser.setStyle("-fx-background-color: #eef2ff; -fx-border-color: #6366f1; -fx-text-fill: #6366f1;");
        } else {
            gc.setStroke(colorPicker.getValue());
            gc.setLineWidth(brushSize.getValue());
            eraser.setStyle("");
        }
    }

    @FXML
    public void toggleBackground() {
        darkMode = !darkMode;
        mainStackPane.setStyle(darkMode
                ? "-fx-background-color: #000000;"
                : "-fx-background-color: #ffffff;");
        toggleBg.setText(darkMode ? "☀ Light" : "🌙 Dark");
    }

    private void switchToCanvas(Canvas c) {
        canvas = c;
        bindingCanvas(canvas, mainStackPane);
        gc = canvas.getGraphicsContext2D();
        gc.setStroke(colorPicker.getValue());
        gc.setLineWidth(brushSize.getValue());
        eraserActive = false;
        if (eraser != null) eraser.setStyle("");
        setupCanvas(canvas);
    }

    private void setupCanvas(Canvas c) {
        c.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                if (eraserActive) {
                    double size = eraserSize.getValue();
                    gc.clearRect(event.getX() - size / 2, event.getY() - size / 2, size, size);
                } else {
                    gc.beginPath();
                    gc.moveTo(event.getX(), event.getY());
                    gc.stroke();
                }
            }
        });

        c.setOnMouseDragged(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (eraserActive) {
                    double size = eraserSize.getValue();
                    gc.clearRect(e.getX() - size / 2, e.getY() - size / 2, size, size);
                } else {
                    gc.lineTo(e.getX(), e.getY());
                    gc.stroke();
                }
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

    private void clearCanvas() {
        gc.clearRect(0, 0, Slide.getCurrentCanvas().getWidth(), Slide.getCurrentCanvas().getHeight());
    }

}