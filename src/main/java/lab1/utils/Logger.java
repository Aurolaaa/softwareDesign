package lab1.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    private static boolean sessionStarted = false;

    public static void log(String filename, String command) {
        if (filename == null) {return;}
        String logFile = "." + filename + ".log";
        String timestamp = sdf.format(new Date());

        StringBuilder sb = new StringBuilder();
        if (!sessionStarted) {
            sb.append("session start at ").append(timestamp).append("\n");
            sessionStarted = true;
        }
        sb.append(timestamp).append(" ").append(command).append("\n");

        try {
            Files.writeString(Paths.get(logFile), sb.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("日志写入失败 (忽略): " + e.getMessage());
        }
    }

    public static void showLog(String filename) {
        String logFile = "." + filename + ".log";
        try {
            if (Files.exists(Paths.get(logFile))) {
                System.out.println(Files.readString(Paths.get(logFile)));
            } else {
                System.out.println("该文件暂无日志。");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}