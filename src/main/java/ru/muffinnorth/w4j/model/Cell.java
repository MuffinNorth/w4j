package ru.muffinnorth.w4j.model;

import javafx.geometry.Point2D;
import lombok.Getter;

public class Cell implements Comparable<Cell>{

    private Point2D coordinate;
    private int clusterCount;

    private Cell(){}

    public static class Builder{
        private Cell cell;

        private Builder(){ cell = new Cell();}

        public Builder clusterCount(int n){
            cell.clusterCount = n;
            return this;
        }

        public Builder coordinate(int x, int y){
            cell.coordinate = new Point2D(x, y);
            return this;
        }

        public Cell build(){
            return cell;
        }
    }

    public static Builder builder(){
        return new Builder();
    }

    @Override
    public int compareTo(Cell other) {
        return Integer.compare(other.clusterCount, this.clusterCount);
    }

    public Point2D getCoordinate() {
        return coordinate;
    }

    @Override
    public String toString() {
        return "c = " + coordinate.getX() + ":" + coordinate.getY() +
                ", l =" + clusterCount;
    }
}
