package cn.sunline.vo.etl;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtlGroupJoinInfo {
    @ExcelProperty("源表schema")
    private String sourceTableSchema;

    @ExcelProperty("源表英文名")
    private String sourceTableEnglishName;

    @ExcelProperty("源表中文名")
    private String sourceTableChineseName;

    @ExcelProperty("源表别名")
    private String sourceTableAlias;

    @ExcelProperty("关联类型")
    private String joinType;

    @ExcelProperty("关联条件")
    private String joinCondition;

    @ExcelProperty("注释")
    private String comment;

}
