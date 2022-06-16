package ru.muffinnorth.w4j.model;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import lombok.Getter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.statistics.HistogramDataset;

import java.util.ArrayList;
import java.util.Iterator;

public class CanvasModel {
    private Waffler waffler;
    private CropModel cropModel;
    private double scale = 1.0;
    private boolean isNeedGrid = false;
    @Getter private double gridStep = 0.2;
    private Point2D selectedCell;
    private GraphicsContext context;
    private double blockSize = 1200 * 0.39 * gridStep;
    private final ArrayList<Cell> cells = new ArrayList<>();
    private int cellsToDraw = 15;
    private double[] bounds;


    public CanvasModel(Image image){
        this.waffler = new Waffler(image);
        this.cropModel = CropModel.getBaseCropModel();
        bounds = new double[]{
                (image.getWidth() - (int)(image.getWidth() / blockSize) * blockSize) / 2,
                (image.getHeight() - (int)(image.getHeight() / blockSize) * blockSize) / 2
        };
    }

    public Image getImage(){
        return waffler.getCurrentImage();
    }

    public Image getOriginal(){
        return waffler.getOriginalImage();
    }

    public void resizeImage(){
        PixelReader reader = getImage().getPixelReader();
        WritableImage newImage = new WritableImage(reader,
                cropModel.getPadding()[0],
                cropModel.getPadding()[1],
                (int) (getImage().getWidth() - cropModel.getPadding()[2] - cropModel.getPadding()[1]),
                (int) (getImage().getHeight() - cropModel.getPadding()[3] - cropModel.getPadding()[0]));
        this.waffler = new Waffler(newImage);
        setGridStep(20);
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
        bounds[0] = (image.getWidth() - (int)(image.getWidth() / blockSize) * blockSize) / 2;
        bounds[1] = (image.getHeight() - (int)(image.getHeight() / blockSize) * blockSize) / 2;
    }

    public void setScale(double value){
        scale = value;
    }

    public double getScale(){
        return scale;
    }


    public void draw(GraphicsContext context){
        context.save();
        this.context = context;
        internalDraw();
        context.restore();
    }

    private void internalDraw(){
        context.scale(this.scale, this.scale);
        drawWafflerImage();
        if (isNeedGrid)
            drawCells();
        drawInteresting();
        if (selectedCell != null){
            drawSelected();
        }
        drawPaddings();
    }


    private void drawPaddings(){
        context.save();
        context.setLineWidth(10);
        context.setStroke(Color.RED);
        int[] paddings = cropModel.getPadding();
        context.strokeLine(paddings[1], paddings[0], getImage().getWidth() - paddings[2], paddings[0]);
        context.strokeLine(paddings[1], getImage().getHeight() - paddings[3], getImage().getWidth() - paddings[2], getImage().getHeight() - paddings[3]);
        context.strokeLine(paddings[1], paddings[0], paddings[1], getImage().getHeight() - paddings[3]);
        context.strokeLine(getImage().getWidth() - paddings[2], paddings[0], getImage().getWidth() - paddings[2], getImage().getHeight() - paddings[3]);
        context.restore();
    }

    private void drawWafflerImage(){
        context.drawImage(getImage(), 0, 0);
    }

    private void drawSelected(){
        context.save();
        Cell cell = Cell.builder().coordinate((int) selectedCell.getX(), (int) selectedCell.getY()).build();
        drawCell(cell, Color.ORANGE, 3);
        context.restore();
    }

    private void drawCell(Cell cell ,Color color, int width){
        context.setLineWidth(width);
        context.setStroke(color);
        drawRect( (int) cell.getCoordinate().getX(), (int) cell.getCoordinate().getY(), bounds, blockSize, 5);
    }

    //FIXME
    private void drawInteresting(){
        context.save();
        Iterator<Cell> iterator = cells.iterator();
        for (int i = 0; i < cellsToDraw; i++) {
            if(iterator.hasNext()){
                drawCell(iterator.next(), Color.RED, 5);
            }
        }
        context.restore();
    }

    private void drawCells(){
        Image image = getImage();
        context.setLineWidth(2);
        context.setStroke(Color.BLACK);
        for (int i = 0; i < (image.getWidth() / blockSize) - 1; i++) {
            for (int j = 0; j < (image.getHeight() / blockSize) - 1; j++) {
                drawRect(i, j, bounds, blockSize, 5);
            }
        }
    }

    private void drawRect(int x, int y, double[] bounds, double blockSize, double offset){
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

    public void setSelectedCell(Cell cell){
        selectedCell = cell.getCoordinate();
    }

    public void appendCell(Cell c){
        cells.add(c);
    }

    public JFreeChart createSelectedHist(){
        if(selectedCell == null){
            return null;
        }
        Image img = getSelectedImage();
        ArrayList<Double> grayscale = new ArrayList<>();
        PixelReader reader = img.getPixelReader();

        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                int gray = (int)(reader.getColor(i, j).grayscale().getRed() * 255);
                grayscale.add((double) gray);
            }
        }
        double[] data = grayscale.stream().mapToDouble(i -> i).toArray();
        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries("key", data, 10);
        JFreeChart chart = ChartFactory.createHistogram("Гистограмма оттенков", "Оттенки", "Кол-во", dataset);
        ValueAxis domain = chart.getXYPlot().getDomainAxis();
        domain.setRange(0, 255);
        chart.removeLegend();
        chart.clearSubtitles();
        return chart;
    }

    public CropModel getCropModel() {
        return cropModel;
    }


    public ArrayList<Cell> getCells() {
        return cells;
    }
}
