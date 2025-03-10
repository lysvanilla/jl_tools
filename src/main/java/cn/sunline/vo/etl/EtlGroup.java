package cn.sunline.vo.etl;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtlGroup {
    @ExcelProperty("分组编号")
    private String groupId;

    @ExcelProperty("目标表英文名")
    private String targetTableEnglishName;

    @ExcelProperty("目标表中文名")
    private String targetTableChineseName;

    @ExcelProperty("分组备注")
    private String groupRemarks;

    @ExcelProperty("模板类型")
    private String templateType;

    @ExcelProperty("分布键")
    private String distributionKey;

    @ExcelProperty("过滤条件")
    private String filterCondition;

    @ExcelProperty("分组条件")
    private String groupingCondition;

    @ExcelProperty("排序条件")
    private String sortingCondition;


    private List<EtlGroupColMapp> etlGroupColMappList = new ArrayList<EtlGroupColMapp>();


    private List<EtlGroupJoinInfo> etlGroupJoinInfoList = new ArrayList<EtlGroupJoinInfo>();

    public void addEtlGroupColMapp(EtlGroupColMapp etlGroupColMapp) {
        if (etlGroupColMappList == null) {
            etlGroupColMappList = new ArrayList<>();
        }
        etlGroupColMappList.add(etlGroupColMapp);
    }

    public void addEtlGroupJoinInfo(EtlGroupJoinInfo etlGroupJoinInfo) {
        if (etlGroupJoinInfoList == null) {
            etlGroupJoinInfoList = new ArrayList<>();
        }
        etlGroupJoinInfoList.add(etlGroupJoinInfo);
    }


}
