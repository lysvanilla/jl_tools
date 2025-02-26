package cn.sunline.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class IndexInfo {
    @ExcelProperty("序号")
    private String serialNumber;
    @ExcelProperty("指标名称")
    private String indexName;
    @ExcelProperty("备注")
    private String remarks;
    @ExcelProperty("加工逻辑")
    private String processingLogic;
    @ExcelProperty("应用场景")
    private String applicationScenario;
    @ExcelProperty("是否启用")
    private String ifEnabled;
    @ExcelProperty("对应业务部门")
    private String correspondingBusinessDepartment;
    @ExcelProperty("联系人")
    private String contactPerson;
    @ExcelProperty("业务分类")
    private String businessClassification;
    @ExcelProperty("加工分类")
    private String processingClassification;
    @ExcelProperty("设计分类")
    private String designClassification;
    @ExcelProperty("指标框架")
    private String indexFramework;
    @ExcelProperty("框架一级分类")
    private String frameworkFirstLevelClassification;
    @ExcelProperty("框架二级分类")
    private String frameworkSecondLevelClassification;
    @ExcelProperty("客户类型")
    private String customerType;
    @ExcelProperty("指标度量")
    private String indexMeasurement;
    @ExcelProperty("标准指标名称")
    private String standardIndexName;
    // 新增标准指标别名属性
    @ExcelProperty("标准指标别名")
    private String standardIndexAlias;
    @ExcelProperty("指标业务定义")
    private String indexBusinessDefinition;
    @ExcelProperty("指标业务口径")
    private String indexBusinessCaliber;
    @ExcelProperty("指标维度")
    private String indexDimension;
    @ExcelProperty("统计单位")
    private String statisticalUnit;
    @ExcelProperty("归口部门")
    private String centralizedDepartment;
    @ExcelProperty("度量表")
    private String measurementTable;
    @ExcelProperty("维度表")
    private String dimensionTable;
    @ExcelProperty("计量层模型")
    private String measurementLayerModel;
    @ExcelProperty("汇报维度")
    private String reportingDimension;
    @ExcelProperty("计量模型覆盖标志")
    private boolean measurementModelCoverageFlag;
    @ExcelProperty("状态")
    private String status;
    @ExcelProperty("指标表中文名")
    private String indexTableChineseName;
    @ExcelProperty("指标字段中文名")
    private String indexFieldChineseName;
    @ExcelProperty("指标加工条件")
    private String indexProcessingCondition;
    @ExcelProperty("与业务沟通对齐状态")
    private String communicationAlignmentStatus;
    @ExcelProperty("更新频率")
    private String updateFrequency;
    @ExcelProperty("数据时效性")
    private String dataTimeliness;
    @ExcelProperty("预留字段")
    private String reservedField;

}