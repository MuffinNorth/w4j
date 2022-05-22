package ru.muffinnorth.w4j.model;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;

public class Waffler {
    private Image currentImage;

    public Waffler(Image image){
        this.currentImage = image;
    }

    public Image getCurrentImage(){
        return currentImage;
    }


}
