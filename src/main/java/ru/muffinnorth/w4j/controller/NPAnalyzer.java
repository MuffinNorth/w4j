package ru.muffinnorth.w4j.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.image.PixelReader;
import jep.Interpreter;
import jep.NDArray;
import jep.SharedInterpreter;
import lombok.Builder;
import ru.muffinnorth.w4j.model.CanvasModel;
import ru.muffinnorth.w4j.model.Cell;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Builder
public class NPAnalyzer extends PyBridge{
    private CanvasModel model;
    private List<Cell> outList;
    private Consumer<List<Cell>> callback;


    private void showAlert(){
        new Alert(Alert.AlertType.WARNING, "Данное действие может занять длительное время!").showAndWait();
    }

    public EventHandler<ActionEvent> actionEvent(){
        return actionEvent -> {
            showAlert();
            try (Interpreter pyInterp = new SharedInterpreter()) {
                System.out.println("Start analyze...");
                File npAnalyser = new File("src/main/java/ru/muffinnorth/w4j/controller/NPanalyse.py");
                File cluster = new File("src/main/java/ru/muffinnorth/w4j/controller/Cluster.py");
                pyInterp.runScript(npAnalyser.getAbsolutePath());
                pyInterp.exec("field_size = " + model.getGridStep());
                pyInterp.exec("dpi = 1200");

                PixelReader reader = model.getImage().getPixelReader();
                int[] pixels = new int[(int) (model.getImage().getHeight() * model.getImage().getWidth())];
                int i = 0;

                for (int x = 0; x < model.getImage().getWidth(); x++) {
                    for (int y = 0; y < model.getImage().getHeight(); y++) {
                        pixels[i] = (int) (reader.getColor(x, y).grayscale().getRed() * 255);
                        i++;
                    }
                }

                NDArray<int[]> nd = new NDArray<>(pixels, (int) model.getImage().getWidth(), (int) model.getImage().getHeight());
                pyInterp.set("img", nd);
                pyInterp.runScript(cluster.getAbsolutePath());
                Long count = (Long) pyInterp.getValue("len(clusters)");
                for (int j = 0; j < count.intValue(); j++) {
                    pyInterp.exec("cell = clusters[%d]".formatted(j));
                    Long y = (Long) pyInterp.getValue("cell.x");
                    Long x = (Long) pyInterp.getValue("cell.y");
                    Long n = (Long) pyInterp.getValue("cell.cluster");
                    Cell cell = Cell.builder()
                            .clusterCount(n.intValue())
                            .coordinate(x.intValue(), y.intValue())
                            .build();
                    outList.add(cell);
                }
                Collections.sort(outList);
                callback.accept(outList);
            }
        };
    }

}
