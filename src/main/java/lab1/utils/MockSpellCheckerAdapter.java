package lab1.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模拟拼写检查器适配器 - Adapter 模式
 * 实际项目中应该适配 LanguageTool 或其他拼写检查库
 * 这里使用硬编码的错误示例进行演示
 */
public class MockSpellCheckerAdapter implements SpellChecker {

    // 硬编码的常见拼写错误和建议
    private static final Map<String, List<String>> COMMON_ERRORS = new HashMap<>();

    static {
        COMMON_ERRORS.put("teh", Arrays.asList("the"));
        COMMON_ERRORS.put("recieve", Arrays.asList("receive"));
        COMMON_ERRORS.put("occured", Arrays.asList("occurred"));
        COMMON_ERRORS.put("seperate", Arrays.asList("separate"));
        COMMON_ERRORS.put("definately", Arrays.asList("definitely"));
        COMMON_ERRORS.put("wierd", Arrays.asList("weird"));
        COMMON_ERRORS.put("thier", Arrays.asList("their"));
        COMMON_ERRORS.put("goverment", Arrays.asList("government"));
        COMMON_ERRORS.put("untill", Arrays.asList("until"));
        COMMON_ERRORS.put("sucessful", Arrays.asList("successful"));
        COMMON_ERRORS.put("enviroment", Arrays.asList("environment"));
        COMMON_ERRORS.put("begining", Arrays.asList("beginning"));
        COMMON_ERRORS.put("occassion", Arrays.asList("occasion"));
        COMMON_ERRORS.put("accomodate", Arrays.asList("accommodate"));
        COMMON_ERRORS.put("neccessary", Arrays.asList("necessary"));
    }

    @Override
    public List<SpellingError> check(String text) {
        List<SpellingError> errors = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            return errors;
        }

        // 使用正则表达式提取单词
        Pattern wordPattern = Pattern.compile("\\b[a-zA-Z]+\\b");
        Matcher matcher = wordPattern.matcher(text);

        while (matcher.find()) {
            String word = matcher.group();
            String lowerWord = word.toLowerCase();

            // 检查是否是已知的拼写错误
            if (COMMON_ERRORS.containsKey(lowerWord)) {
                SpellingError error = new SpellingError(
                        word,
                        COMMON_ERRORS.get(lowerWord),
                        matcher.start());
                errors.add(error);
            }
        }

        return errors;
    }

    /**
     * 添加自定义错误（用于测试）
     */
    public static void addCustomError(String wrongWord, String... suggestions) {
        COMMON_ERRORS.put(wrongWord.toLowerCase(), Arrays.asList(suggestions));
    }
}
