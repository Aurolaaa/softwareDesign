package lab1.model;

import lab1.utils.SessionStatistics;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class Workspace {
    private Map<String, Editor> fileMap = new LinkedHashMap<>(); // 改为多态：Editor 接口
    private String currentFile;
    private boolean logEnabled = false;
    private final String WORKSPACE_FILE = ".editor_workspace";
    private SessionStatistics statistics = new SessionStatistics(); // 统计模块

    // --- 文件操作 ---

    /**
     * 初始化文件 - 支持 text 和 xml 类型
     * 
     * @param editorType "text" 或 "xml"
     * @param filename   文件名
     * @param withLog    是否启用日志
     */
    public void init(String editorType, String filename, boolean withLog) throws IOException {
        Path path = Paths.get(filename);
        if (Files.exists(path)) {
            throw new IOException("文件已存在: " + filename);
        }

        Files.createFile(path);
        Editor editor;

        if ("xml".equalsIgnoreCase(editorType)) {
            // 创建 XML 编辑器，默认包含 <root id="root">
            XmlEditor xmlEditor = new XmlEditor();
            xmlEditor.setModified(true); // 需要保存
            editor = xmlEditor;
        } else {
            // 创建文本编辑器
            TextEditor textEditor = new TextEditor();
            if (withLog) {
                textEditor.append("#log");
                textEditor.setModified(true);
                logEnabled = true;
            }
            editor = textEditor;
        }

        fileMap.put(filename, editor);
        currentFile = filename;

        // 开始统计编辑时长
        statistics.onFileActivated(filename);

        System.out.println("已初始化缓冲区: " + filename + " (类型: " + editorType + ")");
    }

    public void load(String filename) throws IOException {
        Path path = Paths.get(filename);
        if (fileMap.containsKey(filename)) {
            // 切换到已存在的文件
            statistics.onFileActivated(filename);
            currentFile = filename;
        } else {
            Editor editor;
            boolean isXml = filename.endsWith(".xml");

            if (isXml) {
                // 创建 XML 编辑器
                XmlEditor xmlEditor = new XmlEditor();
                if (Files.exists(path)) {
                    String content = Files.readString(path, StandardCharsets.UTF_8);
                    xmlEditor.setContent(content);
                    // TODO: 需要实现 XML 解析逻辑
                } else {
                    xmlEditor.setModified(true);
                }
                editor = xmlEditor;
            } else {
                // 创建文本编辑器
                TextEditor textEditor = new TextEditor();
                if (Files.exists(path)) {
                    String content = Files.readString(path, StandardCharsets.UTF_8);
                    textEditor.setText(content);
                    // 检查 #log
                    if (!textEditor.getLines().isEmpty() && textEditor.getLines().get(0).trim().equals("#log")) {
                        logEnabled = true;
                    }
                } else {
                    textEditor.setModified(true);
                }
                editor = textEditor;
            }

            fileMap.put(filename, editor);
            currentFile = filename;

            // 重置统计（新加载的文件从0开始计时）
            statistics.resetFile(filename);
            statistics.onFileActivated(filename);
        }
        System.out.println("当前活动文件: " + filename);
    }

    public void save(String filename) throws IOException {
        if ("all".equals(filename)) {
            for (String f : fileMap.keySet()) {
                saveFile(f);
            }
        } else {
            String target = (filename == null) ? currentFile : filename;
            if (target != null) {
                saveFile(target);
            }
        }
    }

    private void saveFile(String filename) throws IOException {
        Editor editor = fileMap.get(filename);
        if (editor == null)
            return;
        Files.writeString(Paths.get(filename), editor.getContent(), StandardCharsets.UTF_8);
        editor.setModified(false);
        System.out.println("已保存: " + filename);
    }

    public void close(String filename) {
        String target = (filename == null) ? currentFile : filename;
        if (target == null || !fileMap.containsKey(target)) {
            return;
        }

        // 停止统计
        statistics.onFileDeactivated(target);

        // 简单处理：如果未保存，实际应在Main里询问，这里直接关
        fileMap.remove(target);
        if (target.equals(currentFile)) {
            String newCurrent = fileMap.isEmpty() ? null : fileMap.keySet().iterator().next();
            currentFile = newCurrent;

            // 切换到新的当前文件时开始计时
            if (newCurrent != null) {
                statistics.onFileActivated(newCurrent);
            }
        }
        System.out.println("已关闭: " + target);
    }

    // --- 显示类命令 ---

    public void showEditorList() {
        int index = 1;
        for (Map.Entry<String, Editor> entry : fileMap.entrySet()) {
            String filename = entry.getKey();
            String mark = filename.equals(currentFile) ? "*" : "";
            String mod = entry.getValue().isModified() ? " [modified]" : "";
            String duration = " (" + statistics.getFormattedDuration(filename) + ")";

            System.out.println(index++ + " " + mark + " " + filename + mod + duration);
        }
    }

    public void printDirTree(String pathStr) {
        Path start = pathStr == null ? Paths.get(".") : Paths.get(pathStr);
        try {
            // 忽略隐藏文件
            Files.walk(start)
                    .filter(p -> !p.getFileName().toString().startsWith("."))
                    .forEach(p -> {
                        int depth = p.getNameCount() - start.getNameCount();
                        String prefix = (depth > 0) ? "  ".repeat(depth) + "|- " : "";
                        System.out.println(prefix + p.getFileName());
                    });
        } catch (IOException e) {
            System.out.println("无法读取目录: " + e.getMessage());
        }
    }

    // --- 状态持久化 (Memento模式简化版) [cite: 16, 21] ---

    public void saveWorkspaceState() {
        List<String> lines = new ArrayList<>();
        lines.add("LogEnabled=" + logEnabled);
        lines.add("Current=" + (currentFile == null ? "null" : currentFile));
        for (Map.Entry<String, Editor> entry : fileMap.entrySet()) {
            // 格式: Filename|IsModified
            lines.add(entry.getKey() + "|" + entry.getValue().isModified());
        }
        try {
            Files.write(Paths.get(WORKSPACE_FILE), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("保存工作区状态失败");
        }
    }

    // --- Getters/Setters ---

    /**
     * 获取当前活动编辑器（多态）
     */
    public Editor getActiveEditor() {
        return currentFile == null ? null : fileMap.get(currentFile);
    }

    /**
     * 获取当前活动的文本编辑器（类型安全）
     */
    public TextEditor getActiveTextEditor() {
        Editor editor = getActiveEditor();
        if (editor instanceof TextEditor) {
            return (TextEditor) editor;
        }
        return null;
    }

    /**
     * 获取当前活动的 XML 编辑器（类型安全）
     */
    public XmlEditor getActiveXmlEditor() {
        Editor editor = getActiveEditor();
        if (editor instanceof XmlEditor) {
            return (XmlEditor) editor;
        }
        return null;
    }

    /**
     * 检查当前文件是否为 XML 文件
     */
    public boolean isCurrentFileXml() {
        return currentFile != null && currentFile.endsWith(".xml");
    }

    public String getCurrentFilename() {
        return currentFile;
    }

    public boolean isLogEnabled() {
        return logEnabled;
    }

    public void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }

    public SessionStatistics getStatistics() {
        return statistics;
    }
}