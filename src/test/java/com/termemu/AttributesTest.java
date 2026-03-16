package com.termemu;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AttributesTest {

    @Test void defaultAttributesAreNullColor() {
        TerminalBuffer buf = new TerminalBuffer(10, 5, 10);
        CellAttributes attrs = buf.getAttributesAt(0, 0);
        assertNull(attrs.getFg());
        assertNull(attrs.getBg());
    }

    @Test void writtenCellHasCurrentAttributes() {
        TerminalBuffer buf = new TerminalBuffer(10, 5, 10);
        buf.setAttributes(1, 2, new TextStyle(true, false, false));
        buf.writeText("X");
        CellAttributes attrs = buf.getAttributesAt(0, 0);
        assertEquals(1, attrs.getFg());
        assertEquals(2, attrs.getBg());
        assertTrue(attrs.getStyle().isBold());
    }

    @Test void attributeChangeDoesNotAffectPreviouslyWrittenCells() {
        TerminalBuffer buf = new TerminalBuffer(10, 5, 10);
        buf.setAttributes(3, null, new TextStyle());
        buf.writeText("A");
        buf.setAttributes(7, null, new TextStyle());
        buf.writeText("B");
        assertEquals(3, buf.getAttributesAt(0, 0).getFg());
        assertEquals(7, buf.getAttributesAt(1, 0).getFg());
    }
}
