package lab1;

import lab1.model.XmlEditor;
import lab1.model.XmlNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * XML 编辑器核心功能测试
 */
class XmlEditorTest {

    private XmlEditor editor;

    @BeforeEach
    void setUp() {
        editor = new XmlEditor();
    }

    // --- 基础操作测试 ---

    @Test
    void testAppendChild() {
        editor.appendChild("book", "book1", "root", null);
        XmlNode root = editor.getRoot();
        assertEquals(1, root.getChildren().size(), "root 应该有 1 个子节点");
        
        XmlNode book = root.getChildren().get(0);
        assertEquals("book", book.getTagName());
        assertEquals("book1", book.getId());
    }

    @Test
    void testAppendChildWithText() {
        editor.appendChild("title", "t1", "root", "Design Patterns");
        XmlNode title = editor.findNodeById("t1");
        assertNotNull(title);
        assertEquals("Design Patterns", title.getTextContent());
    }

    @Test
    void testInsertBefore() {
        // 先添加两个节点
        editor.appendChild("item", "i1", "root", null);
        editor.appendChild("item", "i3", "root", null);
        
        // 在 i3 前插入 i2
        editor.insertBefore("item", "i2", "i3", null);
        
        XmlNode root = editor.getRoot();
        assertEquals(3, root.getChildren().size());
        assertEquals("i2", root.getChildren().get(1).getId(), "i2 应该在中间");
    }

    @Test
    void testEditId() {
        editor.appendChild("item", "oldId", "root", null);
        editor.editId("oldId", "newId");
        
        assertNull(editor.findNodeById("oldId"), "旧 ID 不应存在");
        assertNotNull(editor.findNodeById("newId"), "新 ID 应该存在");
    }

    @Test
    void testEditText() {
        editor.appendChild("title", "t1", "root", "Old Text");
        editor.editText("t1", "New Text");
        
        XmlNode node = editor.findNodeById("t1");
        assertEquals("New Text", node.getTextContent());
    }

    @Test
    void testDeleteElement() {
        editor.appendChild("item", "i1", "root", null);
        editor.appendChild("item", "i2", "root", null);
        
        editor.deleteElement("i1");
        
        assertEquals(1, editor.getRoot().getChildren().size());
        assertNull(editor.findNodeById("i1"));
    }

    // --- 约束检查测试 ---

    @Test
    void testDuplicateIdThrowsException() {
        editor.appendChild("item", "i1", "root", null);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            editor.appendChild("item", "i1", "root", null);
        });
        
        assertTrue(exception.getMessage().contains("ID 已存在"));
    }

    @Test
    void testEditToExistingIdThrowsException() {
        editor.appendChild("item", "i1", "root", null);
        editor.appendChild("item", "i2", "root", null);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            editor.editId("i2", "i1");
        });
        
        assertTrue(exception.getMessage().contains("已存在"));
    }

    @Test
    void testDeleteRootThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            editor.deleteElement("root");
        });
        
        assertTrue(exception.getMessage().contains("根节点"));
    }

    @Test
    void testEditNonLeafTextThrowsException() {
        editor.appendChild("parent", "p1", "root", null);
        editor.appendChild("child", "c1", "p1", null);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            editor.editText("p1", "some text");
        });
        
        assertTrue(exception.getMessage().contains("非叶子节点"));
    }

    @Test
    void testMixedContentThrowsException() {
        editor.appendChild("item", "i1", "root", "some text");
        
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            editor.appendChild("child", "c1", "i1", null);
        });
        
        assertTrue(exception.getMessage().contains("混合内容"));
    }

    // --- 辅助功能测试 ---

    @Test
    void testGetTreeString() {
        editor.appendChild("book", "b1", "root", null);
        editor.appendChild("title", "t1", "b1", "Test Book");
        
        String tree = editor.getTreeString();
        assertTrue(tree.contains("root"));
        assertTrue(tree.contains("book"));
        assertTrue(tree.contains("title"));
        assertTrue(tree.contains("Test Book"));
    }

    @Test
    void testGetAllTextContent() {
        editor.appendChild("book", "b1", "root", null);
        editor.appendChild("title", "t1", "b1", "First Title");
        editor.appendChild("author", "a1", "b1", "John Doe");
        
        String allText = editor.getAllTextContent();
        assertTrue(allText.contains("First Title"));
        assertTrue(allText.contains("John Doe"));
    }

    @Test
    void testGetContent() {
        editor.appendChild("book", "b1", "root", null);
        editor.appendChild("title", "t1", "b1", "Test");
        
        String xml = editor.getContent();
        assertTrue(xml.contains("<root"));
        assertTrue(xml.contains("<book"));
        assertTrue(xml.contains("<title"));
        assertTrue(xml.contains("Test"));
    }

    @Test
    void testIsModified() {
        assertFalse(editor.isModified());
        
        editor.appendChild("item", "i1", "root", null);
        assertTrue(editor.isModified());
        
        editor.setModified(false);
        assertFalse(editor.isModified());
    }

    @Test
    void testEditorType() {
        assertEquals("xml", editor.getEditorType());
    }
}

