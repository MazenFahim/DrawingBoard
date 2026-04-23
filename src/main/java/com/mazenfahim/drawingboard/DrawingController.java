package com.mazenfahim.drawingboard;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

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
    @FXML
    private StackPane rootPane;
    @FXML
    private VBox rootBox;
    @FXML
    private HBox topBar;
    @FXML
    private Button maximizeButton;
    @FXML
    private Label statusText;
    @FXML
    private FontIcon eraserIcon;
    @FXML
    private FontIcon themeIcon;

    // Top Bar Fields
    private final PauseTransition topBarHideDelay = new PauseTransition(Duration.millis(1200));
    private boolean topBarVisible = true;
    private double dragOffsetX;
    private double dragOffsetY;

    private static final double TOP_REVEAL_ZONE = 6;
    private static final double TOP_BAR_HIDE_OFFSET = -18;

    private GraphicsContext gc;
    private boolean eraserActive = false;
    private boolean darkMode = false;

    // Eraser preview
    private Pane cursorOverlay;
    private Circle innerRing;
    private Circle outerRing;

    // Smooth eraser tracking
    private double lastEraserX;
    private double lastEraserY;
    private boolean hasLastEraserPoint = false;

    SlidesHandler Slide;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Slide = new SlidesHandler();

        colorPicker.setValue(Color.BLACK);

        brushSize.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (gc != null) {
                gc.setLineWidth(newVal.doubleValue());
            }
        });

        colorPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            eraserActive = false;
            hasLastEraserPoint = false;

            if (eraser != null)
                eraser.getStyleClass().remove("tool-btn-active");
            if (cursorOverlay != null)
                cursorOverlay.setVisible(false);

            if (gc != null) {
                gc.setStroke(newVal);
                gc.setLineWidth(brushSize.getValue());
            }
        });

        // Update preview circle size live as slider moves
        eraserSize.valueProperty().addListener((obs, oldVal, newVal) -> {
            double r = newVal.doubleValue() / 2.0;
            if (innerRing != null)
                innerRing.setRadius(r);
            if (outerRing != null)
                outerRing.setRadius(r);
        });

        javafx.stage.Window.getWindows().addListener(
                (javafx.collections.ListChangeListener<javafx.stage.Window>) change -> {
                    while (change.next()) {
                        if (change.wasAdded()) {
                            for (javafx.stage.Window w : change.getAddedSubList()) {
                                if (mainStackPane.getScene() != null &&
                                        w instanceof Stage &&
                                        w != mainStackPane.getScene().getWindow()) {
                                    Stage dialog = (Stage) w;
                                    dialog.setOnHidden(e -> {
                                        if (mainStackPane.getScene() != null) {
                                            Stage mainStage = (Stage) mainStackPane.getScene().getWindow();
                                            mainStage.toFront();
                                        }
                                    });
                                }
                            }
                        }
                    }
                });

        clear.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> clearCanvas());

        buildEraserPreview();
        switchToCanvas(Slide.addSlide());

        mainStackPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
                    switch (event.getCode()) {
                        case RIGHT -> {
                            switchToCanvas(Slide.forward());
                            updateCurrentSlide();
                            event.consume();
                        }
                        case LEFT -> {
                            switchToCanvas(Slide.backward());
                            updateCurrentSlide();
                            event.consume();
                        }
                        case C -> {
                            clearCanvas();
                            event.consume();
                        }
                        case E -> {
                            toggleEraser();
                            event.consume();
                        }
                        default -> {
                        }
                    }
                });
            }
        });

        rootBox.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((windowObs, oldWindow, newWindow) -> {
                    if (newWindow instanceof Stage stage) {
                        stage.maximizedProperty().addListener((o, oldVal, newVal) -> updateMaximizeButton());
                        updateMaximizeButton();
                    }
                });
            }
        });

        setupWindowBar();
    }

    private void buildEraserPreview() {
        double r = eraserSize.getValue() / 2.0;

        outerRing = new Circle(r);
        outerRing.setFill(Color.TRANSPARENT);
        outerRing.setStroke(Color.rgb(255, 255, 255, 0.9));
        outerRing.setStrokeWidth(2.5);
        outerRing.setStrokeType(StrokeType.OUTSIDE);
        outerRing.setMouseTransparent(true);

        innerRing = new Circle(r);
        innerRing.setFill(Color.TRANSPARENT);
        innerRing.setStroke(Color.rgb(40, 40, 40, 0.85));
        innerRing.setStrokeWidth(1.5);
        innerRing.setStrokeType(StrokeType.CENTERED);
        innerRing.setMouseTransparent(true);

        cursorOverlay = new Pane(outerRing, innerRing);
        cursorOverlay.setMouseTransparent(true);
        cursorOverlay.setPickOnBounds(false);
        cursorOverlay.setVisible(false);
        cursorOverlay.prefWidthProperty().bind(mainStackPane.widthProperty());
        cursorOverlay.prefHeightProperty().bind(mainStackPane.heightProperty());

        mainStackPane.getChildren().add(cursorOverlay);
    }

    private void movePreview(double x, double y) {
        outerRing.setLayoutX(x);
        outerRing.setLayoutY(y);
        innerRing.setLayoutX(x);
        innerRing.setLayoutY(y);
    }

    @FXML
    public void addSlide() {
        switchToCanvas(Slide.addSlide());
        updateCurrentSlide();
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
    public void deleteSlide() {
        Slide.deleteSlide(mainStackPane);
        if (Slide.getCurrentCanvas() != null) {
            switchToCanvas(Slide.getCurrentCanvas());
            updateCurrentSlide();
        }
    }

    @FXML
    public void toggleEraser() {
        eraserActive = !eraserActive;
        hasLastEraserPoint = false;

        if (eraserActive) {
            eraser.getStyleClass().add("tool-btn-active");
            eraserIcon.setIconColor(Color.web("#4338ca"));
        } else {
            if (gc != null) {
                gc.setStroke(colorPicker.getValue());
                gc.setLineWidth(brushSize.getValue());
            }
            eraser.getStyleClass().remove("tool-btn-active");
            eraserIcon.setIconColor(Color.web("#475569"));
            if (cursorOverlay != null) cursorOverlay.setVisible(false);
        }
    }

    @FXML
    public void toggleBackground() {
        darkMode = !darkMode;
        mainStackPane.setStyle(darkMode
                ? "-fx-background-color: #000000;"
                : "-fx-background-color: #ffffff;");
        themeIcon.setIconLiteral(darkMode ? "mdi2w-white-balance-sunny" : "mdi2w-weather-night");
    }

    private void switchToCanvas(Canvas c) {
        canvas = c;
        bindingCanvas(canvas, mainStackPane);

        gc = canvas.getGraphicsContext2D();
        gc.setStroke(colorPicker.getValue());
        gc.setLineWidth(brushSize.getValue());

        eraserActive = false;
        hasLastEraserPoint = false;

        if (eraser != null)
            eraser.getStyleClass().remove("tool-btn-active");
        if (cursorOverlay != null)
            cursorOverlay.setVisible(false);

        setupCanvas(canvas);

        if (cursorOverlay != null)
            cursorOverlay.toFront();
    }

    private void setupCanvas(Canvas c) {
        c.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                if (eraserActive) {
                    eraseCircle(event.getX(), event.getY(), eraserSize.getValue());

                    lastEraserX = event.getX();
                    lastEraserY = event.getY();
                    hasLastEraserPoint = true;

                    movePreview(event.getX(), event.getY());
                    cursorOverlay.setVisible(true);
                } else {
                    hasLastEraserPoint = false;
                    gc.beginPath();
                    gc.moveTo(event.getX(), event.getY());
                    gc.stroke();
                }
            }
        });

        c.setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown()) {
                if (eraserActive) {
                    double x = event.getX();
                    double y = event.getY();
                    double size = eraserSize.getValue();

                    if (hasLastEraserPoint) {
                        eraseLine(lastEraserX, lastEraserY, x, y, size);
                    } else {
                        eraseCircle(x, y, size);
                    }

                    lastEraserX = x;
                    lastEraserY = y;
                    hasLastEraserPoint = true;

                    movePreview(x, y);
                    cursorOverlay.setVisible(true);
                } else {
                    gc.lineTo(event.getX(), event.getY());
                    gc.stroke();
                }
            }

            if (eraserActive) {
                movePreview(event.getX(), event.getY());
            }
        });

        c.setOnMouseReleased(event -> hasLastEraserPoint = false);

        c.setOnMouseEntered(e -> {
            if (eraserActive) {
                movePreview(e.getX(), e.getY());
                cursorOverlay.setVisible(true);
            }
        });

        c.setOnMouseMoved(e -> {
            if (eraserActive) {
                movePreview(e.getX(), e.getY());
                cursorOverlay.setVisible(true);
            }
        });

        c.setOnMouseExited(e -> {
            if (cursorOverlay != null) {
                cursorOverlay.setVisible(false);
            }
            hasLastEraserPoint = false;
        });
    }

    private void eraseLine(double x1, double y1, double x2, double y2, double size) {
        double radius = size / 2.0;
        double dx = x2 - x1;
        double dy = y2 - y1;
        double distance = Math.hypot(dx, dy);

        if (distance == 0) {
            eraseCircle(x1, y1, size);
            return;
        }

        double step = Math.max(1.0, radius * 0.35);
        int steps = Math.max(1, (int) Math.ceil(distance / step));

        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double x = x1 + (dx * t);
            double y = y1 + (dy * t);
            eraseCircle(x, y, size);
        }
    }

    private void eraseCircle(double centerX, double centerY, double size) {
        double radius = size / 2.0;
        double radiusSquared = radius * radius;

        PixelWriter pixelWriter = gc.getPixelWriter();

        int minX = (int) Math.floor(centerX - radius);
        int maxX = (int) Math.ceil(centerX + radius);
        int minY = (int) Math.floor(centerY - radius);
        int maxY = (int) Math.ceil(centerY + radius);

        int canvasWidth = (int) canvas.getWidth();
        int canvasHeight = (int) canvas.getHeight();

        for (int y = minY; y <= maxY; y++) {
            if (y < 0 || y >= canvasHeight)
                continue;

            for (int x = minX; x <= maxX; x++) {
                if (x < 0 || x >= canvasWidth)
                    continue;

                double dx = (x + 0.5) - centerX;
                double dy = (y + 0.5) - centerY;

                if ((dx * dx) + (dy * dy) <= radiusSquared) {
                    pixelWriter.setArgb(x, y, 0x00000000);
                }
            }
        }
    }

    private void bindingCanvas(Canvas canvas, StackPane mainStackPane) {
        canvas.widthProperty().bind(mainStackPane.widthProperty());
        canvas.heightProperty().bind(mainStackPane.heightProperty());
        mainStackPane.getChildren().removeIf(node -> node instanceof Canvas);
        mainStackPane.getChildren().add(0, canvas);
    }

    private void updateCurrentSlide() {
        if (Slide.getCurrentCanvas() != null) {
            int idx = Slide.currentSlideIndex + 1;
            currentSlide.textProperty().bind(new SimpleIntegerProperty(idx).asString());
            if (statusText != null) {
                statusText.setText("Ready  ·  Slide " + idx);
            }
        }
    }

    @FXML
    private void clearCanvas() {
        gc.clearRect(0, 0, Slide.getCurrentCanvas().getWidth(), Slide.getCurrentCanvas().getHeight());
    }

    private void setupWindowBar() {
        topBarHideDelay.setOnFinished(e -> {
            if (!topBar.isHover()) {
                animateTopBar(false);
            }
        });

        topBar.setOnMousePressed(event -> {
            Stage stage = getStage();
            if (stage == null)
                return;

            dragOffsetX = event.getSceneX();
            dragOffsetY = event.getSceneY();
        });

        topBar.setOnMouseDragged(event -> {
            Stage stage = getStage();
            if (stage == null || stage.isMaximized())
                return;

            stage.setX(event.getScreenX() - dragOffsetX);
            stage.setY(event.getScreenY() - dragOffsetY);
        });

        topBar.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                toggleMaximizeWindow();
            }
        });

        topBar.setOnMouseEntered(event -> showTopBarNow());

        topBar.setOnMouseExited(event -> {
            if (!topBar.isHover()) {
                scheduleTopBarHide();
            }
        });
    }

    @FXML
    private void handleRootMouseMove(MouseEvent event) {
        if (event.getY() <= TOP_REVEAL_ZONE) {
            showTopBarNow();
        } else if (!topBar.isHover()) {
            scheduleTopBarHide();
        }
    }

    @FXML
    private void handleRootMouseExit(MouseEvent event) {
        scheduleTopBarHide();
    }

    private void showTopBarNow() {
        topBarHideDelay.stop();
        animateTopBar(true);
    }

    private void scheduleTopBarHide() {
        topBarHideDelay.stop();

        if (!topBarVisible || topBar.isHover()) {
            return;
        }

        topBarHideDelay.playFromStart();
    }

    private void animateTopBar(boolean show) {
        if (topBarVisible == show) {
            return;
        }

        topBarVisible = show;

        if (show) {
            topBar.setVisible(true);
            topBar.setOpacity(0);
            topBar.setTranslateY(TOP_BAR_HIDE_OFFSET);

            FadeTransition fade = new FadeTransition(Duration.millis(180), topBar);
            fade.setToValue(1);

            TranslateTransition slide = new TranslateTransition(Duration.millis(180), topBar);
            slide.setToY(0);

            new ParallelTransition(fade, slide).play();
        } else {
            FadeTransition fade = new FadeTransition(Duration.millis(160), topBar);
            fade.setToValue(0);

            TranslateTransition slide = new TranslateTransition(Duration.millis(160), topBar);
            slide.setToY(TOP_BAR_HIDE_OFFSET);

            ParallelTransition transition = new ParallelTransition(fade, slide);
            transition.setOnFinished(e -> {
                topBar.setVisible(false);
            });
            transition.play();
        }
    }

    @FXML
    private void minimizeWindow() {
        Stage stage = getStage();
        if (stage != null) {
            stage.setIconified(true);
        }
    }

    @FXML
    private void toggleMaximizeWindow() {
        Stage stage = (Stage) maximizeButton.getScene().getWindow();
        boolean maximized = stage.isMaximized();
        stage.setMaximized(!maximized);

        FontIcon icon = (FontIcon) maximizeButton.getGraphic();
        icon.setIconLiteral(maximized ? "mdi2w-window-maximize" : "mdi2w-window-restore");
    }

    @FXML
    private void closeWindow() {
        Stage stage = getStage();
        if (stage != null) {
            stage.close();
        }
    }

    private Stage getStage() {
        if (rootBox == null || rootBox.getScene() == null) {
            return null;
        }
        return (Stage) rootBox.getScene().getWindow();
    }

    private void updateMaximizeButton() {
        Stage stage = getStage();
        if (stage != null && maximizeButton != null) {
            FontIcon icon = (FontIcon) maximizeButton.getGraphic();
            icon.setIconLiteral(stage.isMaximized() ? "mdi2w-window-restore" : "mdi2w-window-maximize");
        }
    }
}