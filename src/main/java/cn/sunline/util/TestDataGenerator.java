package cn.sunline.util;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.sunline.table.ExcelTableStructureReader;
import cn.sunline.vo.TableFieldInfo;
import cn.sunline.vo.TableStructure;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;

/**
 * 测试数据生成器，用于生成表的模拟数据INSERT语句
 */
@Slf4j
public class TestDataGenerator {
    public static final String base_export_path = BasicInfo.getBasicExportPath("autocode_insert_data");

    /**
     * 常用中文姓氏
     */
    private static final String[] FAMILY_NAMES = {
        "李", "王", "张", "刘", "陈", "杨", "黄", "赵", "周", "吴",
        "徐", "孙", "胡", "朱", "高", "林", "何", "郭", "马", "罗",
        "梁", "宋", "郑", "谢", "韩", "唐", "冯", "于", "董", "萧",
        "程", "曹", "袁", "邓", "许", "傅", "沈", "曾", "彭", "吕"
    };

    /**
     * 常用中文名字字符
     */
    private static final String[] NAME_CHARS = {
        "伟", "芳", "娜", "秀英", "敏", "静", "丽", "强", "磊", "军",
        "洋", "勇", "艳", "杰", "娟", "涛", "明", "超", "秀兰", "霞",
        "平", "刚", "桂英", "春", "晶", "智", "云", "志强", "建国", "建华",
        "建军", "建平", "建设", "国强", "国庆", "国华", "国平", "国安", "天明", "天昊"
    };

    /**
     * 期限单位代码数组
     */
    private static final String[] TERM_UNITS = {"D", "M", "Y"};

    /**
     * 机构ID列表
     */
    private static final String[] ORG_IDS = {"10010", "20010", "30010", "40010", "510010", "60010"};

    /**
     * 渠道列表
     */
    private static final String[] CHANNELS = {
        "手机银行", "网银", "柜面", "ATM", "微信银行", "电话银行"
    };

    /**
     * 为每个表生成指定数量的INSERT语句
     */
    public static List<String> generateInsertStatements(LinkedHashMap<String, TableStructure> tableMap, int recordCount) {
        List<String> sqlStatements = new ArrayList<>();
        
        for (Map.Entry<String, TableStructure> entry : tableMap.entrySet()) {
            String tableName = entry.getKey();
            TableStructure table = entry.getValue();
            
            log.info("开始为表 [{}] 生成测试数据", tableName);
            
            // 先生成DELETE语句
            String deleteStmt = generateDeleteStatement(table);
            sqlStatements.add(deleteStmt);
            sqlStatements.add(""); // 添加一个空行，提高可读性
            
            // 再生成INSERT语句
            for (int i = 0; i < recordCount; i++) {
                String insertStmt = generateSingleInsert(table);
                sqlStatements.add(insertStmt);
            }
            
            sqlStatements.add(""); // 在不同表的语句之间添加空行
        }
        
        return sqlStatements;
    }

    /**
     * 生成DELETE语句
     */
    private static String generateDeleteStatement(TableStructure table) {
        String deleteStmt = String.format("-- DELETE FROM %s;", table.getTableNameEn());
        log.debug("生成删除语句: {}", deleteStmt);
        return deleteStmt;
    }

    /**
     * 判断字段是否为日期字段
     */
    private static boolean isDateField(String fieldNameEn, String fieldNameCn) {
        if (fieldNameEn == null || fieldNameCn == null) {
            return false;
        }
        
        fieldNameEn = fieldNameEn.toUpperCase();
        fieldNameCn = fieldNameCn.trim();
        
        // 检查英文名
        boolean isDateEn = (fieldNameEn.contains("DATE") ||
                         // fieldNameEn.contains("TIME") ||
                          fieldNameEn.contains("DT")) &&  !fieldNameEn.contains("ETL_TIME");
                          
        // 检查中文名
        boolean isDateCn = (fieldNameCn.contains("日期") ||
                          fieldNameCn.contains("时间") ||
                          fieldNameCn.contains("时点"))&&  !fieldNameCn.contains("ETL加工时间");
                          
        return isDateEn || isDateCn;
    }

    /**
     * 为单个表生成一条INSERT语句
     */
    private static String generateSingleInsert(TableStructure table) {
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        
        List<TableFieldInfo> fields = table.getFields();
        boolean first = true;

        // 先收集所有日期字段
        Map<String, TableFieldInfo> dateFields = new HashMap<>();
        for (TableFieldInfo field : fields) {
            if (isDateField(field.getFieldNameEn(), field.getFieldNameCn())) {
                dateFields.put(field.getFieldNameEn().toUpperCase(), field);
            }
        }

        // 预先生成本条记录要使用的值
        Map<String, String> dateValues = generateRelatedDates(dateFields);
        String orgIdForThisRecord = ORG_IDS[RandomUtil.randomInt(ORG_IDS.length)];
        String channelForThisRecord = CHANNELS[RandomUtil.randomInt(CHANNELS.length)];
        
        // 生成INSERT语句
        for (TableFieldInfo field : fields) {
            if (!first) {
                columns.append(", ");
                values.append(", ");
            }
            
            columns.append(field.getFieldNameEn());
            
            // 根据字段类型选择对应的值
            if (dateValues.containsKey(field.getFieldNameEn().toUpperCase())) {
                values.append(dateValues.get(field.getFieldNameEn().toUpperCase()));
            } else if (isOrgIdField(field.getFieldNameEn(), field.getFieldNameCn())) {
                values.append("'").append(orgIdForThisRecord).append("'");
            } else if (isChannelField(field.getFieldNameEn(), field.getFieldNameCn())) {
                values.append("'").append(channelForThisRecord).append("'");
                log.debug("字段 {}({}) 使用渠道值: {}", field.getFieldNameCn(), field.getFieldNameEn(), channelForThisRecord);
            } else {
                values.append(generateFieldValue(field));
            }
            
            first = false;
        }
        
        return String.format("INSERT INTO %s (%s) VALUES (%s);",
                table.getTableNameEn(),
                columns.toString(),
                values.toString());
    }

    /**
     * 判断是否为机构ID字段
     */
    private static boolean isOrgIdField(String fieldNameEn, String fieldNameCn) {
        if (fieldNameEn == null || fieldNameCn == null) {
            return false;
        }
        
        fieldNameEn = fieldNameEn.toUpperCase();
        fieldNameCn = fieldNameCn.trim();
        
        return fieldNameEn.contains("ORG_ID") || 
               fieldNameCn.contains("机构标识") ||
               fieldNameCn.contains("机构ID") ||
               fieldNameCn.contains("机构编号");
    }

    /**
     * 判断是否为渠道字段
     */
    private static boolean isChannelField(String fieldNameEn, String fieldNameCn) {
        if (fieldNameEn == null || fieldNameCn == null) {
            return false;
        }
        
        fieldNameEn = fieldNameEn.toUpperCase();
        fieldNameCn = fieldNameCn.trim();
        
        return fieldNameEn.contains("CHAN") || 
               fieldNameCn.contains("渠道") ||
               fieldNameCn.contains("通道");
    }

    /**
     * 生成相关联的日期值
     */
    private static Map<String, String> generateRelatedDates(Map<String, TableFieldInfo> dateFields) {
        Map<String, String> dateValues = new HashMap<>();
        if (dateFields.isEmpty()) {
            return dateValues;
        }

        // 生成基准日期范围
        Date now = new Date();
        // 生成开户日期（最早）
        Date openDate = RandomUtil.randomDate(now, DateField.MONTH, -24, -12);
        
        // 生成失效日期（最晚）
        Date expDate = RandomUtil.randomDate(now, DateField.MONTH, 12, 24);
        
        // 处理每个日期字段
        for (Map.Entry<String, TableFieldInfo> entry : dateFields.entrySet()) {
            String fieldNameEn = entry.getKey();
            String fieldNameCn = entry.getValue().getFieldNameCn();
            String dateValue;

            if (fieldNameEn.contains("DATA_DATE")) {
                // DATA_DATE字段使用固定值
                //dateValue = "'20241231'";
                dateValue = "'20250102'";
                log.debug("生成DATA_DATE固定日期: {} for {}", dateValue, fieldNameCn);
            } else if ((fieldNameEn.contains("OPEN_DATE") || fieldNameCn.contains("开户日期")) ||
                (fieldNameEn.contains("EFF_DATE") || fieldNameCn.contains("生效日期"))) {
                // 开户日期和生效日期相同且最早
                dateValue = formatDateValue(openDate);
                log.debug("生成开户/生效日期: {} for {}", dateValue, fieldNameCn);
            } else if (fieldNameEn.contains("EXP_DATE") || fieldNameCn.contains("失效日期")) {
                // 失效日期最晚
                dateValue = formatDateValue(expDate);
                log.debug("生成失效日期: {} for {}", dateValue, fieldNameCn);
            } else {
                // 其他日期在开户日期和失效日期之间
                Date middleDate = RandomUtil.randomDate(now, DateField.MONTH, -12, 12);
                dateValue = formatDateValue(middleDate);
                log.debug("生成其他日期: {} for {}", dateValue, fieldNameCn);
            }

            dateValues.put(fieldNameEn, dateValue);
        }

        return dateValues;
    }

    /**
     * 格式化日期值
     */
    private static String formatDateValue(Date date) {
        return String.format("'%s'", DateUtil.format(date, "yyyyMMdd"));
    }

    /**
     * 根据字段类型生成对应的值
     */
    private static String generateFieldValue(TableFieldInfo field) {
        String fieldType = field.getFieldType().toUpperCase();
        
        // 处理数值类型
        if (fieldType.startsWith("INT") || 
            fieldType.startsWith("NUMERIC") || 
            fieldType.startsWith("NUMBER") || 
            fieldType.startsWith("DECIMAL")) {
            return generateNumberValue(field);
        }
        
        // 处理字符串类型
        return "'" + generateStringValue(field) + "'";
    }

    /**
     * 生成中文姓名
     */
    private static String generateChineseName() {
        // 随机获取一个姓氏
        String familyName = FAMILY_NAMES[RandomUtil.randomInt(FAMILY_NAMES.length)];
        // 随机获取一个名字
        String name = NAME_CHARS[RandomUtil.randomInt(NAME_CHARS.length)];
        return familyName + name;
    }

    /**
     * 生成字符串类型的模拟数据
     */
    private static String generateStringValue(TableFieldInfo field) {
        String fieldName = field.getFieldNameEn().toUpperCase();
        String fieldNameCn = field.getFieldNameCn();
        String fieldType = field.getFieldType().toUpperCase();
        
        // 解析字段长度
        int maxLength = parseFieldLength(fieldType);
        
        // 特殊字段处理
        if (fieldName.contains("SUBJ_DIRCT_CD") || 
            (fieldNameCn != null && fieldNameCn.contains("科目方向代码"))) {
            return RandomUtil.randomBoolean() ? "C" : "D";
        } else if (fieldName.contains("IS_NO_VIR_ACCT")) {
            // DATA_DATE字段使用固定值
            //dateValue = "'20241231'";
            return RandomUtil.randomBoolean() ? "Y" : "N";
        } else if (fieldName.contains("CURR_CD") ||
                (fieldNameCn != null && fieldNameCn.contains("币种代码"))) {
            return RandomUtil.randomBoolean() ? "CNY" : "USD";
        } else if (fieldNameCn != null && fieldNameCn.contains("标志")) {
            return RandomUtil.randomBoolean() ? "Y" : "N";
        } else if (fieldName.contains("PROD_CD")) {
            return String.format("%03d", RandomUtil.randomInt(0, 999));
        } else if (fieldName.contains("SUBJ_NO")) {
            return String.format("%04d", RandomUtil.randomInt(0, 10000));
        } else if (fieldName.contains("CUST_TYPE_CD")) {
            return RandomUtil.randomBoolean() ? "Y" : "N";
        } else if (fieldName.contains("ETL_TIME")) {
            Date randomDate = RandomUtil.randomDate(new Date(), DateField.MONTH, -12, 0);
            return DateUtil.format(randomDate, "yyyyMMddHHmmss");
        } else if (fieldName.contains("NAME")) {
            return limitLength(generateChineseName(), maxLength);
        } else if (fieldName.contains("CODE")) {
            String prefix = "CODE_";
            return limitLength(prefix + RandomUtil.randomString(6), maxLength);
        } else if (fieldName.contains("PHONE")) {
            return limitLength("1" + RandomUtil.randomNumbers(10), maxLength);
        } else if (fieldName.contains("EMAIL")) {
            String email = RandomUtil.randomString(8) + "@example.com";
            return limitLength(email, maxLength);
        } else if (fieldName.contains("ID")) {
            String id = "ID_" + RandomUtil.randomString(8);
            return limitLength(id, maxLength);
        }
        
        // 默认字符串处理
        return limitLength("VAL_" + RandomUtil.randomString(8), maxLength);
    }

    /**
     * 从字段类型中解析出长度信息
     * 例如：VARCHAR(32) -> 32
     */
    private static int parseFieldLength(String fieldType) {
        try {
            if (fieldType.contains("(") && fieldType.contains(")")) {
                String lengthStr = fieldType.substring(
                    fieldType.indexOf("(") + 1,
                    fieldType.indexOf(")")
                ).trim();
                // 如果是VARCHAR2(32 CHAR)这样的格式，只取数字部分
                lengthStr = lengthStr.split("\\s+")[0];
                return Integer.parseInt(lengthStr);
            }
        } catch (Exception e) {
            log.warn("解析字段长度失败: {}", fieldType);
        }
        // 默认返回255作为最大长度
        return 255;
    }

    /**
     * 限制字符串长度，如果超过最大长度则截断
     */
    private static String limitLength(String value, int maxLength) {
        if (value == null || maxLength <= 0) {
            return value;
        }
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }

    /**
     * 生成数值类型的模拟数据
     */
    private static String generateNumberValue(TableFieldInfo field) {
        String fieldName = field.getFieldNameEn().toUpperCase();
        String fieldType = field.getFieldType().toUpperCase();
        
        // 特殊处理账户编号
        if (fieldName.equals("ACCT_NO")) {
            // 生成8位数字的账户编号
            return String.valueOf(RandomUtil.randomInt(10000000, 99999999));
        }
        
        // 解析精度信息
        int[] precision = parseNumberPrecision(fieldType);
        int totalLength = precision[0];  // 总长度
        int decimalPlaces = precision[1];  // 小数位数
        int integerPlaces = totalLength - decimalPlaces;  // 整数位数
        
        // 计算整数部分的最大值
        long maxInteger = (long)Math.pow(10, integerPlaces) - 1;
        
        if (fieldType.startsWith("INT")) {
            // 整数类型特殊处理
            if (fieldType.equals("INT4") || fieldType.equals("INTEGER")) {
                return String.valueOf(RandomUtil.randomInt(0, Integer.MAX_VALUE));
            } else if (fieldType.equals("INT8") || fieldType.equals("BIGINT")) {
                return String.valueOf(RandomUtil.randomLong(0, Long.MAX_VALUE));
            } else if (fieldType.equals("INT2") || fieldType.equals("SMALLINT")) {
                return String.valueOf(RandomUtil.randomInt(0, Short.MAX_VALUE));
            }
        }
        
        double value;
        if (fieldName.contains("AMOUNT") || fieldName.contains("MONEY")) {
            value = RandomUtil.randomDouble(0, maxInteger);
            return formatNumber(value, Math.min(decimalPlaces, 2));
        } else if (fieldName.contains("RATE") || fieldName.contains("PERCENT")) {
            value = RandomUtil.randomDouble(0, Math.min(100, maxInteger));
            return formatNumber(value, Math.min(decimalPlaces, 4));
        } else if (fieldName.contains("AGE")) {
            return String.valueOf(RandomUtil.randomInt(0, Math.min(100, (int)maxInteger)));
        } else if (fieldName.contains("COUNT") || fieldName.contains("NUM")) {
            return String.valueOf(RandomUtil.randomInt(0, Math.min(1000, (int)maxInteger)));
        }
        
        // 默认数值处理
        if (decimalPlaces > 0) {
            value = RandomUtil.randomDouble(0, maxInteger);
            return formatNumber(value, decimalPlaces);
        } else {
            return String.valueOf(RandomUtil.randomInt(0, (int)maxInteger));
        }
    }

    /**
     * 解析数值类型的精度信息
     * 例如：NUMBER(10,2) -> [10,2]
     * @return int[0]=总长度, int[1]=小数位数
     */
    private static int[] parseNumberPrecision(String fieldType) {
        int[] result = new int[]{10, 0}; // 默认总长度10，小数位数0
        try {
            if (fieldType.contains("(") && fieldType.contains(")")) {
                String precisionStr = fieldType.substring(
                    fieldType.indexOf("(") + 1,
                    fieldType.indexOf(")")
                ).trim();
                String[] parts = precisionStr.split(",");
                if (parts.length > 0) {
                    result[0] = Integer.parseInt(parts[0].trim());
                    if (parts.length > 1) {
                        result[1] = Integer.parseInt(parts[1].trim());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析数值精度失败: {}, 使用默认值", fieldType);
        }
        return result;
    }

    /**
     * 格式化数值，保留指定小数位
     */
    private static String formatNumber(double value, int decimalPlaces) {
        if (decimalPlaces <= 0) {
            return String.valueOf((long)value);
        }
        // 使用String.format格式化数值
        return String.format("%." + decimalPlaces + "f", value);
    }

    /**
     * 主方法，用于测试数据生成
     */
    public static void main(String[] args) {
        // 示例：读取表结构并生成测试数据
        String modelFilePath = "D:\\BaiduSyncdisk\\工作目录\\商机\\202503湖南银行指标管理平台\\业务表表结构.xlsx";
        try {
            LinkedHashMap<String, TableStructure> tableMap = ExcelTableStructureReader.readExcel(modelFilePath);
            List<String> insertStatements = generateInsertStatements(tableMap, 30);
            
            // 打印生成的INSERT语句
            insertStatements.forEach(System.out::println);
            
            // 可选：将INSERT语句写入文件
            writeToFile(insertStatements);
            
        } catch (Exception e) {
            log.error("生成测试数据时发生错误", e);
        }
    }

    /**
     * 将INSERT语句写入文件
     */
    private static void writeToFile(List<String> sqlStatements) {
        String outputPath = base_export_path+"test_data_" + DateUtil.format(new Date(), "yyyyMMdd_HHmmss") + ".sql";
        try {
            // 在文件开头添加注释
            List<String> statementsWithHeader = new ArrayList<>();
            statementsWithHeader.add("-- 测试数据生成脚本");
            statementsWithHeader.add("-- 生成时间: " + DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
            statementsWithHeader.add("-- 注意：执行此脚本将清空相关表数据");
            statementsWithHeader.add("");
            statementsWithHeader.addAll(sqlStatements);

            cn.hutool.core.io.FileUtil.writeLines(statementsWithHeader, outputPath, "UTF-8");
            log.info("测试数据已写入文件: {}", outputPath);
        } catch (Exception e) {
            log.error("写入文件时发生错误", e);
        }
    }
} 