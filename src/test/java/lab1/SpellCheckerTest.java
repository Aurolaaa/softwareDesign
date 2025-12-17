package lab1;

import lab1.utils.MockSpellCheckerAdapter;
import lab1.utils.SpellChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 拼写检查功能测试
 */
class SpellCheckerTest {

    private SpellChecker checker;

    @BeforeEach
    void setUp() {
        checker = new MockSpellCheckerAdapter();
    }

    // --- 基础功能测试 ---

    @Test
    void testCheckSingleError() {
        String text = "I will recieve the package.";
        List<SpellChecker.SpellingError> errors = checker.check(text);

        assertEquals(1, errors.size());
        assertEquals("recieve", errors.get(0).getWord());
        assertTrue(errors.get(0).getSuggestions().contains("receive"));
    }

    @Test
    void testCheckMultipleErrors() {
        String text = "The goverment announced teh decision.";
        List<SpellChecker.SpellingError> errors = checker.check(text);

        assertEquals(2, errors.size());
    }

    @Test
    void testCheckNoErrors() {
        String text = "This is a correct sentence.";
        List<SpellChecker.SpellingError> errors = checker.check(text);

        assertEquals(0, errors.size());
    }

    @Test
    void testCheckEmptyString() {
        List<SpellChecker.SpellingError> errors = checker.check("");
        assertEquals(0, errors.size());
    }

    @Test
    void testCheckNullString() {
        List<SpellChecker.SpellingError> errors = checker.check(null);
        assertEquals(0, errors.size());
    }

    // --- 常见错误检测测试 ---

    @Test
    void testCommonError_Recieve() {
        assertHasError("recieve", "receive");
    }

    @Test
    void testCommonError_Occured() {
        assertHasError("occured", "occurred");
    }

    @Test
    void testCommonError_Seperate() {
        assertHasError("seperate", "separate");
    }

    @Test
    void testCommonError_Definately() {
        assertHasError("definately", "definitely");
    }

    @Test
    void testCommonError_Goverment() {
        assertHasError("goverment", "government");
    }

    @Test
    void testCommonError_Untill() {
        assertHasError("untill", "until");
    }

    // --- 大小写测试 ---

    @Test
    void testCaseInsensitive() {
        String text = "I will Recieve the package.";
        List<SpellChecker.SpellingError> errors = checker.check(text);

        assertEquals(1, errors.size());
        assertEquals("Recieve", errors.get(0).getWord());
    }

    @Test
    void testUpperCase() {
        String text = "RECIEVE";
        List<SpellChecker.SpellingError> errors = checker.check(text);

        assertEquals(1, errors.size());
    }

    // --- 位置信息测试 ---

    @Test
    void testErrorPosition() {
        String text = "I will recieve the package.";
        List<SpellChecker.SpellingError> errors = checker.check(text);

        assertEquals(1, errors.size());
        SpellChecker.SpellingError error = errors.get(0);
        assertEquals(7, error.getPosition(), "recieve 从位置 7 开始");
    }

    @Test
    void testMultipleErrorPositions() {
        String text = "teh goverment";
        List<SpellChecker.SpellingError> errors = checker.check(text);

        assertEquals(2, errors.size());
        assertTrue(errors.get(0).getPosition() < errors.get(1).getPosition());
    }

    // --- 建议测试 ---

    @Test
    void testSuggestions() {
        String text = "recieve";
        List<SpellChecker.SpellingError> errors = checker.check(text);

        assertEquals(1, errors.size());
        List<String> suggestions = errors.get(0).getSuggestions();
        assertFalse(suggestions.isEmpty());
        assertEquals("receive", suggestions.get(0));
    }

    // --- 边界测试 ---

    @Test
    void testOnlySpaces() {
        String text = "     ";
        List<SpellChecker.SpellingError> errors = checker.check(text);
        assertEquals(0, errors.size());
    }

    @Test
    void testSpecialCharacters() {
        String text = "Hello! How are you? I'm fine.";
        List<SpellChecker.SpellingError> errors = checker.check(text);
        assertEquals(0, errors.size());
    }

    @Test
    void testMixedContent() {
        String text = "This is recieve and goverment test.";
        List<SpellChecker.SpellingError> errors = checker.check(text);
        assertEquals(2, errors.size());
    }

    @Test
    void testNumbersIgnored() {
        String text = "Test 123 recieve 456";
        List<SpellChecker.SpellingError> errors = checker.check(text);
        assertEquals(1, errors.size());
        assertEquals("recieve", errors.get(0).getWord());
    }

    // --- XML 场景测试 ---

    @Test
    void testXmlTextOnly() {
        // 模拟 XML 文件中提取的文本内容
        String textFromXml = "Design Patterns Gang of Four";
        List<SpellChecker.SpellingError> errors = checker.check(textFromXml);
        assertEquals(0, errors.size());
    }

    @Test
    void testXmlTextWithErrors() {
        String textFromXml = "Recieve this goverment book";
        List<SpellChecker.SpellingError> errors = checker.check(textFromXml);
        assertEquals(2, errors.size());
    }

    // --- SpellingError 对象测试 ---

    @Test
    void testSpellingErrorToString() {
        String text = "recieve";
        List<SpellChecker.SpellingError> errors = checker.check(text);

        String errorString = errors.get(0).toString();
        assertTrue(errorString.contains("recieve"));
        assertTrue(errorString.contains("receive"));
    }

    // --- 辅助方法 ---

    private void assertHasError(String wrongWord, String correctWord) {
        List<SpellChecker.SpellingError> errors = checker.check(wrongWord);
        assertEquals(1, errors.size());
        assertEquals(wrongWord, errors.get(0).getWord());
        assertTrue(errors.get(0).getSuggestions().contains(correctWord));
    }
}
