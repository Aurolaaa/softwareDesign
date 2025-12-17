package lab1.model;

/**
 * 编辑器通用接口 - 多态设计的基础
 * TextEditor 和 XmlEditor 都实现此接口
 */
public interface Editor {
    /**
     * 获取编辑器内容的字符串表示
     */
    String getContent();

    /**
     * 设置编辑器内容
     */
    void setContent(String content);

    /**
     * 检查编辑器内容是否被修改
     */
    boolean isModified();

    /**
     * 设置编辑器的修改状态
     */
    void setModified(boolean modified);

    /**
     * 获取编辑器类型（用于区分不同编辑器）
     */
    String getEditorType();
}
