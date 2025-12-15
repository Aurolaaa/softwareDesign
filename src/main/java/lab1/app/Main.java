package lab1.app;

import lab1.command.*;
import lab1.command.xml.*;
import lab1.model.Workspace;
import lab1.model.XmlEditor;
import lab1.utils.Logger;
import lab1.utils.SpellChecker;
import lab1.utils.MockSpellCheckerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        Workspace workspace = new Workspace();
        CommandHistory history = new CommandHistory();
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Lab2 多功能编辑器 ===");
        System.out.println("支持功能: 文本编辑 | XML 编辑 | 拼写检查 | 统计模块");
        System.out.println("输入 'help' 查看命令，'exit' 退出。");
        System.out.println();

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }

            // 1. 解析参数 (处理引号)
            String[] parts = parseInput(input);
            String cmdName = parts[0];

            // 2. 预处理日志记录 (Observer逻辑) [cite: 61]
            // 如果日志开启且是编辑类命令，记录到文件
            if (workspace.isLogEnabled() && isEditCommand(cmdName)) {
                Logger.log(workspace.getCurrentFilename(), input);
            }

            try {
                switch (cmdName) {
                    case "help":
                        System.out.println("--- 命令列表 ---");
                        System.out.println(
                                "工作区: load <file>, save [file|all], init <text|xml> <file> [with-log], close [file]");
                        System.out.println("       editor-list, dir-tree [path], exit");
                        System.out.println("文本编辑: append \"text\", insert <line:col> \"text\"");
                        System.out.println("         delete <line:col> <len>, replace <line:col> <len> \"text\"");
                        System.out.println("         show [start:end], undo, redo");
                        System.out.println("XML编辑:  insert-before <tag> <newId> <targetId> [\"text\"]");
                        System.out.println("         append-child <tag> <newId> <parentId> [\"text\"]");
                        System.out.println("         edit-id <oldId> <newId>, edit-text <id> \"text\"");
                        System.out.println("         delete-element <id>, xml-tree");
                        System.out.println("日志:    log-on [file], log-off [file], log-show [file]");
                        System.out.println("拼写检查: spell-check [file]");
                        break;
                    case "exit":
                        workspace.saveWorkspaceState(); // 退出前保存状态 [cite: 176]
                        System.out.println("Bye.");
                        return;

                    // --- 工作区命令 ---
                    case "load":
                        workspace.load(parts[1]);
                        break;
                    case "save":
                        workspace.save(parts.length > 1 ? parts[1] : null);
                        break;
                    case "init":
                        // init <text|xml> <file> [with-log]
                        String editorType = parts[1];
                        String initFilename = parts[2];
                        boolean withLog = parts.length > 3 && "with-log".equals(parts[3]);
                        workspace.init(editorType, initFilename, withLog);
                        break;
                    case "close":
                        workspace.close(parts.length > 1 ? parts[1] : null);
                        break;
                    case "editor-list":
                        workspace.showEditorList();
                        break;
                    case "dir-tree":
                        workspace.printDirTree(parts.length > 1 ? parts[1] : null);
                        break;

                    // --- 历史记录命令 ---
                    case "undo":
                        history.undo();
                        break;
                    case "redo":
                        history.redo();
                        break;

                    // --- 日志命令 ---
                    case "log-on":
                        workspace.setLogEnabled(true);
                        System.out.println("日志已开启");
                        break;
                    case "log-off":
                        workspace.setLogEnabled(false);
                        System.out.println("日志已关闭");
                        break;
                    case "log-show":
                        Logger.showLog(parts.length > 1 ? parts[1] : workspace.getCurrentFilename());
                        break;

                    // --- 文本编辑命令 (需要活动文件) ---
                    case "append":
                    case "insert":
                    case "delete":
                    case "replace":
                    case "show":
                        if (workspace.getActiveEditor() == null) {
                            System.out.println("错误: 没有活动文件");
                            break;
                        }
                        handleEditCommand(cmdName, parts, workspace, history);
                        break;

                    // --- XML 编辑命令 ---
                    case "insert-before":
                    case "append-child":
                    case "edit-id":
                    case "edit-text":
                    case "delete-element":
                    case "xml-tree":
                        if (workspace.getActiveXmlEditor() == null) {
                            System.out.println("错误: 当前文件不是 XML 文件");
                            break;
                        }
                        handleXmlCommand(cmdName, parts, workspace, history);
                        break;

                    // --- 拼写检查命令 ---
                    case "spell-check":
                        handleSpellCheck(workspace);
                        break;

                    default:
                        System.out.println("未知命令: " + cmdName);
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // 处理编辑具体的逻辑
    private static void handleEditCommand(String cmd, String[] args, Workspace ws, CommandHistory hist) {
        try {
            // 确保当前文件是文本编辑器
            if (ws.getActiveTextEditor() == null) {
                System.out.println("错误: 当前文件不是文本文件");
                return;
            }

            switch (cmd) {

                case "append":
                    // append "text"
                    hist.execute(new AppendCommand(ws.getActiveTextEditor(), args[1]));
                    break;
                case "insert":
                    // insert line:col "text"
                    String[] posI = args[1].split(":");
                    hist.execute(new InsertCommand(ws.getActiveTextEditor(),
                            Integer.parseInt(posI[0]), Integer.parseInt(posI[1]), args[2]));
                    break;
                case "delete":
                    // delete line:col len
                    String[] posD = args[1].split(":");
                    hist.execute(new DeleteCommand(ws.getActiveTextEditor(),
                            Integer.parseInt(posD[0]), Integer.parseInt(posD[1]), Integer.parseInt(args[2])));
                    break;
                case "show":
                    // show or show start:end
                    List<String> lines = ws.getActiveTextEditor().getLines();
                    int start = 1, end = lines.size();
                    if (args.length > 1) {
                        String[] range = args[1].split(":");
                        start = Integer.parseInt(range[0]);
                        end = Integer.parseInt(range[1]);
                    }
                    for (int i = start; i <= end && i <= lines.size(); i++) {
                        System.out.println(i + ": " + lines.get(i - 1));
                    }
                    break;
            }
        } catch (Exception e) {
            System.out.println("命令参数错误: " + e.getMessage());
        }
    }

    // 处理拼写检查命令
    private static void handleSpellCheck(Workspace workspace) {
        if (workspace.getActiveEditor() == null) {
            System.out.println("错误: 没有活动文件");
            return;
        }

        SpellChecker checker = new MockSpellCheckerAdapter();
        String textToCheck = "";

        // 根据文件类型获取待检查的文本
        if (workspace.getActiveXmlEditor() != null) {
            // XML 文件：只检查元素的文本内容
            textToCheck = workspace.getActiveXmlEditor().getAllTextContent();
            System.out.println("正在检查 XML 文件的文本内容...");
        } else if (workspace.getActiveTextEditor() != null) {
            // 文本文件：检查全文
            textToCheck = workspace.getActiveTextEditor().getText();
            System.out.println("正在检查文本文件...");
        }

        // 执行拼写检查
        List<SpellChecker.SpellingError> errors = checker.check(textToCheck);

        // 输出结果
        if (errors.isEmpty()) {
            System.out.println("未发现拼写错误。");
        } else {
            System.out.println("发现 " + errors.size() + " 个拼写错误：");
            for (SpellChecker.SpellingError error : errors) {
                System.out.println("  " + error);
            }
        }
    }

    // 处理 XML 编辑命令
    private static void handleXmlCommand(String cmd, String[] args, Workspace ws, CommandHistory hist) {
        try {
            XmlEditor editor = ws.getActiveXmlEditor();

            switch (cmd) {
                case "insert-before":
                    // insert-before <tagName> <newId> <targetId> ["text"]
                    String text1 = args.length > 4 ? args[4] : null;
                    hist.execute(new InsertBeforeCommand(editor, args[1], args[2], args[3], text1));
                    break;

                case "append-child":
                    // append-child <tagName> <newId> <parentId> ["text"]
                    String text2 = args.length > 4 ? args[4] : null;
                    hist.execute(new AppendChildCommand(editor, args[1], args[2], args[3], text2));
                    break;

                case "edit-id":
                    // edit-id <oldId> <newId>
                    hist.execute(new EditIdCommand(editor, args[1], args[2]));
                    break;

                case "edit-text":
                    // edit-text <elementId> "text"
                    hist.execute(new EditTextCommand(editor, args[1], args[2]));
                    break;

                case "delete-element":
                    // delete-element <elementId>
                    hist.execute(new DeleteElementCommand(editor, args[1]));
                    break;

                case "xml-tree":
                    // xml-tree (显示树形结构，不需要 undo)
                    System.out.println("XML 树形结构:");
                    System.out.println(editor.getTreeString());
                    break;
            }
        } catch (Exception e) {
            System.out.println("XML 命令错误: " + e.getMessage());
        }
    }

    private static boolean isEditCommand(String cmd) {
        return cmd.equals("append") || cmd.equals("insert") || cmd.equals("delete")
                || cmd.equals("replace") || cmd.equals("save")
                || cmd.equals("insert-before") || cmd.equals("append-child")
                || cmd.equals("edit-id") || cmd.equals("edit-text") || cmd.equals("delete-element");
    }

    // 正则解析：保留引号内的空格
    private static String[] parseInput(String input) {
        List<String> list = new ArrayList<>();
        // 匹配：双引号内的内容 OR 非空白字符
        Matcher m = Pattern.compile("\"([^\"]*)\"|(\\S+)").matcher(input);
        while (m.find()) {
            // 引号内容
            if (m.group(1) != null) {
                list.add(m.group(1));
            }
            // 普通词
            else {
                list.add(m.group(2));
            }
        }
        return list.toArray(new String[0]);
    }
}