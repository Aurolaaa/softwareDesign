package lab1.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 会话统计模块 - 记录文件的编辑时长
 * 使用简化的 Observer 模式（直接调用而非事件机制）
 */
public class SessionStatistics {
    // 记录每个文件的总编辑时长（毫秒）
    private Map<String, Long> totalDuration = new HashMap<>();
    // 记录当前活动文件的开始时间
    private String currentFile;
    private long startTime;
    
    /**
     * 切换到新文件时调用（开始计时）
     */
    public void onFileActivated(String filename) {
        if (filename == null) {
            return;
        }
        
        // 如果之前有活动文件，先结束它的计时
        if (currentFile != null) {
            stopTiming();
        }
        
        // 开始新文件的计时
        currentFile = filename;
        startTime = System.currentTimeMillis();
    }
    
    /**
     * 关闭文件或切换文件时调用（停止计时）
     */
    public void onFileDeactivated(String filename) {
        if (filename != null && filename.equals(currentFile)) {
            stopTiming();
            currentFile = null;
        }
    }
    
    /**
     * 停止当前文件的计时
     */
    private void stopTiming() {
        if (currentFile != null && startTime > 0) {
            long duration = System.currentTimeMillis() - startTime;
            totalDuration.put(currentFile, totalDuration.getOrDefault(currentFile, 0L) + duration);
            startTime = 0;
        }
    }
    
    /**
     * 获取文件的总编辑时长（格式化字符串）
     */
    public String getFormattedDuration(String filename) {
        long totalMs = totalDuration.getOrDefault(filename, 0L);
        
        // 如果当前正在编辑此文件，加上当前会话的时长
        if (filename.equals(currentFile) && startTime > 0) {
            totalMs += System.currentTimeMillis() - startTime;
        }
        
        return formatDuration(totalMs);
    }
    
    /**
     * 格式化时长为可读字符串
     * 规则：
     * - >= 1小时：显示 "X小时Y分钟"
     * - >= 1分钟：显示 "X分钟"
     * - < 1分钟：显示 "X秒"
     */
    private String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        
        if (seconds < 60) {
            return seconds + "秒";
        }
        
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + "分钟";
        }
        
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        return hours + "小时" + remainingMinutes + "分钟";
    }
    
    /**
     * 重置文件的统计（用于 reload 文件时）
     */
    public void resetFile(String filename) {
        totalDuration.remove(filename);
        if (filename.equals(currentFile)) {
            startTime = System.currentTimeMillis();
        }
    }
    
    /**
     * 获取原始时长（毫秒，用于测试）
     */
    public long getRawDuration(String filename) {
        return totalDuration.getOrDefault(filename, 0L);
    }
}


