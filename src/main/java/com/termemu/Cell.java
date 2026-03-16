package com.termemu;

public class Cell {
    public static final char EMPTY = '\0';

    private char character;
    private CellAttributes attributes;
    private boolean isWidePlaceholder; // true for the 2nd cell of a wide char

    public Cell() {
        this.character = EMPTY;
        this.attributes = new CellAttributes();
    }

    public Cell(char character, CellAttributes attributes) {
        this.character = character;
        this.attributes = attributes;
    }

    public char getCharacter()          { return character; }
    public CellAttributes getAttributes() { return attributes; }
    public boolean isWidePlaceholder()  { return isWidePlaceholder; }

    public void setCharacter(char c)              { this.character = c; }
    public void setAttributes(CellAttributes a)   { this.attributes = a; }
    public void setWidePlaceholder(boolean val)   { this.isWidePlaceholder = val; }

    public boolean isEmpty() { return character == EMPTY && !isWidePlaceholder; }

    public Cell copy() {
        Cell c = new Cell(character, attributes.copy());
        c.isWidePlaceholder = this.isWidePlaceholder;
        return c;
    }

    /** Returns a blank empty cell */
    public static Cell blank() { return new Cell(); }
}
