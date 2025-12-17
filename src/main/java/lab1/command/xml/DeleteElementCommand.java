package lab1.command.xml;

import lab1.command.Command;
import lab1.model.XmlEditor;
import lab1.model.XmlNode;

/**
 * XML 删除元素命令
 * delete-element <elementId>
 */
public class DeleteElementCommand implements Command {
    private XmlEditor editor;
    private String elementId;
    private XmlNode backup;

    public DeleteElementCommand(XmlEditor editor, String elementId) {
        this.editor = editor;
        this.elementId = elementId;
    }

    @Override
    public void execute() {
        backup = editor.getRoot().clone();
        editor.deleteElement(elementId);
        System.out.println("已删除节点 " + elementId + " 及其子树");
    }

    @Override
    public void undo() {
        if (backup != null) {
            editor.setRoot(backup);
            System.out.println("已撤销删除操作");
        }
    }
}
