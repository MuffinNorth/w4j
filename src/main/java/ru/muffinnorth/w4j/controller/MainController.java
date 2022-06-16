package ru.muffinnorth.w4j.controller;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jfree.chart.fx.ChartViewer;
import ru.muffinnorth.w4j.listeners.ChangeTargetListener;
import ru.muffinnorth.w4j.model.CanvasModel;
import ru.muffinnorth.w4j.model.CropModel;
import ru.muffinnorth.w4j.model.NumberCell;
import ru.muffinnorth.w4j.util.TextAreaOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;

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

    @FXML
    ListView<NumberCell> cellsView;

    @FXML
    TextArea out;

    @FXML
    Button applyNeuro;

    private CanvasModel model;

    @FXML
    private void onClickLoadButton() {
        ByteArrayOutputStream b = new TextAreaOutputStream(out);
        /*PrintStream out = new PrintStream(b);
        DualStream dualStream = new DualStream(System.out, out);
        System.setOut(dualStream);
        System.setErr(dualStream);*/
        System.out.println("Start");
        {
            Stage thisStage = (Stage) loadButton.getScene().getWindow();
            FileChooser filechooser = new FileChooser();
            filechooser.setTitle("Open File");
            File selected = filechooser.showOpenDialog(thisStage);
            if (selected == null) {
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
            if (integer <= model.getImage().getHeight() - cropModel.getPadding()[3]) {
                cropModel.setTopPadding(integer);
                draw();
            }
        }));

        bottomPadding.textProperty().addListener(new ChangeTargetListener(integer -> {
            CropModel cropModel = model.getCropModel();
            if (integer <= model.getImage().getHeight() - cropModel.getPadding()[0]) {
                cropModel.setBottomPadding(integer);
                draw();
            }
        }));

        leftPadding.textProperty().addListener(new ChangeTargetListener(integer -> {
            CropModel cropModel = model.getCropModel();
            if (integer <= model.getImage().getWidth() - cropModel.getPadding()[2]) {
                cropModel.setLeftPadding(integer);
                draw();
            }
        }));

        rightPadding.textProperty().addListener(new ChangeTargetListener(integer -> {
            CropModel cropModel = model.getCropModel();
            if (integer <= model.getImage().getWidth() - cropModel.getPadding()[1]) {
                cropModel.setRightPadding(integer);
                draw();
            }
        }));

        cropButton.setOnAction(actionEvent -> {
            model.resizeImage();
            prepare();
            init();
            draw();
        });

        findButton.setOnAction(NPAnalyzer.builder().model(model).outList(model.getCells()).callback(cells -> {
            cells.forEach(cell -> cellsView.getItems().add(new NumberCell(cell, cells.indexOf(cell))));
            cellsView.getSelectionModel().selectedItemProperty().addListener((observableValue, numberCell, t1) -> {
                System.out.println(t1);
                model.setSelectedCell(t1.getCell());
                draw();
            });
            draw();
        }).build().actionEvent());

        applyNeuro.setOnAction(Applyer.builder().model(model).build().actionEvent());
    }

    private void prepare() {
        cellsView.getItems().clear();
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

    @FXML
    private void saveProject(){
    }

    @FXML
    private void loadProject(){
    }
}
