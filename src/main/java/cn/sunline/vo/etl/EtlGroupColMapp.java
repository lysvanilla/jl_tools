package cn.sunline.vo.etl;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtlGroupColMapp {
    @ExcelProperty("源表schema")
    private String sourceTableSchema;

    @ExcelProperty("源表英文名")
    private String sourceTableEnglishName;

    @ExcelProperty("源表中文名")
    private String sourceTableChineseName;

    @ExcelProperty("源字段英文名")
    private String sourceFieldEnglishName;

    @ExcelProperty("源字段中文名")
    private String sourceFieldChineseName;

    @ExcelProperty("源系统代码")
    private String sourceSystemCode;

    @ExcelProperty("源字段类型")
    private String sourceFieldType;

    @ExcelProperty("目标表英文名")
    private String targetTableEnglishName;

    @ExcelProperty("目标表中文名")
    private String targetTableChineseName;

    @ExcelProperty("目标表字段英文名")
    private String targetFieldEnglishName;

    @ExcelProperty("目标表字段中文名")
    private String targetFieldChineseName;

    @ExcelProperty("目标表字段类型")
    private String targetFieldType;

    @ExcelProperty("映射规则")
    private String mappingRule;

    @ExcelProperty("备注")
    private String remarks;

}
