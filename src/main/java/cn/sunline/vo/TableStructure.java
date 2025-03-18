package cn.sunline.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Data
@AllArgsConstructor
@ToString
public class TableStructure {
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
    @ExcelProperty("表级信息描述")
    private String description;
    @ExcelProperty("建表类型")
    private String tableCreationType;
    @ExcelProperty("算法类型")
    private String algorithmType;
    @ExcelProperty("是否存在主键")
    private String hasPrimaryKey;
    @ExcelProperty("分区方式")
    private String partitionMethod;
    @ExcelProperty("分桶数量")
    private String bucketCount;
    @ExcelProperty("重要程度")
    private String importanceLevel;
    @ExcelProperty("上线时间")
    private String onlineTime;
    @ExcelProperty("下游应用")
    private String downstreamApplications;
    @ExcelProperty("公开状态")
    private String publicStatus;
    @ExcelProperty("来源系统")
    private String sourceSystem;
    @ExcelProperty("来源表")
    private String sourceTableNameEn;
    @ExcelProperty("设计人员")
    private String designer;
    @ExcelProperty("状态")
    private String status;
    @ExcelProperty("更新日期")
    private String updateDate;
    @ExcelProperty("备注")
    private String remark;
    @ExcelProperty("更新人")
    private String updatePerson;

    // 非Excel映射属性
    private List<TableFieldInfo> fields;
    private LinkedHashMap<String, TableFieldInfo> fieldMap;
    private LinkedHashMap<String, TableFieldInfo> fieldCnMap;

    public TableStructure() {
        this.fieldMap = new LinkedHashMap<>();
        this.fieldCnMap = new LinkedHashMap<>();
    }

    public void addField(TableFieldInfo field) {
        if (fields == null) {
            fields = new ArrayList<>();
        }
        fields.add(field);
        fieldMap.put(field.getFieldNameEn(), field);
        fieldCnMap.put(field.getFieldNameCn(), field);
    }


}