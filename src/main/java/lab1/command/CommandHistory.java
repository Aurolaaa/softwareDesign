package lab1.command;

import java.util.Stack;

public class CommandHistory {
    // 撤销栈：存刚才做过的操作
    private Stack<Command> undoStack = new Stack<>();
    // 重做栈：存刚才撤销回去的操作
    private Stack<Command> redoStack = new Stack<>();

    // 执行新命令时调用这个
    public void execute(Command cmd) {
        cmd.execute();         // 让命令干活
        undoStack.push(cmd);   // 入栈记录
        redoStack.clear();     // 一旦有新操作，之前的重做历史就失效了
    }

    // 撤销
    public void undo() {
        if (!undoStack.isEmpty()) {
            Command cmd = undoStack.pop();
            cmd.undo();        // 执行反向操作
            redoStack.push(cmd); // 放入重做栈，万一你后悔撤销了呢
            System.out.println("已撤销");
        } else {
            System.out.println("没有可撤销的操作");
        }
    }

    // 重做
    public void redo() {
        if (!redoStack.isEmpty()) {
            Command cmd = redoStack.pop();
            cmd.execute();     // 再次执行
            undoStack.push(cmd); // 放回撤销栈
            System.out.println("已重做");
        } else {
            System.out.println("没有可重做的操作");
        }
    }
}