package lab1.utils;

import java.util.List;

/**
 * 拼写检查器接口
 * 使用 Adapter 模式隔离第三方库依赖
 */
public interface SpellChecker {
    /**
     * 检查文本的拼写错误
     * @param text 要检查的文本
     * @return 拼写错误列表
     */
    List<SpellingError> check(String text);
    
    /**
     * 拼写错误信息
     */
    class SpellingError {
        private String word;           // 错误的单词
        private List<String> suggestions;  // 建议的修正
        private int position;          // 错误位置
        
        public SpellingError(String word, List<String> suggestions, int position) {
            this.word = word;
            this.suggestions = suggestions;
            this.position = position;
        }
        
        public String getWord() {
            return word;
        }
        
        public List<String> getSuggestions() {
            return suggestions;
        }
        
        public int getPosition() {
            return position;
        }
        
        @Override
        public String toString() {
            return "错误单词: \"" + word + "\" (位置: " + position + "), 建议: " + suggestions;
        }
    }
}


