package org.example;

public class Document {
    private final StringBuilder text;

    public Document() {
        this.text = new StringBuilder();
    }

    public String getText() {
        return this.text.toString();
    }

    public void addText(String newText) {
        this.text.append(newText);
    }

    public void removeText(int start, int end) {
        if (start < 0 || end < 0)
            throw new IllegalArgumentException("인텍스는 음수가 될 수 없습니다.");
        if (start > end)
            throw new IllegalArgumentException("start index must not exceed end index");
        if (end > this.text.length())
            throw new IndexOutOfBoundsException();
        this.text.delete(start, end);
    }

    public void clear() {
        this.text.setLength(0);
    }

    public void setText(String value) {
        this.text.setLength(0);
        if (value != null) this.text.append(value);
    }
}
