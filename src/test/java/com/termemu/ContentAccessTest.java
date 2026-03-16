package com.termemu;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ContentAccessTest {

    @Test void getScreenLineReturnsCorrectWidth() {
        TerminalBuffer buf = new TerminalBuffer(10, 5, 10);
        assertEquals(10, buf.getScreenLine(0).length());
    }

    @Test void getScreenContentHasCorrectLineCount() {
        TerminalBuffer buf = new TerminalBuffer(10, 5, 10);
        String content = buf.getScreenContent();
        assertEquals(5, content.split("\n", -1).length - 1); // trailing newline
    }

    @Test void wideCharacterOccupiesTwoCols() {
        TerminalBuffer buf = new TerminalBuffer(10, 5, 10);
        buf.writeText("中"); // CJK wide char
        assertEquals(2, buf.getCursorCol()); // cursor advanced by 2
    }

    @org.junit.Test
    public void resizePreservesContent() {
        TerminalBuffer buf = new TerminalBuffer(10, 5, 10);
        buf.writeText("Hello");
        buf.resize(20, 5);
        assertEquals('H', buf.getCharAt(0, 0));
        assertEquals(20, buf.getWidth());
    }

    @Test void resizeShrinksTruncatesColumns() {
        TerminalBuffer buf = new TerminalBuffer(10, 5, 10);
        buf.writeText("Hello");
        buf.resize(3, 5);
        assertEquals(3, buf.getScreenLine(0).length());
    }
}
