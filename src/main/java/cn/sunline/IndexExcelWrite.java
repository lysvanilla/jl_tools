package cn.sunline;

import cn.hutool.core.io.FileUtil;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.FastExcel;
import cn.idev.excel.write.metadata.WriteSheet;
import cn.sunline.util.BasicInfo;
import cn.sunline.vo.IndexInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class IndexExcelWrite {
    private static final String tpl_path = BasicInfo.tpl_path+"excel"+File.separator+"指标清单模版.xlsx";
    private static final String basicExportPath = BasicInfo.getBasicExportPath("");
    public static void main(String[] args) {
        String sourceFilePath = "D:\\svn\\jilin\\02.需求分析\\0202.智能风控系统\\智能风控系统指标信息_20250304.xlsx";
        writeIndexExcel(sourceFilePath);

        System.out.println("数据写入成功！");
    }
    public static void writeIndexExcel(HashMap<String,String> args_map){
        String file_name=args_map.get("file_name");
        writeIndexExcel(file_name);
    }
    public static void writeIndexExcel(String filePath){
        if (!FileUtil.exist(filePath)){
            log.error("file_name参数对应的文件不存在,[{}]",filePath);
            System.exit(1);
        }
        // 读取源 Excel 文件
        List<IndexInfo> indexInfoList = IndexExcelReader.readExcel(filePath);

        // 过滤 applicationScenario 仅为“启用”和“未启用”的数据
        List<IndexInfo> filteredList = indexInfoList.stream()
                .filter(info -> "启用".equals(info.getIfEnabled()) || "未启用".equals(info.getIfEnabled()))
                .filter(info -> "指标".equals(info.getDesignClassification()))
                .peek(info -> info.setDataTimeliness("批量"))
                .collect(Collectors.toList());

        String fileName = FileUtil.mainName(filePath);
        String extFileName = FileUtil.extName(filePath);

        String outputFilePath = basicExportPath+fileName+"-自动转换"+BasicInfo.currentDate+"."+extFileName; // 将数据写入到 Excel 模板文件中
        IndexExcelWrite.writeIndexExcel(filteredList, tpl_path, outputFilePath);
    }

    public static void writeIndexExcel(List<IndexInfo> indexInfoList, String templatePath, String outputPath) {
        File templateFile = new File(templatePath);
        File outputFile = new File(outputPath);

        try(ExcelWriter excelWriter = FastExcel.write(outputPath).withTemplate(templatePath).build()){
            WriteSheet task_sheet = FastExcel.writerSheet("指标数据").build();
            excelWriter.fill(indexInfoList,task_sheet);
        }
        log.info("转换成功：[{}]",outputPath);
    }
}
