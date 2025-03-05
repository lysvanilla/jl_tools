package cn.sunline.vo;

import lombok.Data;

@Data
public class TranslationResult {
    // 存储翻译后的英文文本
    private String translatedText;
    // 存储拆分后的中文词
    private String splitWords;
    // 存储未匹配上的中文拆词
    private String unmatchedWords;

    public TranslationResult() {
    }

    public TranslationResult(String translatedText, String splitWords, String unmatchedWords) {
        this.translatedText = translatedText;
        this.splitWords = splitWords;
        this.unmatchedWords = unmatchedWords;
    }
}
