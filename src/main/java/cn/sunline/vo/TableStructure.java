package cn.sunline.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Data
public class TableStructure {
    // Excel映射属性
    @ExcelProperty("序号")
    private String id;

    @ExcelProperty("主题")
    private String subject;

    @ExcelProperty("表英文名")
    private String tableNameEn;

    @ExcelProperty("表中文名")
    private String tableNameCn;

    @ExcelProperty("描述信息")
    private String description;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("更新日期")
    private String updateDate;

    @ExcelProperty("备注")
    private String remark;

    // 非Excel映射属性
    private List<TableFieldInfo> fields;
    private LinkedHashMap<String, TableFieldInfo> fieldMap;

    public TableStructure() {
        this.fieldMap = new LinkedHashMap<>();
    }

    public void addField(TableFieldInfo field) {
        if (fields == null) {
            fields = new ArrayList<>();
        }
        fields.add(field);
        fieldMap.put(field.getFieldNameEn(), field);
    }


}