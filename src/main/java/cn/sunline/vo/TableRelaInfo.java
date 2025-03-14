package cn.sunline.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableRelaInfo {
    @ExcelProperty("表英文名")
    private String tableNameEn;
    @ExcelProperty("表中文名")
    private String tableNameCn;
    @ExcelProperty("依赖表")
    private String relatedTableName;

    @ExcelProperty("依赖表类型")
    private String relatedTableType;

    private LinkedHashSet<String> relatedTables;
    public void addRelatedTable(String relatedTable) {
        if (relatedTables == null){
            this.relatedTables = new LinkedHashSet<>();
        }
        this.relatedTables.add(relatedTable);
    }

    public void addRelatedTables(LinkedHashSet<String> relatedTables) {
        if (relatedTables == null){
            this.relatedTables = new LinkedHashSet<>();
        }
        this.relatedTables.addAll(relatedTables);
    }

    public TableRelaInfo(String tableNameEn, String tableNameCn) {
        this.tableNameEn = tableNameEn;
        this.tableNameCn = tableNameCn;
    }

    public TableRelaInfo(String tableNameEn, String tableNameCn, String relatedTableName) {
        this.tableNameEn = tableNameEn;
        this.tableNameCn = tableNameCn;
        this.relatedTableName = relatedTableName;
    }

    public TableRelaInfo(String tableNameEn, String tableNameCn, String relatedTableName, String relatedTableType) {
        this.tableNameEn = tableNameEn;
        this.tableNameCn = tableNameCn;
        this.relatedTableName = relatedTableName;
        this.relatedTableType = relatedTableType;
    }
}
