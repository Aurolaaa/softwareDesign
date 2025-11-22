package lab1;

import lab1.command.*;
import lab1.model.TextEditor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextEditorTest {

    private TextEditor editor;
    private CommandHistory history;

    @BeforeEach
    void setUp() {
        editor = new TextEditor();
        history = new CommandHistory();
        // 初始化一些内容
        editor.append("Line 1");
        editor.append("Line 2");
    }

    // --- 测试 Model 层 (基础功能) ---

    @Test
    void testInsert() {
        // 在第1行第1个位置插入
        editor.insert(1, 1, "Start ");
        assertEquals("Start Line 1", editor.getLines().get(0));
    }

    @Test
    void testDelete() {
        // 删除 "Line " (5个字符)
        editor.delete(1, 1, 5);
        assertEquals("1", editor.getLines().get(0));
    }

    // --- 测试 Command 层 (Undo/Redo) ---

    @Test
    void testInsertUndo() {
        Command cmd = new InsertCommand(editor, 1, 1, "Hello");
        history.execute(cmd);
        assertEquals("HelloLine 1", editor.getLines().get(0));

        history.undo();
        assertEquals("Line 1", editor.getLines().get(0), "撤销后应恢复原状");
    }

    @Test
    void testDeleteUndo() {
        // 原文: Line 1
        Command cmd = new DeleteCommand(editor, 1, 1, 5); // 删掉 "Line "
        history.execute(cmd);
        assertEquals("1", editor.getLines().get(0));

        history.undo();
        assertEquals("Line 1", editor.getLines().get(0), "撤销删除应恢复原状");
    }

    @Test
    void testAppendUndo() {
        Command cmd = new AppendCommand(editor, "Line 3");
        history.execute(cmd);
        assertEquals(3, editor.getLines().size());

        history.undo();
        assertEquals(2, editor.getLines().size(), "撤销追加应移除最后一行");
    }
}