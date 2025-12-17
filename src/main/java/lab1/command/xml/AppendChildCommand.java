package lab1.command.xml;

import lab1.command.Command;
import lab1.model.XmlEditor;
import lab1.model.XmlNode;

/**
 * XML 追加子节点命令
 * append-child <tagName> <newId> <parentId> ["text"]
 */
public class AppendChildCommand implements Command {
    private XmlEditor editor;
    private String tagName;
    private String newId;
    private String parentId;
    private String text;
    private XmlNode backup;

    public AppendChildCommand(XmlEditor editor, String tagName, String newId, String parentId, String text) {
        this.editor = editor;
        this.tagName = tagName;
        this.newId = newId;
        this.parentId = parentId;
        this.text = text;
    }

    @Override
    public void execute() {
        backup = editor.getRoot().clone();
        editor.appendChild(tagName, newId, parentId, text);
        System.out.println("已向节点 " + parentId + " 追加子节点 " + newId);
    }

    @Override
    public void undo() {
        if (backup != null) {
            editor.setRoot(backup);
            System.out.println("已撤销追加操作");
        }
    }
}
