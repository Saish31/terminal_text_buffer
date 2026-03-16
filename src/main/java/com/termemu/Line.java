package com.termemu;

import java.util.ArrayList;
import java.util.List;

public class Line {
    private final List<Cell> cells;
    private final int width;

    public Line(int width) {
        this.width = width;
        this.cells = new ArrayList<>(width);
        for (int i = 0; i < width; i++) {
            cells.add(Cell.blank());
        }
    }

    public Cell getCell(int col) {
        if (col < 0 || col >= width) throw new IndexOutOfBoundsException("col=" + col);
        return cells.get(col);
    }

    public void setCell(int col, Cell cell) {
        if (col < 0 || col >= width) return;
        cells.set(col, cell);
    }

    public int getWidth() { return width; }

    /** Returns visible string, replacing null/empty chars with space */
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder(width);
        for (Cell cell : cells) {
            if (cell.isWidePlaceholder()) continue; // skip wide placeholders
            char c = cell.getCharacter();
            sb.append(c == Cell.EMPTY ? ' ' : c);
        }
        return sb.toString();
    }

    public Line copy() {
        Line copy = new Line(width);
        for (int i = 0; i < width; i++) {
            copy.setCell(i, cells.get(i).copy());
        }
        return copy;
    }
}
