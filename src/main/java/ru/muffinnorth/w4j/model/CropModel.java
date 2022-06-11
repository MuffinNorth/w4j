package ru.muffinnorth.w4j.model;

public class CropModel {
    private int[] cropPadding = new int[4];

    private CropModel(){}

    public static CropModel getBaseCropModel(){
        return new CropModel();
    }

    public void setTopPadding(int pixel){
        cropPadding[0] = pixel;
    }

    public void setLeftPadding(int pixel){
        cropPadding[1] = pixel;
    }

    public void setRightPadding(int pixel){
        cropPadding[2] = pixel;
    }

    public void setBottomPadding(int pixel){
        cropPadding[3] = pixel;
    }

    public int[] getPadding(){
        return cropPadding;
    }
}
