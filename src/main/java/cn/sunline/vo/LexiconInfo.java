package cn.sunline.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LexiconInfo {
    // 单词中文名称
    @ExcelProperty("单词中文名称")
    private String chineseName;
    // 单词英文简称
    @ExcelProperty("单词英文简称")
    private String englishAbbreviation;
    // 单词英文全称
    @ExcelProperty("单词英文全称")
    private String englishFullName;
    // 是否为数据元，使用布尔类型表示，true 表示是，false 表示否
    @ExcelProperty("是否数据元")
    private boolean isDataElement;
    // 是否为复合词，使用布尔类型表示，true 表示是，false 表示否
    @ExcelProperty("是否复合词")
    private boolean isCompoundWord;
    // 同义词列表，使用 List 存储多个同义词
    @ExcelProperty("同义词")
    private String synonyms;
}
