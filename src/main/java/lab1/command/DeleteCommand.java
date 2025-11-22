package lab1.command;

import lab1.model.TextEditor;

public class DeleteCommand implements Command {
    private TextEditor editor;
    private int line;
    private int col;
    private int len;
    private String deletedText; // 关键：用来存“被删掉的内容”作为备份

    public DeleteCommand(TextEditor editor, int line, int col, int len) {
        this.editor = editor;
        this.line = line;
        this.col = col;
        this.len = len;
    }

    @Override
    public void execute() {
        // editor.delete 会返回被删掉的字符串，我们把它存起来
        this.deletedText = editor.delete(line, col, len);
    }

    @Override
    public void undo() {
        // 撤销删除 = 把刚才删掉的文本，在原来的位置插回去
        if (deletedText != null) {
            editor.insert(line, col, deletedText);
        }
    }
}