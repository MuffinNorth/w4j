package ru.muffinnorth.w4j.controller;

import com.google.common.primitives.Ints;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jep.Interpreter;
import jep.NDArray;
import jep.SharedInterpreter;
import org.jfree.chart.fx.ChartViewer;
import ru.muffinnorth.w4j.listeners.ChangeTargetListener;
import ru.muffinnorth.w4j.model.CanvasModel;
import ru.muffinnorth.w4j.model.Cell;
import ru.muffinnorth.w4j.model.CropModel;

import java.io.*;
import java.util.Optional;

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

    @FXML
    VBox histogramBox;

    @FXML
    TextField topPadding;

    @FXML
    TextField leftPadding;

    @FXML
    TextField bottomPadding;

    @FXML
    TextField rightPadding;

    @FXML
    ToggleButton paddingToggle;

    @FXML
    Button cropButton;

    private CanvasModel model;

    @FXML
    private void onClickLoadButton() {
        {
            Stage thisStage = (Stage) loadButton.getScene().getWindow();
            FileChooser filechooser = new FileChooser();
            filechooser.setTitle("Open File");
            File selected = filechooser.showOpenDialog(thisStage);
            if(selected == null){
                new Alert(Alert.AlertType.ERROR, "Не выбрано изображение").showAndWait();
                return;
            }
            model = new CanvasModel(new Image(selected.getAbsolutePath()));
        }
        unlockAll();
        prepare();
        init();
        connectListener();
        draw();


        //FIXME
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
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Внимание!");
            alert.getDialogPane().setContent(new Label("Функция поиска репрезентативных областей может выполняться продолжительное время. Длительность операции зависит от размера шага и разрешения исходного изображения. "));
            alert.showAndWait();
            System.out.println("run thread");
            try (Interpreter pyInterp = new SharedInterpreter()) {
                File npAnalyser = new File("src/main/java/ru/muffinnorth/w4j/controller/NPanalyse.py");
                File cluster = new File("src/main/java/ru/muffinnorth/w4j/controller/Cluster.py");
                pyInterp.runScript(npAnalyser.getAbsolutePath());
                pyInterp.exec("field_size = 0.2");
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
                System.out.println("run analyse");
                pyInterp.runScript(cluster.getAbsolutePath());
                Long count = (Long) pyInterp.getValue("len(clusters)");
                for (int j = 0; j < count.intValue(); j++) {
                    System.out.printf("Collect %d cell%n", j);
                    pyInterp.exec("cell = clusters[%d]".formatted(j));
                    Long y = (Long) pyInterp.getValue("cell.x");
                    Long x = (Long) pyInterp.getValue("cell.y");
                    Long n = (Long) pyInterp.getValue("cell.cluster");
                    Cell cell = Cell.builder()
                            .clusterCount(n.intValue())
                            .coordinate(x.intValue(), y.intValue())
                            .build();
                    model.appendCell(cell);
                }
                draw();
            }
            closeButton.setOnAction(actionEvent1 -> {
            });
        });

    }

    private void unlockAll() {
        // TODO: 10.06.2022 add lock and unlock ui 
    }

    private void connectListener() {

        scaleSlider.valueProperty().addListener((observableValue, number, t1) -> {
            model.setScale(t1.doubleValue());
            scaleField.setText(String.format("%.2f", t1.doubleValue()));
            draw();
        });

        gridToggle.setOnAction(actionEvent -> {
            model.setNeedGrid(gridToggle.isSelected());
            draw();
        });

        gridSizeComboBox
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((observableValue, sold, snew) -> {
                    try {
                        double dnew = Double.parseDouble(snew);
                        model.setGridStep(dnew);
                        draw();
                    } catch (NumberFormatException ex) {
                        gridSizeComboBox.getSelectionModel().select(sold);
                    }
                });

        canvas.setOnMouseClicked(
                model.getCoordinateListener(() -> {
                            histogramBox.getChildren().clear();
                            ChartViewer v = new ChartViewer(model.createSelectedHist());
                            v.setMinWidth(histogramBox.getMinWidth());
                            v.setMinHeight(200);
                            histogramBox.getChildren().add(v);
                            draw();
                            cellView.setImage(model.getSelectedImage());
                            selectedCoordinateLabel.setText(String.format("X:%d Y:%d",
                                    (int) model.getSelectedCell().getX(),
                                    (int) model.getSelectedCell().getY()));
                        }
                ));

        topPadding.textProperty().addListener(new ChangeTargetListener(integer -> {
            CropModel cropModel = model.getCropModel();
            if(integer <= model.getImage().getHeight() - cropModel.getPadding()[3]){
                cropModel.setTopPadding(integer);
                draw();
            }
        }));

        bottomPadding.textProperty().addListener(new ChangeTargetListener(integer -> {
            CropModel cropModel = model.getCropModel();
            if(integer <= model.getImage().getHeight() - cropModel.getPadding()[0]){
                cropModel.setBottomPadding(integer);
                draw();
            }
        }));

        leftPadding.textProperty().addListener(new ChangeTargetListener(integer -> {
            CropModel cropModel = model.getCropModel();
            if(integer <= model.getImage().getWidth() - cropModel.getPadding()[2]){
                cropModel.setLeftPadding(integer);
                draw();
            }
        }));

        rightPadding.textProperty().addListener(new ChangeTargetListener(integer -> {
            CropModel cropModel = model.getCropModel();
            if(integer <= model.getImage().getWidth() - cropModel.getPadding()[1]){
                cropModel.setRightPadding(integer);
                draw();
            }
        }));

        cropButton.setOnAction(actionEvent -> {
            model.resizeImage();
            draw();
        });
    }

    private void prepare() {
        topPadding.setText("0");
        leftPadding.setText("0");
        rightPadding.setText("0");
        bottomPadding.setText("0");
        scaleSlider.valueProperty().setValue(1.0);
        scaleField.setText("1.00");
        gridToggle.setSelected(false);
        gridSizeComboBox.getItems().clear();
        gridSizeComboBox.getItems().addAll("20", "40");
        gridSizeComboBox.getSelectionModel().select(0);
    }

    private void init() {
        model.setGridStep(
                Double.parseDouble(gridSizeComboBox.getSelectionModel().getSelectedItem())
        );
    }

    private void draw() {
        canvas.setWidth(model.getWidth());
        canvas.setHeight(model.getHeight());
        canvas.getGraphicsContext2D().clearRect(0, 0, model.getHeight(), model.getWidth());
        model.draw(canvas.getGraphicsContext2D());
    }

    @FXML
    private void onScaleChange() {
    }
}
