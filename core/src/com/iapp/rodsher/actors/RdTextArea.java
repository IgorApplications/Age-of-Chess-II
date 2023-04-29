package com.iapp.rodsher.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.LongArray;
import com.badlogic.gdx.utils.Null;
import com.github.tommyettinger.textra.Font;

/** A text input field with multiple lines. */
public class RdTextArea extends RdTextField {

    /** Array storing lines breaks positions **/
    private IntArray linesBreak;

    /** Last text processed. This attribute is used to avoid unnecessary computations while calculating offsets **/
    private String lastText;

    /** Current line for the cursor **/
    private int cursorLine;

    /** Number of lines showed by the text area **/
    private int linesShowing;

    /** Variable to maintain the x offset of the cursor when moving up and down. If it's set to -1, the offset is reset **/
    private float moveOffset;

    /** minimum number of lines */
    private int minLines = 1;

    /** maximum number of lines */
    private int maxLines = Integer.MAX_VALUE;

    /** blocks character input */
    private boolean blockedTyping;

    public RdTextArea(String text, Skin skin) {
        super(text, skin);
    }

    public RdTextArea(String text, Skin skin, String styleName) {
        super(text, skin, styleName);
    }

    public RdTextArea(String text, RdTextFieldStyle style) {
        super(text, style);
    }

    public RdTextArea(String text) {
        super(text);
    }

    public RdTextArea(String text, String styleName) {
        super(text, styleName);
    }

    /** sets the minimum number of lines */
    public void setMinLines(int minLines) {
        this.minLines = minLines;
    }

    /** returns the minimum number of lines */
    public int getMinLines() {
        return minLines;
    }

    /** sets the maximum number of lines */
    public void setMaxLines(int maxLines) {
        this.maxLines = maxLines;
    }

    /** returns the maximum number of lines */
    public int getMaxLines() {
        return maxLines;
    }

    /** sets the minimum and maximum number of rows */
    public void setPrefLines(int prefLines) {
        minLines = prefLines;
        maxLines = prefLines;
    }

    public float getPrefHeight () {
        float prefHeight = linesShowing * getLineHeight();

        if (style.background != null) {
            prefHeight = Math.max(prefHeight + style.background.getBottomHeight() + style.background.getTopHeight(),
                    style.background.getMinHeight());
        }

        return prefHeight;
    }

    /** Returns total number of lines that the text occupies **/
    public int getLines () {
        return linesBreak.size / 2 + (newLineAtEnd() ? 1 : 0);
    }

    /** Returns if there's a new line at then end of the text **/
    public boolean newLineAtEnd () {
        return text.length() != 0
                && (text.charAt(text.length() - 1) == NEWLINE || text.charAt(text.length() - 1) == CARRIAGE_RETURN);
    }

    /** Moves the cursor to the given number line **/
    public void moveCursorLine (int line) {
        if (line < 0) {
            cursorLine = 0;
            cursor = 0;
            moveOffset = -1;
        } else if (line >= getLines()) {
            int newLine = getLines() - 1;
            cursor = text.length();
            if (line > getLines() || newLine == cursorLine) {
                moveOffset = -1;
            }
            cursorLine = newLine;
        } else if (line != cursorLine) {
            if (moveOffset < 0) {
                moveOffset = linesBreak.size <= cursorLine * 2 ? 0
                        : glyphPositions.get(cursor) - glyphPositions.get(linesBreak.get(cursorLine * 2));
            }
            cursorLine = line;
            cursor = cursorLine * 2 >= linesBreak.size ? text.length() : linesBreak.get(cursorLine * 2);
            while (cursor < text.length() && cursor <= linesBreak.get(cursorLine * 2 + 1) - 1
                    && glyphPositions.get(cursor) - glyphPositions.get(linesBreak.get(cursorLine * 2)) < moveOffset) {
                cursor++;
            }
            showCursor();
        }
    }

    public int getCursorLine () {
        return cursorLine;
    }

    public int getLinesShowing () {
        return linesShowing;
    }

    public float getCursorX () {
        float textOffset = 0;

        if (!(cursor >= glyphPositions.size || cursorLine * 2 >= linesBreak.size)) {
            int lineStart = linesBreak.items[cursorLine * 2];
            float glyphOffset = 0;

            // TODO
            //BitmapFont.Glyph lineFirst = fontData.getGlyph(displayText.charAt(lineStart));
            //if (lineFirst != null) {
            // BitmapFontData.getGlyphs()#852
            //  glyphOffset = lineFirst.fixedWidth ? 0 : -lineFirst.xoffset * fontData.scaleX - fontData.padLeft;
            //}

            textOffset = glyphPositions.get(cursor) - glyphPositions.get(lineStart) + glyphOffset;

        }

        return textOffset;
    }

    public float getCursorY () {
        return -(cursorLine) * getLineHeight();
    }

    protected void initialize () {
        super.initialize();
        writeEnters = true;
        linesBreak = new IntArray();
        cursorLine = 0;
        moveOffset = -1;
        linesShowing = 0;
    }

    protected int letterUnderCursor (float x) {
        if (linesBreak.size > 0) {
            if (cursorLine * 2 >= linesBreak.size) {
                return text.length();
            } else {
                float[] glyphPositions = this.glyphPositions.items;
                int start = linesBreak.items[cursorLine * 2];
                x += glyphPositions[start];
                int end = linesBreak.items[cursorLine * 2 + 1];
                int i = start;
                for (; i < end; i++)
                    if (glyphPositions[i] > x) break;
                if (i > 0 && glyphPositions[i] - x <= x - glyphPositions[i - 1]) return i;
                return Math.max(0, i - 1);
            }
        } else {
            return 0;
        }
    }

    // OVERRIDE from TextField
    protected void sizeChanged () {
        //lastText = null; // Cause calculateOffsets to recalculate the line breaks.

        // The number of lines showed must be updated whenever the height is updated
        //Font font = style.font;
        //Drawable background = style.background;
        //float availableHeight = getHeight() - (background == null ? 0
        //      : background.getBottomHeight() + background.getTopHeight());
        // font.getLineHeight()
        // (int)Math.floor(availableHeight / layout.getWorkingLayout().getLine(0).height)
        // / layout.getWorkingLayout().lines()
        // TODO

        //linesShowing
        var goalLines = (int) Math.floor(linesBreak.size * 0.5f);
        if (goalLines < minLines) goalLines = minLines;
        if (goalLines > maxLines) goalLines = maxLines;
        linesShowing = goalLines;

        var layout = label.getWorkingLayout();
        if (layout.getLine(layout.lines() - 1)
                .glyphs.isEmpty() && layout.lines() >= minLines) {
            //linesShowing++;
        }

        super.sizeChanged();
    }

    protected float getTextY (Font font, @Null Drawable background) {
        float textY = getHeight();
        if (background != null) {
            textY = textY - background.getTopHeight();
        }

        if (style.font.integerPosition) textY = (int)textY;
        return textY + getLineHeight() * 0.5f;
    }

    protected void drawSelection (Drawable selection, Batch batch, Font font, float x, float y) {
        int i = 0;
        float offsetY = 0;
        int minIndex = Math.min(cursor, selectionStart);
        int maxIndex = Math.max(cursor, selectionStart);

        while (i + 1 < linesBreak.size && i < linesShowing * 2) {

            int lineStart = linesBreak.get(i);
            int lineEnd = linesBreak.get(i + 1);

            if (!((minIndex < lineStart && minIndex < lineEnd && maxIndex < lineStart && maxIndex < lineEnd)
                    || (minIndex > lineStart && minIndex > lineEnd && maxIndex > lineStart && maxIndex > lineEnd))) {

                int start = Math.max(lineStart, minIndex);
                int end = Math.min(lineEnd, maxIndex);

                float fontLineOffsetX = 0;
                float fontLineOffsetWidth = 0;

                // we can't use fontOffset as it is valid only for first glyph/line in the text
                // we will grab first character in this line and calculate proper offset for this line

                // TODO
                //BitmapFont.Glyph lineFirst = fontData.getGlyph(displayText.charAt(lineStart));
                //if (lineFirst != null) {
                // see BitmapFontData.getGlyphs()#852 for offset calculation
                // if selection starts when line starts we want to offset width instead of moving the start as it looks better
                //if (start == lineStart) {
                //fontLineOffsetWidth = lineFirst.fixedWidth ? 0 : -lineFirst.xoffset * fontData.scaleX - fontData.padLeft;
                //} else {
                //fontLineOffsetX = lineFirst.fixedWidth ? 0 : -lineFirst.xoffset * fontData.scaleX - fontData.padLeft;
                //}
                //}

                float selectionX = glyphPositions.get(start) - glyphPositions.get(lineStart);
                float selectionWidth = glyphPositions.get(end) - glyphPositions.get(start);
                selection.draw(batch, x + selectionX + fontLineOffsetX, y - offsetY,
                        selectionWidth + fontLineOffsetWidth, getLineHeight());
            }

            offsetY += getLineHeight();
            i += 2;
        }
    }

    protected void drawText (Batch batch, Font font, float x, float y, float maxWidth) {
        var starts = new IntArray();
        var ends = new IntArray();

        for (int i = 0; i < (linesShowing) * 2 && i < linesBreak.size; i += 2) {
            starts.add(linesBreak.items[i]);
            ends.add(linesBreak.items[i + 1]);
        }

        label.setText(displayText.toString());
        cutLabel(starts, ends);

        label.setPosition(x, y);
        label.setAlignment(Align.topLeft);

        label.act(Gdx.graphics.getDeltaTime());
        label.draw(batch, getColor().a);

        invalidateHierarchy();
        sizeChanged();
    }

    protected void drawCursor (Drawable cursorPatch, Batch batch, Font font, float x, float y) {
        cursorPatch.draw(batch, x + getCursorX(), y + getCursorY(),
                cursorPatch.getMinWidth(), textHeight);
    }

    protected void calculateOffsets () {
        super.calculateOffsets();

        int end = 0;
        if (!this.text.equals(lastText)) {
            this.lastText = text;

            float maxWidthLine = this.getWidth()
                    - (style.background != null ? style.background.getLeftWidth()
                    + style.background.getRightWidth() : 0);

            linesBreak.clear();
            int lineStart = 0;
            int lastSpace = 0;
            int last = 0;
            char lastCharacter;

            for (int i = 0; i < text.length(); i++) {

                lastCharacter = text.charAt(i);
                if (lastCharacter == CARRIAGE_RETURN || lastCharacter == NEWLINE) {

                    linesBreak.add(lineStart);
                    linesBreak.add(i);
                    lineStart = i + 1;
                    end++;
                    sizeChanged();

                } else {
                    lastSpace = (continueCursor(i, 0) ? lastSpace : i);

                    //style.font.markup(text, label.layout.clear());
                    label.setText(text.subSequence(lineStart, i + 1).toString());
                    label.layout();

                    if (label.getWorkingLayout().getWidth() > maxWidthLine) {

                        if (lineStart >= lastSpace) {
                            lastSpace = i - 1;
                        }
                        last = i;

                        linesBreak.add(lineStart);
                        linesBreak.add(lastSpace + 1);

                        lineStart = lastSpace + 1;
                        lastSpace = lineStart;
                        end++;

                    }
                    sizeChanged();

                }
            }

            // Add last line
            if (lineStart < text.length()) {

                if (end != maxLines) {
                    linesBreak.add(lineStart);
                    linesBreak.add(text.length());
                } else {
                    cursor -= text.length() - lineStart;
                    text = text.substring(0, last);
                }
                sizeChanged();

            }

            showCursor();
        }

        blockedTyping = linesBreak.size / 2 >= maxLines - 1 && maxLines == end;
    }

    protected InputListener createInputListener () {
        return new RdTextAreaListener();
    }

    /** Updates the current line, checking the cursor position in the text **/
    void updateCurrentLine () {
        int index = calculateCurrentLineIndex(cursor);
        int line = index / 2;
        // Special case when cursor moves to the beginning of the line from the end of another and a word
        // wider than the box
        if (index % 2 == 0 || index + 1 >= linesBreak.size || cursor != linesBreak.items[index]
                || linesBreak.items[index + 1] != linesBreak.items[index]) {
            if (line < linesBreak.size / 2 || text.length() == 0 || text.charAt(text.length() - 1) == NEWLINE
                    || text.charAt(text.length() - 1) == CARRIAGE_RETURN) {
                cursorLine = line;
            }
        }
    }

    /** Scroll the text area to show the line of the cursor **/
    void showCursor () {
        updateCurrentLine();
    }

    /** Calculates the text area line for the given cursor position **/
    private int calculateCurrentLineIndex (int cursor) {
        int index = 0;
        while (index < linesBreak.size && cursor > linesBreak.items[index]) {
            index++;
        }
        return index;
    }

    private void cutLabel(IntArray starts, IntArray ends) {
        var all = new LongArray();
        for (int i = 0; i < label.getWorkingLayout().lines(); i++) {
            all.addAll(label.getWorkingLayout().getLine(i).glyphs);
        }
        label.getWorkingLayout().clear();

        for (int i = 1; i < starts.size && i < maxLines; i++) {
            label.getWorkingLayout().pushLine();
        }
        //label.getWorkingLayout().pushLine();

        for (int i = 0; i < starts.size && i < maxLines; i++) {
            var line = label.getWorkingLayout().getLine(i);

            // if the first is an empty string, then there must be a character '\n'
            if (i == 0 && ends.get(i) == starts.get(i)) {}
            else line.glyphs.clear();
            line.glyphs.addAll(sub(all.items, starts.get(i), ends.get(i)));
        }

    }

    public void setSelection (int selectionStart, int selectionEnd) {
        super.setSelection(selectionStart, selectionEnd);
        updateCurrentLine();
    }

    protected void moveCursor (boolean forward, boolean jump) {
        int count = forward ? 1 : -1;
        int index = (cursorLine * 2) + count;
        if (index >= 0 && index + 1 < linesBreak.size && linesBreak.items[index] == cursor
                && linesBreak.items[index + 1] == cursor) {
            cursorLine += count;
            if (jump) {
                super.moveCursor(forward, jump);
            }
            showCursor();
        } else {
            super.moveCursor(forward, jump);
        }
        updateCurrentLine();

    }

    protected boolean continueCursor (int index, int offset) {
        int pos = calculateCurrentLineIndex(index + offset);
        return super.continueCursor(index, offset) && (pos < 0 || pos >= linesBreak.size - 2 || (linesBreak.items[pos + 1] != index)
                || (linesBreak.items[pos + 1] == linesBreak.items[pos + 2]));
    }

    private long[] sub(long[] arr, int start, int end) {
        if (end - start < 0) throw new IllegalArgumentException("end - start < 0");
        var newArr = new long[end - start];
        System.arraycopy(arr, start, newArr, 0, end - start);
        return newArr;
    }

    /** Input listener for the text area **/
    public class RdTextAreaListener extends TextFieldClickListener {
        protected void setCursorPosition (float x, float y) {
            moveOffset = -1;

            Drawable background = style.background;
            float height = getHeight();

            if (background != null) {
                height -= background.getTopHeight();
                x -= background.getLeftWidth();
            }
            x = Math.max(0, x);
            if (background != null) {
                y -= background.getTopHeight();
            }

            cursorLine = (int) Math.floor((height - y) / getLineHeight());
            cursorLine = Math.max(0, Math.min(cursorLine, getLines() - 1));

            super.setCursorPosition(x, y);
            updateCurrentLine();
        }

        public boolean keyDown (InputEvent event, int keycode) {
            boolean result = super.keyDown(event, keycode);
            if (hasKeyboardFocus()) {
                boolean repeat = false;
                boolean shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
                if (keycode == Input.Keys.DOWN) {
                    if (shift) {
                        if (!hasSelection) {
                            selectionStart = cursor;
                            hasSelection = true;
                        }
                    } else {
                        clearSelection();
                    }
                    moveCursorLine(cursorLine + 1);
                    repeat = true;

                } else if (keycode == Input.Keys.UP) {
                    if (shift) {
                        if (!hasSelection) {
                            selectionStart = cursor;
                            hasSelection = true;
                        }
                    } else {
                        clearSelection();
                    }
                    moveCursorLine(cursorLine - 1);
                    repeat = true;

                } else {
                    moveOffset = -1;
                }
                if (repeat) {
                    scheduleKeyRepeatTask(keycode);
                }
                showCursor();
                return true;
            }
            return result;
        }

        protected boolean checkFocusTraversal (char character) {
            return focusTraversal && character == TAB;
        }

        public boolean keyTyped (InputEvent event, char character) {
            switch (character) {
                case BACKSPACE:
                case TAB:
                case NEWLINE:
                case CARRIAGE_RETURN:
                    boolean result = super.keyTyped(event, character);
                    showCursor();
                    return result;
            }

            if (blockedTyping) return false;
            boolean result = super.keyTyped(event, character);
            showCursor();
            return result;
        }

        protected void goHome (boolean jump) {
            if (jump) {
                cursor = 0;
            } else if (cursorLine * 2 < linesBreak.size) {
                cursor = linesBreak.get(cursorLine * 2);
            }
        }

        protected void goEnd (boolean jump) {
            if (jump || cursorLine >= getLines()) {
                cursor = text.length();
            } else if (cursorLine * 2 + 1 < linesBreak.size) {
                cursor = linesBreak.get(cursorLine * 2 + 1);
            }
        }
    }

}
