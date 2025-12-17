package lab1.command.xml;

import lab1.command.Command;
import lab1.model.XmlEditor;
import lab1.model.XmlNode;

/**
 * XML 修改节点文本命令
 * edit-text <elementId> "text"
 */
public class EditTextCommand implements Command {
    private XmlEditor editor;
    private String elementId;
    private String text;
    private XmlNode backup;

    public EditTextCommand(XmlEditor editor, String elementId, String text) {
        this.editor = editor;
        this.elementId = elementId;
        this.text = text;
    }

    @Override
    public void execute() {
        backup = editor.getRoot().clone();
        editor.editText(elementId, text);
        System.out.println("已修改节点 " + elementId + " 的文本内容");
    }

    @Override
    public void undo() {
        if (backup != null) {
            editor.setRoot(backup);
            System.out.println("已撤销文本修改操作");
        }
    }
}
