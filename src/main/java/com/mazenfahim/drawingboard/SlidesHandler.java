package com.mazenfahim.drawingboard;

import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;

public class SlidesHandler {
    private List<Canvas> slidesList = new ArrayList<>();
    public int currentSlideIndex = -1;

    public Canvas addSlide() {
        Canvas canvas = new Canvas();
        slidesList.add(canvas);
        currentSlideIndex = slidesList.size() - 1;
        return canvas;
    }

    public Canvas getCurrentCanvas() {
        if (slidesList.isEmpty() || currentSlideIndex < 0) return null;
        return slidesList.get(currentSlideIndex);
    }

    public void deleteSlide(StackPane mainStackPane) {
        if (slidesList.isEmpty()) return;
        mainStackPane.getChildren().removeIf(node -> node instanceof Canvas);
        slidesList.remove(currentSlideIndex);
        if (currentSlideIndex >= slidesList.size()) {
            currentSlideIndex = slidesList.size() - 1;
        }
    }

    public Canvas forward() {
        if (currentSlideIndex < slidesList.size() - 1) {
            currentSlideIndex++;
        }
        return getCurrentCanvas();
    }

    public Canvas backward() {
        if (currentSlideIndex > 0) {
            currentSlideIndex--;
        }
        return getCurrentCanvas();
    }
}