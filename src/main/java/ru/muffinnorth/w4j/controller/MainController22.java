package ru.muffinnorth.w4j.controller;

import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.muffinnorth.w4j.model.CanvasModel;

import java.io.File;
import java.util.ArrayList;

public class MainController22 {

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

    Image image;

    int jCanvas;
    int iCanvas;

    ArrayList<Point2D> interestingList = new ArrayList<>();



    private CanvasModel model;

    @FXML
    private void onClickLoadButton(){
        //Open image
        Stage thisStage = (Stage) loadButton.getScene().getWindow();
        FileChooser filechooser = new FileChooser();
        filechooser.setTitle("Open File");
        File selected = filechooser.showOpenDialog(thisStage);
        image = new Image(selected.getAbsolutePath());
        //Create model
        model = new CanvasModel(image);




        gridSizeComboBox.getItems().add("20");
        gridSizeComboBox.getSelectionModel().select(0);
        gridSizeComboBox.getItems().add("40");

        gridSizeComboBox.getSelectionModel().selectedItemProperty().addListener((observableValue, integer, t1) -> {
            drawImage();
        });



        canvas.setOnScroll(scrollEvent -> {
            if(scrollEvent.isControlDown()){
                System.out.println("Hello");
                if(scrollEvent.getDeltaY() < 0){
                    if(scaleSlider.getValue() >= 0.05)
                        scaleSlider.valueProperty().setValue(scaleSlider.getValue() - 0.01);
                }else {
                    scaleSlider.valueProperty().setValue(scaleSlider.getValue() + 0.01);
                }
            }
        });




        scaleSlider.valueProperty().addListener((observableValue, number, t1) -> {
            scaleField.setText(number.toString());
            drawImage();
        });

        findButton.setOnAction(actionEvent -> {
            interestingList.add(new Point2D(0,0));
        });

        drawImage();
    }

    @FXML
    private void onScaleChange(){
        drawImage();
    }

    @FXML
    private void drawImage(){
        canvas.getGraphicsContext2D().save();

        Double scale = scaleSlider.getValue();
        canvas.setHeight(image.getHeight()*scale);
        canvas.setWidth(image.getWidth()*scale);
        canvas.getGraphicsContext2D().scale(scale,scale);
        canvas.getGraphicsContext2D().drawImage(image, 0, 0);
        Double grid = Double.parseDouble(gridSizeComboBox.getSelectionModel().getSelectedItem()) / 100;
        Double blockSize = 1200 * 0.39 * grid;
        Double boundsX = (image.getWidth() - (int)((image.getWidth() / blockSize))*blockSize ) / 2;
        Double boundsY = (image.getHeight() - (int)((image.getHeight() / blockSize))*blockSize) / 2;

        canvas.setOnMouseClicked(mouseEvent -> {
            iCanvas = Math.abs((int) ( (boundsX - mouseEvent.getX() / scale) / blockSize ));
            jCanvas = Math.abs((int) ( (boundsY - mouseEvent.getY() / scale) / blockSize ));
            drawImage();
            PixelReader reader = image.getPixelReader();
            WritableImage newImage = new WritableImage(reader, (int)(boundsX + iCanvas * blockSize),
                    (int)(boundsY + jCanvas * blockSize), (int)Math.round(blockSize), (int)Math.round(blockSize));
            cellView.setImage(newImage);
        });

        canvas.getGraphicsContext2D().setLineWidth(2);
        canvas.getGraphicsContext2D().setStroke(Color.BLACK);
        if(gridToggle.isSelected()){
            for (int i = 0; i < (image.getWidth() / blockSize) - 1; i++) {
                for (int j = 0; j < (image.getHeight() / blockSize) - 1; j++) {
                    canvas.getGraphicsContext2D().setStroke(Color.BLACK);
                    if(i == iCanvas && j == jCanvas)
                        canvas.getGraphicsContext2D().setStroke(Color.ORANGE);
                    canvas.getGraphicsContext2D().strokeRect(  boundsX + i*blockSize + 5, boundsY + j*blockSize + 5, blockSize - 5, blockSize - 5);
                }
            }
            interestingList.forEach(point2D -> {
                System.out.println(point2D);
                canvas.getGraphicsContext2D().setStroke(Color.RED);
                canvas.getGraphicsContext2D().strokeRect(  boundsX + point2D.getX()*blockSize + 5, boundsY + point2D.getY()*blockSize + 5, blockSize - 5, blockSize - 5);
            });
        }
        canvas.getGraphicsContext2D().restore();
    }
}
