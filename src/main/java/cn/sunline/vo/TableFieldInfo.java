package cn.sunline.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableFieldInfo {
    // Excel映射属性
    @ExcelProperty("序号")
    private String id;

    @ExcelProperty("系统模块")
    private String systemModule;

    @ExcelProperty("主题")
    private String subject;

    @ExcelProperty("表英文名")
    private String tableNameEn;

    @ExcelProperty("表中文名")
    private String tableNameCn;

    @ExcelProperty("字段英文名")
    private String fieldNameEn;

    @ExcelProperty("字段中文名")
    private String fieldNameCn;

    @ExcelProperty("主键")
    private String primaryKey;

    @ExcelProperty("分桶键")
    private String bucketKey;

    @ExcelProperty("是否不为空")
    private String notNull;

    @ExcelProperty("字段序号")
    private Integer fieldOrder;

    @ExcelProperty("字段类型")
    private String fieldType;

    @ExcelProperty("分区字段")
    private String partKey;

    @ExcelProperty("建表类型")
    private String tableCreationType;

    @ExcelProperty("外键")
    private String foreignKey;

    @ExcelProperty("是否代码字段")
    private String ifCodeField;

    @ExcelProperty("引用代码")
    private String referenceCode;

    @ExcelProperty("代码描述")
    private String codeDescription;

    @ExcelProperty("通用检核规则")
    private String checkRule;

    @ExcelProperty("敏感信息类型")
    private String sensitiveType;

    @ExcelProperty("上线时间")
    private String onlineTime;

    @ExcelProperty("来源系统")
    private String sourceSystem;

    @ExcelProperty("下游应用")
    private String downstreamApplications;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("更新日期")
    private String updateDate;

    @ExcelProperty("更新人")
    private String updatePerson;

    @ExcelProperty("来源字段英文名")
    private String sourceFieldNameEn;

}