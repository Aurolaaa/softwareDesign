package lab1.model;

import java.util.ArrayList;
import java.util.List;

/**
 * XML 节点 - Composite 模式实现
 * 用于构建 XML DOM 树结构
 */
public class XmlNode {
    private String tagName;
    private String id;
    private String textContent; // 文本内容（仅叶子节点）
    private List<XmlNode> children; // 子节点列表
    private XmlNode parent; // 父节点引用

    public XmlNode(String tagName, String id) {
        this.tagName = tagName;
        this.id = id;
        this.textContent = null;
        this.children = new ArrayList<>();
        this.parent = null;
    }

    public XmlNode(String tagName, String id, String textContent) {
        this.tagName = tagName;
        this.id = id;
        this.textContent = textContent;
        this.children = new ArrayList<>();
        this.parent = null;
    }

    // --- Composite 模式方法 ---

    /**
     * 添加子节点
     */
    public void addChild(XmlNode child) {
        if (this.textContent != null) {
            throw new IllegalStateException("不支持混合内容：已有文本的节点不能添加子节点");
        }
        children.add(child);
        child.parent = this;
    }

    /**
     * 在指定位置插入子节点
     */
    public void insertChild(int index, XmlNode child) {
        if (this.textContent != null) {
            throw new IllegalStateException("不支持混合内容：已有文本的节点不能添加子节点");
        }
        children.add(index, child);
        child.parent = this;
    }

    /**
     * 移除子节点
     */
    public void removeChild(XmlNode child) {
        children.remove(child);
        child.parent = null;
    }

    /**
     * 获取子节点在父节点中的索引
     */
    public int getChildIndex(XmlNode child) {
        return children.indexOf(child);
    }

    /**
     * 判断是否为叶子节点
     */
    public boolean isLeaf() {
        return children.isEmpty();
    }

    /**
     * 判断是否为根节点
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * 递归查找指定 ID 的节点
     */
    public XmlNode findById(String targetId) {
        if (this.id.equals(targetId)) {
            return this;
        }
        for (XmlNode child : children) {
            XmlNode found = child.findById(targetId);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * 检查 ID 是否在子树中唯一
     */
    public boolean hasIdInSubtree(String targetId) {
        return findById(targetId) != null;
    }

    /**
     * 转换为 XML 字符串
     */
    public String toXmlString() {
        return toXmlString(0);
    }

    private String toXmlString(int indent) {
        StringBuilder sb = new StringBuilder();
        String indentStr = "  ".repeat(indent);

        // 开始标签
        sb.append(indentStr).append("<").append(tagName).append(" id=\"").append(id).append("\">");

        if (textContent != null) {
            // 叶子节点：直接输出文本
            sb.append(textContent);
            sb.append("</").append(tagName).append(">\n");
        } else if (children.isEmpty()) {
            // 空节点
            sb.append("</").append(tagName).append(">\n");
        } else {
            // 有子节点：递归输出
            sb.append("\n");
            for (XmlNode child : children) {
                sb.append(child.toXmlString(indent + 1));
            }
            sb.append(indentStr).append("</").append(tagName).append(">\n");
        }

        return sb.toString();
    }

    /**
     * 打印树形结构（用于 xml-tree 命令）
     */
    public String toTreeString() {
        return toTreeString(0);
    }

    private String toTreeString(int depth) {
        StringBuilder sb = new StringBuilder();
        String prefix = "  ".repeat(depth);

        // 显示节点信息：标签名 [id]
        sb.append(prefix).append("|- ").append(tagName).append(" [id=").append(id).append("]");
        if (textContent != null) {
            sb.append(" \"").append(textContent).append("\"");
        }
        sb.append("\n");

        // 递归显示子节点
        for (XmlNode child : children) {
            sb.append(child.toTreeString(depth + 1));
        }

        return sb.toString();
    }

    /**
     * 克隆节点（深拷贝，用于 undo/redo）
     */
    public XmlNode clone() {
        XmlNode cloned = new XmlNode(this.tagName, this.id, this.textContent);
        for (XmlNode child : this.children) {
            cloned.addChild(child.clone());
        }
        return cloned;
    }

    // --- Getters & Setters ---

    public String getTagName() {
        return tagName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        if (!children.isEmpty()) {
            throw new IllegalStateException("不支持混合内容：有子节点的元素不能设置文本");
        }
        this.textContent = textContent;
    }

    public List<XmlNode> getChildren() {
        return children;
    }

    public XmlNode getParent() {
        return parent;
    }
}
