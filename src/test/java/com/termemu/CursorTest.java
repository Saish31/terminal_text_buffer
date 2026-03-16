package com.termemu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CursorTest {
    private TerminalBuffer buf;

    @BeforeEach void setUp() { buf = new TerminalBuffer(80, 24, 500); }

    @Test void defaultCursorIsAtOrigin() {
        assertEquals(0, buf.getCursorCol());
        assertEquals(0, buf.getCursorRow());
    }

    @Test void setCursorMovesCorrectly() {
        buf.setCursor(10, 5);
        assertEquals(10, buf.getCursorCol());
        assertEquals(5,  buf.getCursorRow());
    }

    @Test void cursorClampsToRightEdge() {
        buf.setCursor(999, 0);
        assertEquals(79, buf.getCursorCol());
    }

    @Test void cursorClampsToBottomEdge() {
        buf.setCursor(0, 999);
        assertEquals(23, buf.getCursorRow());
    }

    @Test void cursorDoesNotGoNegative() {
        buf.moveCursorLeft(100);
        assertEquals(0, buf.getCursorCol());
        buf.moveCursorUp(100);
        assertEquals(0, buf.getCursorRow());
    }

    @Test void moveCursorRelative() {
        buf.setCursor(5, 5);
        buf.moveCursorRight(3);
        buf.moveCursorDown(2);
        assertEquals(8, buf.getCursorCol());
        assertEquals(7, buf.getCursorRow());
    }
}
