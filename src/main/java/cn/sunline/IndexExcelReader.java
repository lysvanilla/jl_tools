package cn.sunline;

import cn.idev.excel.FastExcel;
import cn.sunline.vo.IndexInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class IndexExcelReader {
    public static void main(String[] args) {
        String filePath = "D:\\svn\\jilin\\02.需求分析\\风控中台指标信息_20250220.xlsx";
        List<IndexInfo> indexInfoList = IndexExcelReader.readExcel(filePath);
        // 过滤 applicationScenario 仅为“启用”和“未启用”的数据
        List<IndexInfo> filteredList = indexInfoList.stream()
                .filter(info -> "启用".equals(info.getIfEnabled()) || "未启用".equals(info.getIfEnabled()))
                .filter(info -> "指标".equals(info.getDesignClassification()))
                .collect(Collectors.toList());
       /* for (IndexInfo indexInfo : indexInfoList) {
            System.out.println(indexInfo);
        }*/
    }
    public static List<IndexInfo> readExcel(String filePath) {
        File file = new File(filePath);
        return FastExcel.read(file)
                .sheet("风控中台_指标清单")
                .head(IndexInfo.class)
                .doReadSync();
    }
}
