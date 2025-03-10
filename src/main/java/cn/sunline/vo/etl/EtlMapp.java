package cn.sunline.vo.etl;

import cn.idev.excel.annotation.ExcelProperty;
import cn.sunline.vo.TableFieldInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtlMapp {
    @ExcelProperty("sheet名字")
    private String sheetName;

    @ExcelProperty("表英文名")
    private String tableEnglishName;

    @ExcelProperty("表中文名")
    private String tableChineseName;

    @ExcelProperty("主键字段")
    private String primaryKeyField;

    @ExcelProperty("分析人员")
    private String analyst;

    @ExcelProperty("归属层次")
    private String attributionLevel;

    @ExcelProperty("主要应用")
    private String mainApplication;

    @ExcelProperty("时间粒度")
    private String timeGranularity;

    @ExcelProperty("创建日期")
    private String creationDate;

    @ExcelProperty("归属主题")
    private String attributionTheme;

    @ExcelProperty("保留周期")
    private String retentionPeriod;

    @ExcelProperty("描述")
    private String description;

    @ExcelProperty("初始设置")
    private String initialSettings;

    @ExcelProperty("初始加载")
    private String initialLoad;

    @ExcelProperty("每日加载")
    private String dailyLoad;

    private List<EtlGroup> etlGroupList = new ArrayList<EtlGroup>();
    private List<EtlUpdateRecord> etlUpdateRecordList = new ArrayList<EtlUpdateRecord>();
    public void addEtlGroup(EtlGroup etlGroup) {
        if (etlGroupList == null) {
            etlGroupList = new ArrayList<>();
        }
        etlGroupList.add(etlGroup);
    }

    public void addEtlUpdateRecord(EtlUpdateRecord etlUpdateRecord) {
        if (etlUpdateRecordList == null) {
            etlUpdateRecordList = new ArrayList<>();
        }
        etlUpdateRecordList.add(etlUpdateRecord);
    }



}
