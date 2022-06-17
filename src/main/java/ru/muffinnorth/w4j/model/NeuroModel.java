package ru.muffinnorth.w4j.model;

import lombok.Getter;
import lombok.Setter;

public class NeuroModel {
    private String pathToModel;

    @Getter
    @Setter
    private double scale = 0.5;

    public NeuroModel(){}



    public void setModelWeight(String path){
        if(path == null){
            return;
        }
        if(!path.isBlank() && !path.isEmpty()){
            pathToModel = path;
            return;
        }
        throw new RuntimeException("String is empty");
    }

    public void newModel(){
        setModelWeight(null);
    }


    public String getPathToModel(){
        return pathToModel;
    }
}
