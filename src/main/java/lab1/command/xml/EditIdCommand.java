package lab1.command.xml;

import lab1.command.Command;
import lab1.model.XmlEditor;
import lab1.model.XmlNode;

/**
 * XML 修改节点 ID 命令
 * edit-id <oldId> <newId>
 */
public class EditIdCommand implements Command {
    private XmlEditor editor;
    private String oldId;
    private String newId;
    private XmlNode backup;
    
    public EditIdCommand(XmlEditor editor, String oldId, String newId) {
        this.editor = editor;
        this.oldId = oldId;
        this.newId = newId;
    }
    
    @Override
    public void execute() {
        backup = editor.getRoot().clone();
        editor.editId(oldId, newId);
        System.out.println("已将节点 ID 从 " + oldId + " 修改为 " + newId);
    }
    
    @Override
    public void undo() {
        if (backup != null) {
            editor.setRoot(backup);
            System.out.println("已撤销 ID 修改操作");
        }
    }
}
