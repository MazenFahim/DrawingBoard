package com.mazenfahim.drawingboard;

import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;

public class SlidesHandler {
    public static List<Canvas> slidesList = new ArrayList<>();
    public Canvas canvas;
    public static int currentSlideIndex = -1;
    public static GraphicsContext gc;
    SlidesHandler(){
        canvas = new Canvas();
        gc = canvas.getGraphicsContext2D();
        currentSlideIndex++;
    }

    public void deleteSlide(StackPane mainStackPane) {
        mainStackPane.getChildren().remove(slidesList.get(currentSlideIndex));
        slidesList.remove(currentSlideIndex);
        if(currentSlideIndex == slidesList.size() - 1){
            currentSlideIndex--;
        }
    }
    public static void forward(){
        currentSlideIndex++;
    }
    public static void backward(){
        currentSlideIndex--;
    }

}
