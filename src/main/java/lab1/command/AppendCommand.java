package lab1.command;

import lab1.model.TextEditor;
import java.util.List;

public class AppendCommand implements Command {
    private TextEditor editor;
    private String text;

    public AppendCommand(TextEditor editor, String text) {
        this.editor = editor;
        this.text = text;
    }

    @Override
    public void execute() {
        editor.append(text);
    }

    @Override
    public void undo() {
        // 撤销追加 = 删除最后一行
        List<String> lines = editor.getLines();
        if (!lines.isEmpty()) {
            // 直接移除 list 的最后一个元素
            lines.remove(lines.size() - 1);
            // 记得标记为已修改，虽然是撤销，但文件状态确实变了
            editor.setModified(true);
        }
    }
}