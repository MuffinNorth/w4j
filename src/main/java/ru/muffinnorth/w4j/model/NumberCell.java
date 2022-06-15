package ru.muffinnorth.w4j.model;

public class NumberCell{
    private int number;

    public Cell getCell() {
        return cell;
    }

    private Cell cell;

    public NumberCell(Cell cell, int number){
        this.cell = cell;
        this.number = number;
    }

    @Override
    public String toString() {
        return "n = " + number +
                ", " + cell;
    }
}
