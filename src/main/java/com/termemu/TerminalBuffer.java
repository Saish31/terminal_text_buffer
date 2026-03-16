package com.termemu;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TerminalBuffer {

    // ── Dimensions ─────────────────────────────────────────────
    private int width;
    private int height;
    private final int maxScrollback;

    // ── Storage ─────────────────────────────────────────────────
    private final List<Line> screen;          // exactly `height` lines
    private final LinkedList<Line> scrollback; // oldest first, max maxScrollback

    // ── Cursor ───────────────────────────────────────────────────
    private int cursorCol;
    private int cursorRow;

    // ── Current attributes (used for writes) ─────────────────────
    private CellAttributes currentAttributes;

    // ────────────────────────────────────────────────────────────
    // Construction
    // ────────────────────────────────────────────────────────────

    public TerminalBuffer(int width, int height, int maxScrollback) {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Width and height must be > 0");
        if (maxScrollback < 0)         throw new IllegalArgumentException("maxScrollback must be >= 0");

        this.width = width;
        this.height = height;
        this.maxScrollback = maxScrollback;
        this.screen = new ArrayList<>(height);
        this.scrollback = new LinkedList<>();
        this.currentAttributes = new CellAttributes();

        for (int i = 0; i < height; i++) {
            screen.add(new Line(width));
        }
    }

    // ────────────────────────────────────────────────────────────
    // Attributes
    // ────────────────────────────────────────────────────────────

    public void setAttributes(Integer fg, Integer bg, TextStyle style) {
        currentAttributes = new CellAttributes(fg, bg, style != null ? style : new TextStyle());
    }

    public void setForeground(Integer fg) { currentAttributes.setFg(fg); }
    public void setBackground(Integer bg) { currentAttributes.setBg(bg); }
    public void setStyle(TextStyle style) { currentAttributes.setStyle(style); }
    public CellAttributes getCurrentAttributes() { return currentAttributes.copy(); }

    // ────────────────────────────────────────────────────────────
    // Cursor
    // ────────────────────────────────────────────────────────────

    public int getCursorCol() { return cursorCol; }
    public int getCursorRow() { return cursorRow; }

    public void setCursor(int col, int row) {
        cursorCol = clampCol(col);
        cursorRow = clampRow(row);
    }

    public void moveCursorUp(int n)    { cursorRow = clampRow(cursorRow - n); }
    public void moveCursorDown(int n)  { cursorRow = clampRow(cursorRow + n); }
    public void moveCursorLeft(int n)  { cursorCol = clampCol(cursorCol - n); }
    public void moveCursorRight(int n) { cursorCol = clampCol(cursorCol + n); }

    private int clampCol(int col) { return Math.max(0, Math.min(col, width - 1)); }
    private int clampRow(int row) { return Math.max(0, Math.min(row, height - 1)); }

    // ────────────────────────────────────────────────────────────
    // Editing — cursor-dependent
    // ────────────────────────────────────────────────────────────

    /**
     * Writes text at cursor, overwriting existing content.
     * Stops at the end of the line (no wrapping).
     * Moves cursor forward.
     */
    public void writeText(String text) {
        if (text == null || text.isEmpty()) return;
        Line line = screen.get(cursorRow);
        for (int i = 0; i < text.length() && cursorCol < width; ) {
            int cp = text.codePointAt(i);
            int colWidth = getCharColumnWidth(cp);
            char[] chars = Character.toChars(cp);

            if (colWidth == 2 && cursorCol + 1 < width) {
                // Wide character: write char in current cell, placeholder in next
                line.setCell(cursorCol, new Cell(chars[0], currentAttributes.copy()));
                Cell placeholder = Cell.blank();
                placeholder.setWidePlaceholder(true);
                placeholder.setAttributes(currentAttributes.copy());
                line.setCell(cursorCol + 1, placeholder);
                cursorCol += 2;
            } else if (colWidth == 1) {
                line.setCell(cursorCol, new Cell(chars[0], currentAttributes.copy()));
                cursorCol++;
            }
            // If wide char won't fit in last column, skip it
            i += Character.charCount(cp);
        }
    }

    /**
     * Inserts text at cursor, shifting existing content right.
     * Wraps to the next line if content overflows.
     * Moves cursor forward.
     */
    public void insertText(String text) {
        if (text == null || text.isEmpty()) return;

        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            int colWidth = getCharColumnWidth(cp);
            char ch = Character.toChars(cp)[0];

            Line line = screen.get(cursorRow);
            // Shift existing cells right from cursorCol
            for (int c = width - colWidth; c > cursorCol; c--) {
                line.setCell(c + colWidth - 1, line.getCell(c - 1).copy());
            }
            line.setCell(cursorCol, new Cell(ch, currentAttributes.copy()));
            if (colWidth == 2 && cursorCol + 1 < width) {
                Cell placeholder = Cell.blank();
                placeholder.setWidePlaceholder(true);
                line.setCell(cursorCol + 1, placeholder);
            }
            cursorCol += colWidth;

            // Wrap to next line
            if (cursorCol >= width) {
                cursorCol = 0;
                if (cursorRow < height - 1) {
                    cursorRow++;
                } else {
                    insertEmptyLineAtBottom(); // scroll
                }
            }
            i += Character.charCount(cp);
        }
    }

    /**
     * Fills the current cursor row entirely with the given character.
     * Pass '\0' to clear the line.
     */
    public void fillLine(char ch) {
        Line line = screen.get(cursorRow);
        for (int c = 0; c < width; c++) {
            if (ch == '\0') {
                line.setCell(c, Cell.blank());
            } else {
                line.setCell(c, new Cell(ch, currentAttributes.copy()));
            }
        }
    }

    // ────────────────────────────────────────────────────────────
    // Editing — cursor-independent
    // ────────────────────────────────────────────────────────────

    /**
     * Pushes the top screen line into scrollback, then appends a blank line
     * at the bottom. This is what happens when the terminal scrolls up.
     */
    public void insertEmptyLineAtBottom() {
        Line evicted = screen.remove(0);
        pushToScrollback(evicted);
        screen.add(new Line(width));
    }

    /** Clears the screen (fills all lines with blanks). Cursor stays. */
    public void clearScreen() {
        for (int r = 0; r < height; r++) {
            screen.set(r, new Line(width));
        }
    }

    /** Clears both screen and scrollback. Resets cursor. */
    public void clearAll() {
        clearScreen();
        scrollback.clear();
        cursorCol = 0;
        cursorRow = 0;
    }

    // ────────────────────────────────────────────────────────────
    // Content Access — screen
    // ────────────────────────────────────────────────────────────

    public char getCharAt(int col, int row) {
        return screen.get(clampRow(row)).getCell(clampCol(col)).getCharacter();
    }

    public CellAttributes getAttributesAt(int col, int row) {
        return screen.get(clampRow(row)).getCell(clampCol(col)).getAttributes().copy();
    }

    public String getScreenLine(int row) {
        return screen.get(clampRow(row)).toDisplayString();
    }

    public String getScreenContent() {
        StringBuilder sb = new StringBuilder();
        for (Line line : screen) {
            sb.append(line.toDisplayString()).append('\n');
        }
        return sb.toString();
    }

    // ────────────────────────────────────────────────────────────
    // Content Access — scrollback
    // ────────────────────────────────────────────────────────────

    /**
     * Scrollback rows are indexed from 0 (oldest) upward.
     * Screen rows continue after scrollback.
     * Total addressable rows = scrollback.size() + height.
     */
    public char getCharAtAbsolute(int col, int absoluteRow) {
        if (absoluteRow < scrollback.size()) {
            return scrollback.get(absoluteRow).getCell(clampCol(col)).getCharacter();
        }
        int screenRow = absoluteRow - scrollback.size();
        return getCharAt(col, screenRow);
    }

    public CellAttributes getAttributesAtAbsolute(int col, int absoluteRow) {
        if (absoluteRow < scrollback.size()) {
            return scrollback.get(absoluteRow).getCell(clampCol(col)).getAttributes().copy();
        }
        int screenRow = absoluteRow - scrollback.size();
        return getAttributesAt(col, screenRow);
    }

    public String getScrollbackLine(int scrollbackRow) {
        if (scrollbackRow < 0 || scrollbackRow >= scrollback.size())
            throw new IndexOutOfBoundsException("scrollbackRow=" + scrollbackRow);
        return scrollback.get(scrollbackRow).toDisplayString();
    }

    public String getFullContent() {
        StringBuilder sb = new StringBuilder();
        for (Line line : scrollback) {
            sb.append(line.toDisplayString()).append('\n');
        }
        sb.append(getScreenContent());
        return sb.toString();
    }

    public int getScrollbackSize() { return scrollback.size(); }
    public int getWidth()          { return width; }
    public int getHeight()         { return height; }

    // ────────────────────────────────────────────────────────────
    // Bonus: Resize
    // ────────────────────────────────────────────────────────────

    /**
     * Resize strategy: preserve content by truncating or padding each line.
     * If height grows, new blank lines are added at the bottom.
     * If height shrinks, excess lines at the bottom are dropped.
     * Cursor is clamped to new bounds.
     */
    public void resize(int newWidth, int newHeight) {
        if (newWidth <= 0 || newHeight <= 0)
            throw new IllegalArgumentException("Dimensions must be > 0");

        // Adjust each existing screen line's width
        for (int r = 0; r < screen.size(); r++) {
            screen.set(r, resizeLine(screen.get(r), newWidth));
        }

        // Adjust height
        while (screen.size() < newHeight) screen.add(new Line(newWidth));
        while (screen.size() > newHeight) screen.remove(screen.size() - 1);

        // Resize scrollback lines too
        for (int r = 0; r < scrollback.size(); r++) {
            scrollback.set(r, resizeLine(scrollback.get(r), newWidth));
        }

        this.width = newWidth;
        this.height = newHeight;
        this.cursorCol = clampCol(cursorCol);
        this.cursorRow = clampRow(cursorRow);
    }

    // ────────────────────────────────────────────────────────────
    // Private helpers
    // ────────────────────────────────────────────────────────────

    private void pushToScrollback(Line line) {
        if (maxScrollback == 0) return;
        if (scrollback.size() >= maxScrollback) {
            scrollback.removeFirst(); // evict oldest
        }
        scrollback.addLast(line);
    }

    private Line resizeLine(Line original, int newWidth) {
        Line newLine = new Line(newWidth);
        int copyWidth = Math.min(original.getWidth(), newWidth);
        for (int c = 0; c < copyWidth; c++) {
            newLine.setCell(c, original.getCell(c).copy());
        }
        return newLine;
    }

    /**
     * Returns the column width of a code point.
     * CJK and emoji ranges return 2; everything else returns 1.
     * This is a simplified version of wcwidth.
     */
    private int getCharColumnWidth(int cp) {
        // Common wide ranges: CJK Unified Ideographs, Fullwidth Forms, common emoji
        if ((cp >= 0x1100 && cp <= 0x115F)   // Hangul Jamo
                || (cp >= 0x2E80 && cp <= 0x303E)   // CJK Radicals
                || (cp >= 0x3041 && cp <= 0x33BF)   // Japanese
                || (cp >= 0x33FF && cp <= 0xA4CF)   // CJK Unified
                || (cp >= 0xA960 && cp <= 0xA97F)   // Hangul
                || (cp >= 0xAC00 && cp <= 0xD7FF)   // Hangul Syllables
                || (cp >= 0xF900 && cp <= 0xFAFF)   // CJK Compatibility
                || (cp >= 0xFE10 && cp <= 0xFE1F)   // Vertical
                || (cp >= 0xFE30 && cp <= 0xFE6F)   // CJK Compatibility Forms
                || (cp >= 0xFF01 && cp <= 0xFF60)   // Fullwidth Latin
                || (cp >= 0xFFE0 && cp <= 0xFFE6)   // Fullwidth Signs
                || (cp >= 0x1F300 && cp <= 0x1FAFF) // Emoji
                || (cp >= 0x20000 && cp <= 0x2FFFD) // CJK Extension B
                || (cp >= 0x30000 && cp <= 0x3FFFD)) {
            return 2;
        }
        return 1;
    }
}
