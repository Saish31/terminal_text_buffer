package com.termemu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EditingTest {
    private TerminalBuffer buf;

    @BeforeEach void setUp() { buf = new TerminalBuffer(10, 5, 100); }

    @Test void writeTextAtCursor() {
        buf.setCursor(0, 0);
        buf.writeText("Hello");
        assertEquals('H', buf.getCharAt(0, 0));
        assertEquals('e', buf.getCharAt(1, 0));
        assertEquals(5,   buf.getCursorCol());
    }

    @Test void writeTextOverwritesExisting() {
        buf.writeText("Hello");
        buf.setCursor(0, 0);
        buf.writeText("World");
        assertEquals("World     ", buf.getScreenLine(0));
    }

    @Test void writeTextStopsAtLineEnd() {
        buf.setCursor(8, 0);
        buf.writeText("ABCDE"); // only 2 chars fit
        assertEquals('A', buf.getCharAt(8, 0));
        assertEquals('B', buf.getCharAt(9, 0));
        assertEquals(10, buf.getCursorCol()); // clamped to width
    }

    @Test void insertTextShiftsCells() {
        buf.writeText("ABCDE");
        buf.setCursor(2, 0);
        buf.insertText("X");
        assertEquals('A', buf.getCharAt(0, 0));
        assertEquals('B', buf.getCharAt(1, 0));
        assertEquals('X', buf.getCharAt(2, 0));
        assertEquals('C', buf.getCharAt(3, 0));
    }

    @Test void fillLineWithChar() {
        buf.setCursor(0, 1);
        buf.fillLine('-');
        assertEquals("----------", buf.getScreenLine(1));
    }

    @Test void fillLineWithEmpty() {
        buf.writeText("AAAA");
        buf.setCursor(0, 0);
        buf.fillLine('\0');
        assertEquals("          ", buf.getScreenLine(0));
    }

    @Test void insertEmptyLineScrollsTopToScrollback() {
        buf.setCursor(0, 0);
        buf.writeText("FirstLine");
        buf.insertEmptyLineAtBottom();
        assertEquals(1, buf.getScrollbackSize());
        assertEquals("FirstLine ", buf.getScrollbackLine(0));
    }

    @Test void clearScreenEmptiesAllLines() {
        buf.writeText("Data");
        buf.clearScreen();
        assertEquals("          ", buf.getScreenLine(0));
    }

    @Test void clearAllResetsScrollback() {
        buf.insertEmptyLineAtBottom();
        buf.insertEmptyLineAtBottom();
        buf.clearAll();
        assertEquals(0, buf.getScrollbackSize());
        assertEquals(0, buf.getCursorCol());
    }
}
