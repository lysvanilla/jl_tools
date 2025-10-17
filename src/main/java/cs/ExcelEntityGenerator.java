package cs;


import org.apache.poi.ss.usermodel.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ExcelEntityGenerator {
    // 实体类模板（@JsonProperty使用驼峰格式）
    private static final String ENTITY_TEMPLATE = "package com.example.entity;\n" +
            "\n" +
            "import com.fasterxml.jackson.annotation.JsonProperty;\n" +
            "import javax.persistence.Column;\n" +
            "import com.example.annotation.UiAnnotation;\n" +
            "\n" +
            "public class %s {\n" +
            "%s\n" +
            "}\n";

    // 字段模板（@JsonProperty使用驼峰格式，去掉下划线）
    private static final String FIELD_TEMPLATE = "    @JsonProperty(\"%s\")\n" +
            "    @Column(name = \"%s\")\n" +
            "    @UiAnnotation(description = \"%s\", type = \"text\", visible = true, readonly = true)\n" +
            "    private %s %s;\n\n";

    // Mapping文件模板
    private static final String MAPPING_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \n" +
            "\"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n" +
            "\n" +
            "<mapper namespace=\"com.example.mapper.%sMapper\">\n" +
            "    <!-- 基础结果映射 -->\n" +
            "    <resultMap id=\"BaseResultMap\" type=\"com.example.entity.%s\">\n" +
            "%s" +
            "    </resultMap>\n" +
            "\n" +
            "    <!-- 根据主键查询 -->\n" +
            "    <select id=\"selectByPrimaryKey\" resultMap=\"BaseResultMap\" parameterType=\"java.lang.Long\">\n" +
            "        select\n" +
            "%s" +
            "        from your_table_name <!-- 请替换为实际表名 -->\n" +
            "        where ID = #{id,jdbcType=BIGINT}\n" +
            "    </select>\n" +
            "\n" +
            "    <!-- 动态更新（非空字段才更新） -->\n" +
            "    <update id=\"updateByPrimaryKeySelective\" parameterType=\"com.example.entity.%s\">\n" +
            "        update your_table_name <!-- 请替换为实际表名 -->\n" +
            "        <set>\n" +
            "%s" +
            "        </set>\n" +
            "        where ID = #{id,jdbcType=BIGINT}\n" +
            "    </update>\n" +
            "</mapper>\n";

    // Mapping字段模板
    private static final String MAPPING_FIELD_TEMPLATE = "        <result column=\"%s\" property=\"%s\" jdbcType=\"%s\"/>\n";

    public static void generate(String excelFilePath, String entityName,
                                String entityOutputDir, String mappingOutputDir) {
        try {
            List<FieldInfo> fieldInfos = readExcel(excelFilePath);
            generateEntityClass(entityName, fieldInfos, entityOutputDir);
            generateMappingFile(entityName, fieldInfos, mappingOutputDir);
            System.out.println("实体类和Mapping文件生成成功！");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("生成失败：" + e.getMessage());
        }
    }

    private static List<FieldInfo> readExcel(String filePath) throws IOException {
        List<FieldInfo> fieldInfos = new ArrayList<>();

        try (InputStream is = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                FieldInfo fieldInfo = new FieldInfo();
                Cell nameCell = row.getCell(0);
                if (nameCell != null) {
                    fieldInfo.setChineseName(getCellValue(nameCell));
                }

                Cell englishNameCell = row.getCell(1);
                if (englishNameCell != null) {
                    fieldInfo.setEnglishName(getCellValue(englishNameCell));
                }

                Cell typeCell = row.getCell(2);
                if (typeCell != null) {
                    fieldInfo.setType(getCellValue(typeCell));
                }

                fieldInfos.add(fieldInfo);
            }
        }

        return fieldInfos;
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private static void generateEntityClass(String entityName, List<FieldInfo> fieldInfos, String outputDir) throws IOException {
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        StringBuilder fieldsBuilder = new StringBuilder();

        for (FieldInfo fieldInfo : fieldInfos) {
            String fieldName = camelCase(fieldInfo.getEnglishName());
            // 为@JsonProperty生成驼峰格式（去掉下划线）
            String jsonPropertyName = camelCase(fieldInfo.getEnglishName());

            fieldsBuilder.append(String.format(FIELD_TEMPLATE,
                    jsonPropertyName,  // @JsonProperty使用驼峰格式
                    fieldInfo.getEnglishName(),  // @Column保持原始下划线格式
                    fieldInfo.getChineseName(),
                    fieldInfo.getType(),
                    fieldName));
        }

        String entityContent = String.format(ENTITY_TEMPLATE, entityName, fieldsBuilder.toString());

        try (FileWriter writer = new FileWriter(new File(outputDir, entityName + ".java"))) {
            writer.write(entityContent);
        }
    }

    private static void generateMappingFile(String entityName, List<FieldInfo> fieldInfos, String outputDir) throws IOException {
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        StringBuilder resultMapBuilder = new StringBuilder();
        StringBuilder selectFieldsBuilder = new StringBuilder();
        StringBuilder updateSetBuilder = new StringBuilder();

        for (FieldInfo fieldInfo : fieldInfos) {
            String fieldName = camelCase(fieldInfo.getEnglishName());
            String jdbcType = getJdbcType(fieldInfo.getType());

            resultMapBuilder.append(String.format(MAPPING_FIELD_TEMPLATE,
                    fieldInfo.getEnglishName(),
                    fieldName,
                    jdbcType));

            selectFieldsBuilder.append("        ").append(fieldInfo.getEnglishName()).append(",\n");

            if (!"ID".equalsIgnoreCase(fieldInfo.getEnglishName())) {
                updateSetBuilder.append("        <if test=\"").append(fieldName).append(" != null\">\n")
                        .append("            ").append(fieldInfo.getEnglishName()).append(" = #{" + fieldName + ",jdbcType=" + jdbcType + "},\n")
                        .append("        </if>\n");
            }
        }

        if (selectFieldsBuilder.length() > 0) {
            selectFieldsBuilder.setLength(selectFieldsBuilder.length() - 2);
            selectFieldsBuilder.append("\n");
        }

        String mappingContent = String.format(MAPPING_TEMPLATE,
                entityName,
                entityName,
                resultMapBuilder.toString(),
                selectFieldsBuilder.toString(),
                entityName,
                updateSetBuilder.toString());

        try (FileWriter writer = new FileWriter(new File(outputDir, entityName + "Mapper.xml"))) {
            writer.write(mappingContent);
        }
    }

    // 下划线转驼峰命名（核心方法）
    private static String camelCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        StringBuilder result = new StringBuilder();
        boolean nextUpperCase = false;

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if (c == '_') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    result.append(Character.toUpperCase(c));
                    nextUpperCase = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }

        return result.toString();
    }

    private static String getJdbcType(String javaType) {
        switch (javaType.toLowerCase()) {
            case "string":
                return "VARCHAR";
            case "int":
            case "integer":
                return "INTEGER";
            case "long":
                return "BIGINT";
            case "float":
                return "FLOAT";
            case "double":
                return "DOUBLE";
            case "date":
                return "DATE";
            case "timestamp":
                return "TIMESTAMP";
            default:
                return "VARCHAR";
        }
    }

    public static void main(String[] args) {
        String excelFilePath = "C:\\Users\\lysva\\Desktop\\fields.xlsx";
        String entityName = "BusinessInfo";
        String entityOutputDir = "src/main/java/com/example/entity";
        String mappingOutputDir = "src/main/resources/mapper";

        generate(excelFilePath, entityName, entityOutputDir, mappingOutputDir);
    }

    private static class FieldInfo {
        private String chineseName;
        private String englishName;
        private String type;

        public String getChineseName() { return chineseName; }
        public void setChineseName(String chineseName) { this.chineseName = chineseName; }
        public String getEnglishName() { return englishName; }
        public void setEnglishName(String englishName) { this.englishName = englishName; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
}

