package cn.sunline.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandardizedMappingRelation {
    @ExcelProperty("源表中文名")
    private String sourceTableChineseName;

    @ExcelProperty("源表英文名")
    private String sourceTableEnglishName;

    @ExcelProperty("源字段中文名")
    private String sourceFieldChineseName;

    @ExcelProperty("源字段英文名")
    private String sourceFieldEnglishName;

    @ExcelProperty("源字段类型")
    private String sourceFieldType;

    @ExcelProperty("字段中文名")
    private String fieldChineseName;

    @ExcelProperty("字段英文名")
    private String fieldEnglishName;

    @ExcelProperty("字段类型")
    private String fieldType;

    @ExcelProperty("变更内容")
    private String changeContent;
}
