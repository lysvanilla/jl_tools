package cn.sunline;

import cn.idev.excel.ExcelWriter;
import cn.idev.excel.FastExcel;
import cn.idev.excel.write.metadata.WriteSheet;
import cn.sunline.vo.IndexInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class IndexExcelWrite {
    public static void main(String[] args) {
        String sourceFilePath = "D:\\svn\\jilin\\02.需求分析\\风控中台指标信息_20250220.xlsx";
        String templateFilePath = "D:\\projects\\jl_bank_tool\\template\\指标清单模版.xlsx";
        String outputFilePath = "D:\\svn\\jilin\\02.需求分析\\风险数据集市指标清单-自动转换.xlsx";

        // 读取源 Excel 文件
        List<IndexInfo> indexInfoList = IndexExcelReader.readExcel(sourceFilePath);

        // 过滤 applicationScenario 仅为“启用”和“未启用”的数据
        List<IndexInfo> filteredList = indexInfoList.stream()
                .filter(info -> "启用".equals(info.getIfEnabled()) || "未启用".equals(info.getIfEnabled()))
                .filter(info -> "指标".equals(info.getDesignClassification()))
                .peek(info -> info.setDataTimeliness("批量"))
                .collect(Collectors.toList());

        // 将数据写入到 Excel 模板文件中
        IndexExcelWrite.writeExcel(filteredList, templateFilePath, outputFilePath);

        System.out.println("数据写入成功！");
    }

    public static void writeExcel(List<IndexInfo> indexInfoList, String templatePath, String outputPath) {
        File templateFile = new File(templatePath);
        File outputFile = new File(outputPath);

        try(ExcelWriter excelWriter = FastExcel.write(outputPath).withTemplate(templatePath).build()){
            WriteSheet task_sheet = FastExcel.writerSheet("指标数据").build();
            excelWriter.fill(indexInfoList,task_sheet);
        }

    }
}
