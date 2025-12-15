package lab1.command.xml;

import lab1.command.Command;
import lab1.model.XmlEditor;
import lab1.model.XmlNode;

/**
 * XML 插入兄弟节点命令
 * insert-before <tagName> <newId> <targetId> ["text"]
 */
public class InsertBeforeCommand implements Command {
    private XmlEditor editor;
    private String tagName;
    private String newId;
    private String targetId;
    private String text;
    private XmlNode backup;  // 用于 undo 的备份
    
    public InsertBeforeCommand(XmlEditor editor, String tagName, String newId, String targetId, String text) {
        this.editor = editor;
        this.tagName = tagName;
        this.newId = newId;
        this.targetId = targetId;
        this.text = text;
    }
    
    @Override
    public void execute() {
        // 备份当前 DOM 树
        backup = editor.getRoot().clone();
        // 执行插入
        editor.insertBefore(tagName, newId, targetId, text);
        System.out.println("已在节点 " + targetId + " 前插入新节点 " + newId);
    }
    
    @Override
    public void undo() {
        if (backup != null) {
            editor.setRoot(backup);
            System.out.println("已撤销插入操作");
        }
    }
}
