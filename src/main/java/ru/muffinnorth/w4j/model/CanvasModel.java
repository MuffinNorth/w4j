package ru.muffinnorth.w4j.model;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class CanvasModel {
    private Waffler waffler;
    private double scale = 1.0;
    private boolean isNeedGrid = false;
    private double gridStep = 0.2;
    private Point2D selectedCell;
    private double blockSize = 1200 * 0.39 * gridStep;
    private double[] bounds;


    public CanvasModel(Image image){
        this.waffler = new Waffler(image);
        bounds = new double[]{
                (image.getWidth() - (int)((image.getWidth() / blockSize))* blockSize ) / 2,
                (image.getHeight() - (int)((image.getHeight() / blockSize))* blockSize) / 2
        };
    }

    public Image getImage(){
        return waffler.getCurrentImage();
    }

    public double getWidth(){
        return waffler.getCurrentImage().getWidth()*scale;
    }

    public double getHeight(){
        return waffler.getCurrentImage().getHeight()*scale;
    }

    public void setNeedGrid(boolean b){
        this.isNeedGrid = b;
    }

    public void setGridStep(double mm){
        gridStep = mm / 100;
        blockSize = 1200 * 0.39 * gridStep;
        Image image = getImage();
        bounds[0] = (image.getWidth() - (int)((image.getWidth() / blockSize))* blockSize ) / 2;
        bounds[1] = (image.getHeight() - (int)((image.getHeight() / blockSize))* blockSize) / 2;
    }

    public void setScale(double value){
        scale = value;
    }

    public double getScale(){
        return scale;
    }

    public void draw(GraphicsContext context){
        context.save();
        context.scale(this.scale, this.scale);
        context.drawImage(getImage(), 0, 0);
        if (isNeedGrid)
            drawCells(context);
        if (selectedCell != null){
            drawSelected(context);
        }
        context.restore();
    }

    private void drawSelected(GraphicsContext context){
        context.save();
        context.setLineWidth(3);
        context.setStroke(Color.RED);
        drawRect(context, (int) selectedCell.getX(), (int) selectedCell.getY(), bounds, blockSize, 5);
        context.restore();
    }

    private void drawCells(GraphicsContext context){
        Image image = getImage();
        context.setLineWidth(2);
        context.setStroke(Color.BLACK);
        for (int i = 0; i < (image.getWidth() / blockSize) - 1; i++) {
            for (int j = 0; j < (image.getHeight() / blockSize) - 1; j++) {
                drawRect(context, i, j, bounds, blockSize, 5);
            }
        }
    }

    private void drawRect(GraphicsContext context, int x, int y, double[] bounds, double blockSize, double offset){
        context.strokeRect(  bounds[0] + x*blockSize + offset, bounds[1] + y*blockSize + offset, blockSize - offset, blockSize - offset);
    }

    public EventHandler<? super MouseEvent> getCoordinateListener(Runnable callback){
        return mouseEvent -> {
            int x = Math.abs((int) ( (bounds[0] - mouseEvent.getX() / scale) / blockSize ));
            int y = Math.abs((int) ( (bounds[1] - mouseEvent.getY() / scale) / blockSize ));
            selectedCell = new Point2D(x, y);
            if(callback != null){
                callback.run();
            }
        };
    }

    public WritableImage getSelectedImage(){
        PixelReader reader = getImage().getPixelReader();
        return new WritableImage(reader,
                (int)(bounds[0] + selectedCell.getX() * blockSize),
                (int)(bounds[1] + selectedCell.getY() * blockSize),
                (int)Math.round(blockSize),
                (int)Math.round(blockSize)
        );
    }

    public Point2D getSelectedCell() {
        return selectedCell;
    }

}
