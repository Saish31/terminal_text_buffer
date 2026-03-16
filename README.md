# Terminal Text Buffer

A lightweight terminal text buffer implementation in Java — the core data structure
used by terminal emulators to store and manipulate displayed text.

When a shell sends output, a terminal emulator updates this buffer, and the UI renders it.

## Features

- **Fixed-size screen grid** — configurable width × height (default 80×24)
- **Scrollback history** — lines that scroll off the top are preserved, with a configurable max size
- **Per-cell attributes** — foreground/background color (16 ANSI colors, or default), plus bold, italic, underline style flags
- **Cursor management** — get/set cursor position, move in 4 directions, clamped to screen bounds
- **Write mode** — overwrite text at cursor position, moves cursor forward
- **Insert mode** — shift existing content right, wraps to next line on overflow
- **Fill line** — fill an entire row with a character, or clear it
- **Wide character support** — CJK ideographs and emoji occupy 2 columns, with placeholder cell handling
- **Resize** — change screen dimensions with content preservation (clips rather than reflows)

## Project Structure

src/  
├── main/java/com/termemu/  
│ ├── TextStyle.java # Style flags: bold, italic, underline  
│ ├── CellAttributes.java # Foreground/background color + TextStyle  
│ ├── Cell.java # Single character cell with attributes  
│ ├── Line.java # A row of cells  
│ └── TerminalBuffer.java # Core buffer: screen, scrollback, cursor, editing  
└── test/java/com/termemu/  
├── CursorTest.java # Cursor movement and boundary clamping  
├── EditingTest.java # Write, insert, fill, clear operations  
├── ScrollbackTest.java # Scrollback eviction, cap, absolute addressing  
├── AttributesTest.java # Cell attribute isolation and color assignment  
└── ContentAccessTest.java # Content access, wide chars, and resize  


## Build & Run Tests

**Requirements:** Java 17+, Maven 3.8+

```bash
mvn test
```

## Expected output:  
Tests run: 27, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS

## Design Decisions & Trade-offs  
Decision	Rationale
null for default fg/bg color	Clearly means "inherit from terminal theme"; avoids a DEFAULT enum sentinel that complicates equality checks
LinkedList for scrollback	O(1) removeFirst() on eviction and addLast() on append — ideal for a capped FIFO queue
ArrayList<Line> for screen	O(1) random row access by index; screen height is always small so remove(0) on scroll is negligible
CellAttributes copied on write	Each cell owns a snapshot of attributes at write time; later setAttributes() calls don't mutate already-written cells
Wide char placeholder cell	The second column of a wide character holds a placeholder, preventing other characters from overwriting half a glyph
Resize clips content	Matches xterm/iTerm2 behaviour — simpler than reflowing text, and avoids ambiguous cursor repositioning

## Potential Future Improvements  
ANSI escape code parser — a VtParser layer on top that interprets byte streams and drives TerminalBuffer


Proper wcwidth lookup — replace the manual Unicode range checks with a full East Asian Width table

Dirty region tracking — mark changed cells since last render to enable efficient partial UI redraws

Double-width / double-height lines — VT100 DECDHL/DECDWL sequence support


***
