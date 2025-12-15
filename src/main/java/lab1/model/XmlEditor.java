package lab1.model;

/**
 * XML 编辑器 - 使用 DOM 树结构
 * 使用 Composite 模式（XmlNode）管理 XML 结构
 */
public class XmlEditor implements Editor {
    private XmlNode root;
    private boolean isModified;
    
    public XmlEditor() {
        // 初始化时创建默认根节点
        this.root = new XmlNode("root", "root");
        this.isModified = false;
    }
    
    // --- 核心 XML 操作 ---
    
    /**
     * 在目标节点前插入兄弟节点
     */
    public void insertBefore(String tagName, String newId, String targetId, String text) {
        // 检查 ID 唯一性
        if (root.hasIdInSubtree(newId)) {
            throw new IllegalArgumentException("ID 已存在: " + newId);
        }
        
        XmlNode target = root.findById(targetId);
        if (target == null) {
            throw new IllegalArgumentException("目标节点不存在: " + targetId);
        }
        
        if (target.isRoot()) {
            throw new IllegalArgumentException("不能在根节点前插入");
        }
        
        XmlNode parent = target.getParent();
        int index = parent.getChildIndex(target);
        
        XmlNode newNode = text != null ? new XmlNode(tagName, newId, text) : new XmlNode(tagName, newId);
        parent.insertChild(index, newNode);
        
        isModified = true;
    }
    
    /**
     * 追加子节点到指定父节点
     */
    public void appendChild(String tagName, String newId, String parentId, String text) {
        // 检查 ID 唯一性
        if (root.hasIdInSubtree(newId)) {
            throw new IllegalArgumentException("ID 已存在: " + newId);
        }
        
        XmlNode parent = root.findById(parentId);
        if (parent == null) {
            throw new IllegalArgumentException("父节点不存在: " + parentId);
        }
        
        XmlNode newNode = text != null ? new XmlNode(tagName, newId, text) : new XmlNode(tagName, newId);
        parent.addChild(newNode);
        
        isModified = true;
    }
    
    /**
     * 修改节点 ID
     */
    public void editId(String oldId, String newId) {
        if (root.hasIdInSubtree(newId)) {
            throw new IllegalArgumentException("新 ID 已存在: " + newId);
        }
        
        XmlNode node = root.findById(oldId);
        if (node == null) {
            throw new IllegalArgumentException("节点不存在: " + oldId);
        }
        
        node.setId(newId);
        isModified = true;
    }
    
    /**
     * 修改节点文本内容
     */
    public void editText(String elementId, String text) {
        XmlNode node = root.findById(elementId);
        if (node == null) {
            throw new IllegalArgumentException("节点不存在: " + elementId);
        }
        
        if (!node.isLeaf()) {
            throw new IllegalArgumentException("非叶子节点不能修改文本内容");
        }
        
        node.setTextContent(text);
        isModified = true;
    }
    
    /**
     * 删除节点及其子树
     */
    public void deleteElement(String elementId) {
        XmlNode node = root.findById(elementId);
        if (node == null) {
            throw new IllegalArgumentException("节点不存在: " + elementId);
        }
        
        if (node.isRoot()) {
            throw new IllegalArgumentException("不能删除根节点");
        }
        
        XmlNode parent = node.getParent();
        parent.removeChild(node);
        
        isModified = true;
    }
    
    /**
     * 获取树形结构字符串
     */
    public String getTreeString() {
        return root.toTreeString();
    }
    
    /**
     * 根据 ID 查找节点
     */
    public XmlNode findNodeById(String id) {
        return root.findById(id);
    }
    
    /**
     * 获取所有文本内容（用于拼写检查）
     */
    public String getAllTextContent() {
        StringBuilder sb = new StringBuilder();
        collectTextContent(root, sb);
        return sb.toString().trim();
    }
    
    private void collectTextContent(XmlNode node, StringBuilder sb) {
        if (node.getTextContent() != null) {
            sb.append(node.getTextContent()).append(" ");
        }
        for (XmlNode child : node.getChildren()) {
            collectTextContent(child, sb);
        }
    }
    
    // --- Editor 接口实现 ---
    
    @Override
    public String getContent() {
        return root.toXmlString();
    }
    
    @Override
    public void setContent(String content) {
        // 简化实现：解析 XML 字符串（实际项目中应使用 DOM Parser）
        // 这里我们假设内容已经是正确的 XML 格式
        // 为了支持 load 功能，需要实现简单的 XML 解析
        // 暂时先保留原有结构，后续在需要时补充解析逻辑
        // TODO: 实现 XML 解析
        isModified = false;
    }
    
    @Override
    public boolean isModified() {
        return isModified;
    }
    
    @Override
    public void setModified(boolean modified) {
        this.isModified = modified;
    }
    
    @Override
    public String getEditorType() {
        return "xml";
    }
    
    // --- Getters ---
    
    public XmlNode getRoot() {
        return root;
    }
    
    public void setRoot(XmlNode root) {
        this.root = root;
        this.isModified = true;
    }
}


