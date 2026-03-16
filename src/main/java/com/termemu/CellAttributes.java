package com.termemu;

/**
 * Holds visual attributes for a cell.
 * fg/bg == null means "default terminal color".
 * Color values 0–15 map to the 16 standard ANSI terminal colors.
 */
public class CellAttributes {
    private Integer fg; // null = default
    private Integer bg; // null = default
    private TextStyle style;

    public CellAttributes() {
        this.style = new TextStyle();
    }

    public CellAttributes(Integer fg, Integer bg, TextStyle style) {
        this.fg = fg;
        this.bg = bg;
        this.style = style != null ? style : new TextStyle();
    }

    public Integer getFg() { return fg; }
    public Integer getBg() { return bg; }
    public TextStyle getStyle() { return style; }

    public void setFg(Integer fg) { this.fg = fg; }
    public void setBg(Integer bg) { this.bg = bg; }
    public void setStyle(TextStyle style) { this.style = style; }

    public CellAttributes copy() {
        return new CellAttributes(fg, bg, style.copy());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CellAttributes a)) return false;
        return java.util.Objects.equals(fg, a.fg)
                && java.util.Objects.equals(bg, a.bg)
                && style.equals(a.style);
    }
}
