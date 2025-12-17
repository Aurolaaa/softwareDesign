package lab1.model;

import java.util.ArrayList;
import java.util.List;

public class TextEditor implements Editor {
    private List<String> lines;
    private boolean isModified;

    public TextEditor() {
        this.lines = new ArrayList<>();
        this.isModified = false;
    }

    public String getText() {
        return String.join("\n", lines);
    }

    public void setText(String content) {
        lines.clear();
        if (content.isEmpty()) {
            return;
        }
        String[] splitLines = content.split("\n", -1);
        for (String line : splitLines) {
            lines.add(line);
        }
    }

    public List<String> getLines() {
        return lines;
    }

    // --- 核心操作 ---

    public void append(String text) {
        lines.add(text);
        isModified = true;
    }

    public void insert(int line, int col, String text) {
        if (line < 1 || line > lines.size() + 1) {
            throw new IllegalArgumentException("行号越界");
        }
        if (lines.isEmpty()) {
            if (line == 1 && col == 1) {
                lines.add(text);
                isModified = true;
                return;
            } else {
                throw new IllegalArgumentException("空文件只能在1:1位置插入");
            }
        }
        String currentLine = lines.get(line - 1);
        if (col < 1 || col > currentLine.length() + 1) {
            throw new IllegalArgumentException("列号越界");
        }

        StringBuilder sb = new StringBuilder(currentLine);
        sb.insert(col - 1, text);
        lines.set(line - 1, sb.toString());
        isModified = true;
    }

    public String delete(int line, int col, int length) {
        if (line < 1 || line > lines.size()) {
            throw new IllegalArgumentException("行号越界");
        }
        String currentLine = lines.get(line - 1);
        if (col < 1 || col > currentLine.length() + 1) {
            throw new IllegalArgumentException("列号越界");
        }
        if (col - 1 + length > currentLine.length()) {
            throw new IllegalArgumentException("删除长度超出行尾");
        }

        String deletedText = currentLine.substring(col - 1, col - 1 + length);
        StringBuilder sb = new StringBuilder(currentLine);
        sb.delete(col - 1, col - 1 + length);
        lines.set(line - 1, sb.toString());
        isModified = true;
        // 返回被删除的文本，用于Undo
        return deletedText;
    }

    public boolean isModified() {
        return isModified;
    }

    public void setModified(boolean modified) {
        isModified = modified;
    }

    // --- Editor 接口实现 ---

    @Override
    public String getContent() {
        return getText();
    }

    @Override
    public void setContent(String content) {
        setText(content);
    }

    @Override
    public String getEditorType() {
        return "text";
    }
}