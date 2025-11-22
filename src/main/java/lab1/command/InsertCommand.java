package lab1.command;

import lab1.model.TextEditor;

public class InsertCommand implements Command {
    private TextEditor editor;
    private int line;
    private int col;
    private String text;

    public InsertCommand(TextEditor editor, int line, int col, String text) {
        this.editor = editor;
        this.line = line;
        this.col = col;
        this.text = text;
    }

    @Override
    public void execute() {
        // 调用 editor 去真正插入文本
        editor.insert(line, col, text);
    }

    @Override
    public void undo() {
        // 撤销插入 = 在原来的位置删除掉同样长度的文本
        // 注意：这里我们只要删除刚插进去的长度即可
        editor.delete(line, col, text.length());
    }
}