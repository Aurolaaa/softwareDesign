package lab1.app;

import lab1.command.*;
import lab1.model.Workspace;
import lab1.utils.Logger;

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

        System.out.println("Lab1 编辑器启动。输入 'help' 查看命令，'exit' 退出。");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {continue;}

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
                        System.out.println("工作区: load <file>, save [file|all], init <file> [with-log], close [file]");
                        System.out.println("       editor-list, dir-tree [path], exit");
                        System.out.println("编辑:   append \"text\", insert <line:col> \"text\"");
                        System.out.println("       delete <line:col> <len>, replace <line:col> <len> \"text\"");
                        System.out.println("       show [start:end], undo, redo");
                        System.out.println("日志:   log-on [file], log-off [file], log-show [file]");
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
                        boolean withLog = parts.length > 2 && "with-log".equals(parts[2]);
                        workspace.init(parts[1], withLog);
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

                    // --- 编辑命令 (需要活动文件) ---
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
            switch (cmd) {

                case "append":
                    // append "text"
                    hist.execute(new AppendCommand(ws.getActiveEditor(), args[1]));
                    break;
                case "insert":
                    // insert line:col "text"
                    String[] posI = args[1].split(":");
                    hist.execute(new InsertCommand(ws.getActiveEditor(),
                            Integer.parseInt(posI[0]), Integer.parseInt(posI[1]), args[2]));
                    break;
                case "delete":
                    // delete line:col len
                    String[] posD = args[1].split(":");
                    hist.execute(new DeleteCommand(ws.getActiveEditor(),
                            Integer.parseInt(posD[0]), Integer.parseInt(posD[1]), Integer.parseInt(args[2])));
                    break;
                case "show":
                    // show or show start:end
                    List<String> lines = ws.getActiveEditor().getLines();
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

    private static boolean isEditCommand(String cmd) {
        return cmd.equals("append") || cmd.equals("insert") || cmd.equals("delete")
                || cmd.equals("replace") || cmd.equals("save");
    }

    // 正则解析：保留引号内的空格
    private static String[] parseInput(String input) {
        List<String> list = new ArrayList<>();
        // 匹配：双引号内的内容 OR 非空白字符
        Matcher m = Pattern.compile("\"([^\"]*)\"|(\\S+)").matcher(input);
        while (m.find()) {
            // 引号内容
            if (m.group(1) != null) {list.add(m.group(1)); }
            // 普通词
            else {list.add(m.group(2));}
        }
        return list.toArray(new String[0]);
    }
}