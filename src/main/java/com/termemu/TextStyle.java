package com.termemu;

public class TextStyle {
    private boolean bold;
    private boolean italic;
    private boolean underline;

    public TextStyle() {}

    public TextStyle(boolean bold, boolean italic, boolean underline) {
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
    }

    public boolean isBold()      { return bold; }
    public boolean isItalic()    { return italic; }
    public boolean isUnderline() { return underline; }

    public void setBold(boolean bold)           { this.bold = bold; }
    public void setItalic(boolean italic)       { this.italic = italic; }
    public void setUnderline(boolean underline) { this.underline = underline; }

    public TextStyle copy() {
        return new TextStyle(bold, italic, underline);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TextStyle s)) return false;
        return bold == s.bold && italic == s.italic && underline == s.underline;
    }
}
