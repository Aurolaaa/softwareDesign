package lab1.model; //

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TextEditor {
    // 使用 List<String> 存储行，符合实验要求 [cite: 39]
    private List<String> lines;
    private boolean isModified;

    public TextEditor() {
        this.lines = new ArrayList<>();
        this.isModified = false;
    }

    // 获取全部文本，用于保存
    public String getText() {
        return String.join("\n", lines);
    }

    // 设置文本（加载文件时用）
    public void setText(String content) {
        lines.clear();
        if (content.isEmpty()) {
            return;
        }
        // 按换行符分割
        String[] splitLines = content.split("\n", -1);
        for (String line : splitLines) {
            lines.add(line);
        }
    }

    public List<String> getLines() {
        return lines;
    }

    // --- 核心编辑操作 (逻辑尽量简单，方便 Command 调用) ---

    public void insert(int line, int col, String text) {
        // 边界检查：行号越界 [cite: 200]
        if (line < 1 || line > lines.size() + 1) {
            throw new IllegalArgumentException("行号越界");
        }

        // 特殊情况：空文件插入 [cite: 201]
        if (lines.isEmpty()) {
            if (line == 1 && col == 1) {
                lines.add(text); // 简化处理，直接加进去，暂不处理换行符拆分，由Command层处理复杂逻辑
                isModified = true;
                return;
            } else {
                throw new IllegalArgumentException("空文件只能在1:1位置插入");
            }
        }

        // 获取当前行内容 (注意：用户输入line是1，List下标是0)
        String currentLine = lines.get(line - 1);

        // 边界检查：列号越界 [cite: 200]
        if (col < 1 || col > currentLine.length() + 1) {
            throw new IllegalArgumentException("列号越界");
        }

        // 拼接字符串
        StringBuilder sb = new StringBuilder(currentLine);
        sb.insert(col - 1, text);
        lines.set(line - 1, sb.toString());
        isModified = true;
    }

    public void delete(int line, int col, int length) {
        if (line < 1 || line > lines.size()) throw new IllegalArgumentException("行号越界");

        String currentLine = lines.get(line - 1);
        if (col < 1 || col > currentLine.length() + 1) throw new IllegalArgumentException("列号越界");

        // 检查删除长度是否超出行尾 [cite: 211]
        if (col - 1 + length > currentLine.length()) {
            throw new IllegalArgumentException("删除长度超出行尾");
        }

        StringBuilder sb = new StringBuilder(currentLine);
        sb.delete(col - 1, col - 1 + length);
        lines.set(line - 1, sb.toString());
        isModified = true;
    }

    public boolean isModified() {
        return isModified;
    }

    public void setModified(boolean modified) {
        isModified = modified;
    }
}