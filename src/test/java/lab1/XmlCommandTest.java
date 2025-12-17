package lab1;

import lab1.command.CommandHistory;
import lab1.command.xml.*;
import lab1.model.XmlEditor;
import lab1.model.XmlNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * XML 命令 Undo/Redo 功能测试
 */
class XmlCommandTest {

    private XmlEditor editor;
    private CommandHistory history;

    @BeforeEach
    void setUp() {
        editor = new XmlEditor();
        history = new CommandHistory();
    }

    // --- AppendChildCommand 测试 ---

    @Test
    void testAppendChildCommand() {
        AppendChildCommand cmd = new AppendChildCommand(editor, "book", "b1", "root", "Test");
        history.execute(cmd);

        assertNotNull(editor.findNodeById("b1"));
        assertEquals("Test", editor.findNodeById("b1").getTextContent());
    }

    @Test
    void testAppendChildUndo() {
        AppendChildCommand cmd = new AppendChildCommand(editor, "book", "b1", "root", null);
        history.execute(cmd);

        assertEquals(1, editor.getRoot().getChildren().size());

        history.undo();

        assertEquals(0, editor.getRoot().getChildren().size(), "撤销后应该没有子节点");
        assertNull(editor.findNodeById("b1"));
    }

    @Test
    void testAppendChildRedo() {
        AppendChildCommand cmd = new AppendChildCommand(editor, "book", "b1", "root", null);
        history.execute(cmd);
        history.undo();
        history.redo();

        assertEquals(1, editor.getRoot().getChildren().size(), "重做后应该恢复子节点");
        assertNotNull(editor.findNodeById("b1"));
    }

    // --- InsertBeforeCommand 测试 ---

    @Test
    void testInsertBeforeCommand() {
        editor.appendChild("item", "i1", "root", null);

        InsertBeforeCommand cmd = new InsertBeforeCommand(editor, "item", "i0", "i1", "Before");
        history.execute(cmd);

        XmlNode root = editor.getRoot();
        assertEquals(2, root.getChildren().size());
        assertEquals("i0", root.getChildren().get(0).getId());
    }

    @Test
    void testInsertBeforeUndo() {
        editor.appendChild("item", "i1", "root", null);

        InsertBeforeCommand cmd = new InsertBeforeCommand(editor, "item", "i0", "i1", null);
        history.execute(cmd);
        history.undo();

        assertEquals(1, editor.getRoot().getChildren().size());
        assertNull(editor.findNodeById("i0"));
    }

    // --- EditIdCommand 测试 ---

    @Test
    void testEditIdCommand() {
        editor.appendChild("item", "oldId", "root", null);

        EditIdCommand cmd = new EditIdCommand(editor, "oldId", "newId");
        history.execute(cmd);

        assertNull(editor.findNodeById("oldId"));
        assertNotNull(editor.findNodeById("newId"));
    }

    @Test
    void testEditIdUndo() {
        editor.appendChild("item", "oldId", "root", null);

        EditIdCommand cmd = new EditIdCommand(editor, "oldId", "newId");
        history.execute(cmd);
        history.undo();

        assertNotNull(editor.findNodeById("oldId"), "撤销后旧 ID 应该恢复");
        assertNull(editor.findNodeById("newId"));
    }

    // --- EditTextCommand 测试 ---

    @Test
    void testEditTextCommand() {
        editor.appendChild("title", "t1", "root", "Old Text");

        EditTextCommand cmd = new EditTextCommand(editor, "t1", "New Text");
        history.execute(cmd);

        assertEquals("New Text", editor.findNodeById("t1").getTextContent());
    }

    @Test
    void testEditTextUndo() {
        editor.appendChild("title", "t1", "root", "Old Text");

        EditTextCommand cmd = new EditTextCommand(editor, "t1", "New Text");
        history.execute(cmd);
        history.undo();

        assertEquals("Old Text", editor.findNodeById("t1").getTextContent());
    }

    // --- DeleteElementCommand 测试 ---

    @Test
    void testDeleteElementCommand() {
        editor.appendChild("item", "i1", "root", null);
        editor.appendChild("child", "c1", "i1", null);

        DeleteElementCommand cmd = new DeleteElementCommand(editor, "i1");
        history.execute(cmd);

        assertEquals(0, editor.getRoot().getChildren().size());
        assertNull(editor.findNodeById("i1"));
        assertNull(editor.findNodeById("c1"), "子节点也应该被删除");
    }

    @Test
    void testDeleteElementUndo() {
        editor.appendChild("item", "i1", "root", null);
        editor.appendChild("child", "c1", "i1", "text");

        DeleteElementCommand cmd = new DeleteElementCommand(editor, "i1");
        history.execute(cmd);
        history.undo();

        assertNotNull(editor.findNodeById("i1"), "撤销后节点应该恢复");
        assertNotNull(editor.findNodeById("c1"), "撤销后子节点也应该恢复");
        assertEquals("text", editor.findNodeById("c1").getTextContent());
    }

    // --- 复杂场景测试 ---

    @Test
    void testMultipleCommandsUndo() {
        // 执行多个命令
        history.execute(new AppendChildCommand(editor, "book", "b1", "root", null));
        history.execute(new AppendChildCommand(editor, "title", "t1", "b1", "Book 1"));
        history.execute(new AppendChildCommand(editor, "author", "a1", "b1", "Author 1"));

        assertEquals(3, countAllNodes(editor.getRoot()) - 1); // -1 不算 root

        // 撤销所有
        history.undo();
        history.undo();
        history.undo();

        assertEquals(0, editor.getRoot().getChildren().size());
    }

    @Test
    void testUndoRedoChain() {
        history.execute(new AppendChildCommand(editor, "item", "i1", "root", null));
        history.execute(new AppendChildCommand(editor, "item", "i2", "root", null));

        history.undo(); // 撤销 i2
        assertEquals(1, editor.getRoot().getChildren().size());

        history.redo(); // 恢复 i2
        assertEquals(2, editor.getRoot().getChildren().size());

        history.undo(); // 再次撤销 i2
        history.undo(); // 撤销 i1
        assertEquals(0, editor.getRoot().getChildren().size());
    }

    @Test
    void testNewCommandClearsRedoStack() {
        history.execute(new AppendChildCommand(editor, "item", "i1", "root", null));
        history.execute(new AppendChildCommand(editor, "item", "i2", "root", null));

        history.undo(); // 撤销 i2

        // 执行新命令应该清空 redo 栈
        history.execute(new AppendChildCommand(editor, "item", "i3", "root", null));

        // redo 应该无效（因为栈已清空）
        int beforeRedo = editor.getRoot().getChildren().size();
        history.redo();
        assertEquals(beforeRedo, editor.getRoot().getChildren().size(), "redo 应该无效");
    }

    // --- 辅助方法 ---

    private int countAllNodes(XmlNode node) {
        int count = 1;
        for (XmlNode child : node.getChildren()) {
            count += countAllNodes(child);
        }
        return count;
    }
}
