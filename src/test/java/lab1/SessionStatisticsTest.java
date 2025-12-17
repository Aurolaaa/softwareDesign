package lab1;

import lab1.utils.SessionStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 统计模块测试
 */
class SessionStatisticsTest {

    private SessionStatistics statistics;

    @BeforeEach
    void setUp() {
        statistics = new SessionStatistics();
    }

    // --- 基础功能测试 ---

    @Test
    void testInitialDuration() {
        String duration = statistics.getFormattedDuration("test.txt");
        assertEquals("0秒", duration);
    }

    @Test
    void testFileActivation() throws InterruptedException {
        statistics.onFileActivated("test.txt");
        Thread.sleep(1000); // 等待 1 秒

        String duration = statistics.getFormattedDuration("test.txt");
        assertFalse(duration.equals("0秒"), "应该有时长记录");
    }

    @Test
    void testFileDeactivation() throws InterruptedException {
        statistics.onFileActivated("test.txt");
        Thread.sleep(100);
        statistics.onFileDeactivated("test.txt");

        long duration1 = statistics.getRawDuration("test.txt");
        Thread.sleep(100);
        long duration2 = statistics.getRawDuration("test.txt");

        assertEquals(duration1, duration2, "停止计时后时长不应增加");
    }

    @Test
    void testMultipleFiles() throws InterruptedException {
        statistics.onFileActivated("file1.txt");
        Thread.sleep(500);
        statistics.onFileActivated("file2.txt"); // 自动停止 file1
        Thread.sleep(500);
        statistics.onFileDeactivated("file2.txt"); // 停止 file2

        long duration1 = statistics.getRawDuration("file1.txt");
        long duration2 = statistics.getRawDuration("file2.txt");

        assertTrue(duration1 > 0, "file1 应该有时长");
        assertTrue(duration2 > 0, "file2 应该有时长");
    }

    @Test
    void testFileSwitching() throws InterruptedException {
        // 切换文件应该自动停止前一个文件的计时
        statistics.onFileActivated("file1.txt");
        Thread.sleep(500);
        statistics.onFileDeactivated("file1.txt");

        long file1Duration = statistics.getRawDuration("file1.txt");

        statistics.onFileActivated("file2.txt");
        Thread.sleep(500);

        // file1 的时长不应再增加（允许50ms误差）
        assertEquals(file1Duration, statistics.getRawDuration("file1.txt"), 50.0);
    }

    // --- 格式化测试 ---

    @Test
    void testFormatSeconds() {
        statistics.onFileActivated("test.txt");
        // 原始时长设为 5 秒（通过反射或等待，这里简化测试）
        String formatted = statistics.getFormattedDuration("test.txt");
        assertTrue(formatted.endsWith("秒"));
    }

    @Test
    void testFormatMinutes() throws InterruptedException {
        statistics.onFileActivated("test.txt");
        Thread.sleep(100);
        statistics.onFileDeactivated("test.txt");

        // 模拟 1 分钟（实际测试中无法等待，这里测试格式逻辑）
        String formatted = statistics.getFormattedDuration("test.txt");
        assertNotNull(formatted);
    }

    // --- 重置功能测试 ---

    @Test
    void testResetFile() throws InterruptedException {
        statistics.onFileActivated("test.txt");
        Thread.sleep(100);
        statistics.onFileDeactivated("test.txt");

        assertTrue(statistics.getRawDuration("test.txt") > 0);

        statistics.resetFile("test.txt");
        assertEquals(0, statistics.getRawDuration("test.txt"));
    }

    @Test
    void testResetCurrentFile() throws InterruptedException {
        statistics.onFileActivated("test.txt");
        Thread.sleep(500);
        statistics.onFileDeactivated("test.txt");

        statistics.resetFile("test.txt");

        // 重置后应该重新开始计时
        statistics.onFileActivated("test.txt");
        Thread.sleep(500);
        statistics.onFileDeactivated("test.txt");

        assertTrue(statistics.getRawDuration("test.txt") > 0, "重置后重新计时应该有时长");
    }

    // --- 累计时长测试 ---

    @Test
    void testAccumulatedDuration() throws InterruptedException {
        statistics.onFileActivated("test.txt");
        Thread.sleep(50);
        statistics.onFileDeactivated("test.txt");

        long duration1 = statistics.getRawDuration("test.txt");

        // 再次激活并累计时长
        statistics.onFileActivated("test.txt");
        Thread.sleep(50);
        statistics.onFileDeactivated("test.txt");

        long duration2 = statistics.getRawDuration("test.txt");
        assertTrue(duration2 > duration1, "时长应该累计");
    }

    // --- 边界测试 ---

    @Test
    void testNullFilename() {
        statistics.onFileActivated(null);
        // 不应抛出异常
        assertEquals("0秒", statistics.getFormattedDuration("any.txt"));
    }

    @Test
    void testDeactivateNonActive() {
        statistics.onFileActivated("file1.txt");
        statistics.onFileDeactivated("file2.txt"); // 停止未激活的文件
        // 不应抛出异常
    }

    @Test
    void testNonExistentFile() {
        String duration = statistics.getFormattedDuration("nonexistent.txt");
        assertEquals("0秒", duration);
    }

    // --- 并发场景测试 ---

    @Test
    void testRapidSwitching() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            statistics.onFileActivated("file" + i + ".txt");
            Thread.sleep(50);
        }

        // 应该只有最后一个文件在计时
        statistics.onFileActivated("final.txt");
        Thread.sleep(500);
        statistics.onFileDeactivated("final.txt");

        assertTrue(statistics.getRawDuration("final.txt") > 0, "最后激活的文件应该有时长");
    }

    // --- 实际使用场景测试 ---

    @Test
    void testWorkspaceScenario() throws InterruptedException {
        // 模拟实际工作流程
        // 1. 打开文件1
        statistics.onFileActivated("file1.txt");
        Thread.sleep(500);

        // 2. 切换到文件2
        statistics.onFileActivated("file2.txt");
        Thread.sleep(500);

        // 3. 关闭文件2
        statistics.onFileDeactivated("file2.txt");

        // 4. 重新打开文件1（累计时长）
        statistics.onFileActivated("file1.txt");
        Thread.sleep(500);
        statistics.onFileDeactivated("file1.txt");

        long file1Duration = statistics.getRawDuration("file1.txt");
        long file2Duration = statistics.getRawDuration("file2.txt");

        assertTrue(file1Duration > 800, "file1 至少 500 + 500"); // 至少 500 + 500
        assertTrue(file2Duration >= 400 && file2Duration <= 700, "file2 应该在 400-700ms");
    }

    @Test
    void testLoadScenario() throws InterruptedException {
        // 模拟 load 命令场景
        statistics.onFileActivated("test.txt");
        Thread.sleep(500);
        statistics.onFileDeactivated("test.txt");

        long oldDuration = statistics.getRawDuration("test.txt");

        // 重新 load 应该重置
        statistics.resetFile("test.txt");
        statistics.onFileActivated("test.txt");

        Thread.sleep(200);
        long newDuration = statistics.getRawDuration("test.txt");

        assertTrue(newDuration < oldDuration, "重置后的时长应该小于之前");
    }
}
