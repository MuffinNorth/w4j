package ru.muffinnorth.w4j.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import jep.DirectNDArray;
import jep.Interpreter;
import jep.SharedInterpreter;
import lombok.Builder;
import lombok.Setter;
import ru.muffinnorth.w4j.model.CanvasModel;
import ru.muffinnorth.w4j.model.NeuroModel;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;


@Builder
public class Applyer extends PyBridge {
    private CanvasModel model;
    @Setter
    private NeuroModel neuroModel;

    private Runnable callback;

    private void showAlert(){
        new Alert(Alert.AlertType.WARNING, "Данное действие может занять длительное время!").showAndWait();
    }

    public EventHandler<ActionEvent> actionEvent(){
        return actionEvent -> {
            try (Interpreter pyInterp = new SharedInterpreter()) {
                showAlert();
                File script = new File("src/main/java/ru/muffinnorth/w4j/controller/NeuroApply.py");
                Image image = model.getOriginal();
                pyInterp.set("width", image.getWidth());
                pyInterp.set("height", image.getHeight());
                PixelReader reader = image.getPixelReader();
                IntBuffer data = ByteBuffer.allocateDirect((int)(image.getWidth() * image.getHeight()) * 4).asIntBuffer();
                DirectNDArray<IntBuffer> nd = new DirectNDArray<>(data, data.capacity());
                int index;
                LinkedList<Integer> list = new LinkedList<>();
                for (int x = 0; x < image.getWidth(); x++) {
                    for (int y = 0; y < image.getHeight(); y++) {
                        list.add((int) (reader.getColor(x, y).grayscale().getRed() * 255));
                    }
                }
                data.put(list.stream().mapToInt(i->i).toArray());
                IntBuffer dataout = ByteBuffer.allocateDirect((int)(image.getWidth() * image.getHeight()) * 4 * 3).asIntBuffer();
                DirectNDArray<IntBuffer> ndout = new DirectNDArray<>(dataout, dataout.capacity());
                pyInterp.set("data", nd);
                pyInterp.set("out_data", ndout);
                pyInterp.set("scale", neuroModel.getScale());
                pyInterp.set("path_to_model", neuroModel.getPathToModel());
                pyInterp.runScript(script.getAbsolutePath());
                WritableImage imageOut = new WritableImage((int) image.getWidth(), (int) image.getHeight());
                PixelWriter writer = imageOut.getPixelWriter();

                int colorIndex = 0;
                int w = 0;
                int h = 0;
                double[] color = new double[3];
                System.out.println("write image");
                for (int i = 0; i < dataout.capacity(); i++) {
                    color[colorIndex] = (double) (dataout.get(i)) / 255;
                    colorIndex ++;
                    if(colorIndex == 3){
                        colorIndex = 0;
                        writer.setColor(w, h, new Color(color[0],color[1], color[2], 1));
                        w += 1;
                        if(w == image.getWidth()){
                            h += 1;
                            w = 0;
                        }
                    }
                }
                System.out.println("set image");
                model.setBackgroundWaffler(imageOut);
                callback.run();
            }
        };
    }
}
