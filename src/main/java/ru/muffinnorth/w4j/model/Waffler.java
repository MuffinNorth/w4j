package ru.muffinnorth.w4j.model;

import javafx.scene.image.Image;

public class Waffler {
    private final Image currentImage;
    private Image coloredImage;

    private boolean switchImage = false;

    public Waffler(Image image){
        this.currentImage = image;
    }

    public void setSwitch(boolean b){
        switchImage = b;
    }

    public void setColoredImage(Image image){
        coloredImage = image;
    }

    public Image getCurrentImage(){
        if(switchImage && coloredImage != null){
            return coloredImage;
        }
        return currentImage;
    }

    public Image getOriginalImage(){
        return currentImage;
    }


}
