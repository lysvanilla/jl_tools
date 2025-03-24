package cn.sunline.table;

import cn.idev.excel.FastExcel;
import cn.sunline.util.BasicInfo;
import cn.sunline.vo.StandardizedMappingRelation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * StandardizedMappingRelationReader 类用于从指定的 Excel 文件中读取标准化映射关系信息。
 */
@Slf4j
public class StandardizedMappingRelationReader {
    private static final String MAPP_TPL_PATH = BasicInfo.TPL_PATH + "excel/标准化.xlsx";

    /**
     * 程序的入口方法，用于测试从 Excel 文件读取映射关系的功能。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        LinkedHashMap<String, StandardizedMappingRelation> mappingMap = readExcel();
        System.out.println("1");
    }

    public static LinkedHashMap<String, StandardizedMappingRelation> readExcel() {
        return readExcel(MAPP_TPL_PATH);
    }

    /**
     * 从指定的 Excel 文件中读取映射关系信息。
     *
     * @param filePath Excel 文件的路径
     * @return 包含字段英文名和对应映射关系的 LinkedHashMap
     */
    public static LinkedHashMap<String, StandardizedMappingRelation> readExcel(String filePath) {
        LinkedHashMap<String, StandardizedMappingRelation> mappingMap = new LinkedHashMap<>();
        
        // 检查文件路径是否为空
        if (filePath == null || filePath.isEmpty()) {
            log.error("传入的文件路径为空，无法读取 Excel 文件");
            return mappingMap;
        }

        // 创建文件对象并检查文件是否存在
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            log.error("指定的 Excel 文件不存在或不是一个有效的文件: {}", filePath);
            return mappingMap;
        }

        try {
            log.debug("开始从文件 [{}] 读取标准化映射关系信息", filePath);
            
            // 读取映射关系信息
            List<StandardizedMappingRelation> mappingRelations = FastExcel.read(file)
                    .sheet("标准化")  // 读取第一个工作表
                    .head(StandardizedMappingRelation.class)
                    .doReadSync();

            // 如果读取结果为空，初始化为空列表
            if (mappingRelations == null) {
                mappingRelations = new ArrayList<>();
            }
            
            log.info("成功读取到 [{}] 条标准化映射关系信息", mappingRelations.size());

            // 将映射关系信息放入 Map，使用字段英文名作为键
            for (StandardizedMappingRelation mapping : mappingRelations) {
                String sourceFieldEnglishName = mapping.getSourceFieldEnglishName();
                if (StringUtils.isNotBlank(sourceFieldEnglishName)) {
                    mappingMap.put(sourceFieldEnglishName, mapping);
                } else {
                    log.warn("跳过一条无效的标准化映射关系记录：字段英文名为空");
                }
            }

            log.info("成功将 [{}] 条标准化映射关系信息放入 Map", mappingMap.size());

        } catch (Exception e) {
            log.error("读取文件 [{}] 时出现异常，异常信息: {}", filePath, e.getMessage(), e);
        }

        return mappingMap;
    }
}
