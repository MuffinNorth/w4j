package ru.muffinnorth.w4j.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import jep.DirectNDArray;
import jep.Interpreter;
import jep.SharedInterpreter;
import lombok.Builder;
import ru.muffinnorth.w4j.model.CanvasModel;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;


@Builder
public class Applyer extends PyBridge {
    private CanvasModel model;

    public EventHandler<ActionEvent> actionEvent(){
        return actionEvent -> {
            try (Interpreter pyInterp = new SharedInterpreter()) {
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
                pyInterp.set("data", nd);
                pyInterp.runScript(script.getAbsolutePath());
                System.out.println(data.capacity());
            }
        };
    }
}
