package cn.sunline.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class TranslationResultFull {
    // 中文原文
    @ExcelProperty("中文")
    private String chinese;

    // 向左翻译英文结果
    @ExcelProperty("向左翻译英文")
    private String leftTranslation;

    // 向左拆解词根结果
    @ExcelProperty("向左拆解词根")
    private String leftSplitWords;

    // 向左缺失的词根
    @ExcelProperty("向左缺词根")
    private String leftUnmatchedWords;

    // 向左是否缺词根
    @ExcelProperty("向左是否缺词根")
    private String isLeftUnmatchedWords;

    // 向右翻译英文结果
    @ExcelProperty("向右翻译英文")
    private String rightTranslation;

    // 向右拆解词根结果
    @ExcelProperty("向右拆解词根")
    private String rightSplitWords;

    // 向右缺失的词根
    @ExcelProperty("向右缺词根")
    private String rightUnmatchedWords;

    // 向右是否缺词根
    @ExcelProperty("向右是否缺词根")
    private String isRightUnmatchedWords;

    // 左右翻译结果是否相同
    @ExcelProperty("翻译是否相同")
    private String isTranslationSame;

    public TranslationResultFull() {
    }

    public TranslationResultFull(String chinese, String leftTranslation, String leftSplitWords, String leftUnmatchedWords, String isLeftUnmatchedWords, String rightTranslation, String rightSplitWords, String rightUnmatchedWords, String isRightUnmatchedWords, String isTranslationSame) {
        this.chinese = chinese;
        this.leftTranslation = leftTranslation;
        this.leftSplitWords = leftSplitWords;
        this.leftUnmatchedWords = leftUnmatchedWords;
        this.isLeftUnmatchedWords = isLeftUnmatchedWords;
        this.rightTranslation = rightTranslation;
        this.rightSplitWords = rightSplitWords;
        this.rightUnmatchedWords = rightUnmatchedWords;
        this.isRightUnmatchedWords = isRightUnmatchedWords;
        this.isTranslationSame = isTranslationSame;
    }
}