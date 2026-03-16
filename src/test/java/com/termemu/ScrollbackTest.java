package com.termemu;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ScrollbackTest {

    @Test void scrollbackCapIsRespected() {
        TerminalBuffer buf = new TerminalBuffer(10, 3, 2);
        buf.insertEmptyLineAtBottom(); // scrollback: 1
        buf.insertEmptyLineAtBottom(); // scrollback: 2
        buf.insertEmptyLineAtBottom(); // scrollback should evict oldest → still 2
        assertEquals(2, buf.getScrollbackSize());
    }

    @Test void scrollbackZeroDiscardsLines() {
        TerminalBuffer buf = new TerminalBuffer(10, 3, 0);
        buf.insertEmptyLineAtBottom();
        assertEquals(0, buf.getScrollbackSize());
    }

    @Test void scrollbackLinesAreReadOnly_contentPreserved() {
        TerminalBuffer buf = new TerminalBuffer(10, 3, 10);
        buf.setCursor(0, 0);
        buf.writeText("OldLine");
        buf.insertEmptyLineAtBottom();
        assertEquals("OldLine   ", buf.getScrollbackLine(0));
    }

    @Test void absoluteRowAddressingSpansScrollbackAndScreen() {
        TerminalBuffer buf = new TerminalBuffer(10, 3, 10);
        buf.setCursor(0, 0);
        buf.writeText("A");
        buf.insertEmptyLineAtBottom(); // "A" goes to scrollback row 0
        buf.setCursor(0, 0);
        buf.writeText("B");           // "B" is now screen row 0 = absolute row 1

        assertEquals('A', buf.getCharAtAbsolute(0, 0));
        assertEquals('B', buf.getCharAtAbsolute(0, 1));
    }

    @Test void getFullContentIncludesScrollback() {
        TerminalBuffer buf = new TerminalBuffer(10, 2, 10);
        buf.setCursor(0, 0);
        buf.writeText("Line0");
        buf.insertEmptyLineAtBottom();
        buf.setCursor(0, 0);
        buf.writeText("Line1");
        String full = buf.getFullContent();
        assertTrue(full.contains("Line0"));
        assertTrue(full.contains("Line1"));
    }
}
