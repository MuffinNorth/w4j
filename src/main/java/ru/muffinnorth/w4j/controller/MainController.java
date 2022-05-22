package ru.muffinnorth.w4j.controller;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jep.Interpreter;
import jep.JepConfig;
import jep.NDArray;
import jep.SharedInterpreter;
import ru.muffinnorth.w4j.model.CanvasModel;

import java.io.*;
import java.util.Arrays;

public class MainController {

    @FXML
    Button loadButton;

    @FXML
    Canvas canvas;

    @FXML
    Slider scaleSlider;

    @FXML
    TextField scaleField;

    @FXML
    Button findButton;

    @FXML
    ToggleButton gridToggle;

    @FXML
    ImageView cellView;

    @FXML
    ComboBox<String> gridSizeComboBox;

    @FXML
    Label selectedCoordinateLabel;

    @FXML
    Label outLabel;

    @FXML
    Button closeButton;

    private CanvasModel model;

    @FXML
    private void onClickLoadButton(){
        {
            Stage thisStage = (Stage) loadButton.getScene().getWindow();
            FileChooser filechooser = new FileChooser();
            filechooser.setTitle("Open File");
            File selected = filechooser.showOpenDialog(thisStage);
            model = new CanvasModel(new Image(selected.getAbsolutePath()));
        }
        prepare();
        init();
        connectListener();
        draw();

        /*findButton.setOnAction(actionEvent -> {
            try(Interpreter interpreter = new SharedInterpreter()){
                PixelReader reader = model.getImage().getPixelReader();
                int[] pixels = new int[(int) (model.getImage().getHeight()*model.getImage().getWidth())];
                int i = 0;
                for (int x = 0; x < model.getImage().getWidth(); x++) {
                    for (int y = 0; y < model.getImage().getHeight(); y++) {
                        pixels[i] = (int) (reader.getColor(x, y).grayscale().getRed()*255);
                        i++;
                    }
                }
                System.out.println(Arrays.toString(pixels));
                NDArray<int[]> nd = new NDArray<>(pixels, (int)model.getImage().getWidth(), (int)model.getImage().getHeight());
                interpreter.set("inp", nd);
                System.out.println(interpreter.getValue("inp.shape"));
            }
        });*/

        findButton.setOnAction(actionEvent -> {
            Thread t = new Thread(() -> {
                System.out.println("run thread");
                try(Interpreter pyInterp = new SharedInterpreter()) {
                    File npAnalyser = new File("src/main/java/ru/muffinnorth/w4j/controller/NPanalyse.py");
                    File cluster = new File("src/main/java/ru/muffinnorth/w4j/controller/Cluster.py");
                    pyInterp.runScript(npAnalyser.getAbsolutePath());
                    pyInterp.exec("field_size = 0.2");
                    pyInterp.exec("dpi = 1200");

                    PixelReader reader = model.getImage().getPixelReader();
                    int[] pixels = new int[(int) (model.getImage().getHeight()*model.getImage().getWidth())];
                    int i = 0;
                    for (int x = 0; x < model.getImage().getWidth(); x++) {
                        for (int y = 0; y < model.getImage().getHeight(); y++) {
                            pixels[i] = (int) (reader.getColor(x, y).grayscale().getRed()*255);
                            i++;
                        }
                    }
                    NDArray<int[]> nd = new NDArray<>(pixels, (int)model.getImage().getWidth(), (int)model.getImage().getHeight());
                    pyInterp.set("img", nd);

                    System.out.println("run analyse");
                    pyInterp.runScript(cluster.getAbsolutePath());
                    System.out.println(pyInterp.getValue("clusters"));
                }
            });
            t.start();
            closeButton.setOnAction(actionEvent1 -> {
                System.out.println("stop");
                t.stop();
            });
        });

    }

    private void connectListener(){
        scaleSlider.valueProperty().addListener((observableValue, number, t1) -> {
            model.setScale(t1.doubleValue());
            scaleField.setText(String.format("%.2f", t1.doubleValue()));
            draw();
        });

        gridToggle.setOnAction(actionEvent -> {
            model.setNeedGrid(gridToggle.isSelected());
            draw();
        });

        gridSizeComboBox.getSelectionModel().selectedItemProperty().addListener((observableValue, sold, snew) -> {
            try {
                double dnew = Double.parseDouble(snew);
                model.setGridStep(dnew);
                draw();
            } catch (NumberFormatException ex){
                gridSizeComboBox.getSelectionModel().select(sold);
            }
        });

        canvas.setOnMouseClicked(model.getCoordinateListener(
                () -> {
                    draw();
                    cellView.setImage(model.getSelectedImage());
                    selectedCoordinateLabel.setText(String.format("X:%d Y:%d",
                            (int) model.getSelectedCell().getX(),
                            (int) model.getSelectedCell().getY()));
                }
        ));
    }

    private void prepare(){
        scaleSlider.valueProperty().setValue(1.0);
        scaleField.setText("1.00");
        gridToggle.setSelected(false);
        gridSizeComboBox.getItems().clear();
        gridSizeComboBox.getItems().addAll("20", "40");
        gridSizeComboBox.getSelectionModel().select(0);
    }

    private void init(){
        model.setGridStep(
                Double.parseDouble(gridSizeComboBox.getSelectionModel().getSelectedItem())
        );
    }

    private void draw(){
        canvas.setWidth(model.getWidth());
        canvas.setHeight(model.getHeight());
        canvas.getGraphicsContext2D().clearRect(0,0, model.getHeight(), model.getWidth());
        model.draw(canvas.getGraphicsContext2D());
    }

    @FXML
    private void onScaleChange(){
    }
}
