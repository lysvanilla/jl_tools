package cn.sunline.table.template;

import cn.hutool.core.io.FileUtil;
import cn.sunline.vo.TableFieldInfo;
import cn.sunline.vo.TableStructure;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.junit.Assert.*;

/**
 * DdlTemplateFiller 测试类
 */
public class DdlTemplateFillerTest {
    
    private DdlTemplateFiller filler;
    private TableStructure testTable;
    
    @Before
    public void setUp() {
        // 创建模板填充器
        filler = (DdlTemplateFiller) TemplateFillerFactory.createFiller(TemplateFillerFactory.TemplateType.DDL);
        
        // 创建测试表结构
        testTable = new TableStructure();
        testTable.setTableNameEn("TEST_TABLE");
        testTable.setTableNameCn("测试表");
        testTable.setSystemModule("test");
        testTable.setAlgorithmType("hash");
        testTable.setDesigner("测试人员");
        testTable.setOnlineTime("2023-05-15");
        
        // 创建字段
        LinkedHashMap<String, TableFieldInfo> fieldMap = new LinkedHashMap<>();
        
        // 添加主键字段
        TableFieldInfo idField = new TableFieldInfo();
        idField.setFieldNameEn("ID");
        idField.setFieldNameCn("标识");
        idField.setFieldType("VARCHAR(32)");
        idField.setNotNull("Y");
        idField.setPrimaryKey("Y");
        idField.setBucketKey("Y");
        fieldMap.put("ID", idField);
        
        // 添加普通字段
        TableFieldInfo nameField = new TableFieldInfo();
        nameField.setFieldNameEn("NAME");
        nameField.setFieldNameCn("名称");
        nameField.setFieldType("VARCHAR(100)");
        nameField.setNotNull("Y");
        fieldMap.put("NAME", nameField);
        
        TableFieldInfo descField = new TableFieldInfo();
        descField.setFieldNameEn("DESCRIPTION");
        descField.setFieldNameCn("描述");
        descField.setFieldType("VARCHAR(500)");
        descField.setNotNull("N");
        fieldMap.put("DESCRIPTION", descField);
        
        // 设置字段映射
        testTable.setFieldMap(fieldMap);
    }
    
    /**
     * 测试获取模板路径
     */
    @Test
    public void testGetTemplatePath() {
        String templatePath = filler.getTemplatePath(testTable);
        assertNotNull("模板路径不应为空", templatePath);
        assertTrue("模板路径应该存在", FileUtil.exist(templatePath));
    }
    
    /**
     * 测试填充模板
     */
    @Test
    public void testFillTemplate() {
        String filledTemplate = filler.fillTemplate(testTable);
        assertNotNull("填充后的模板不应为空", filledTemplate);
        assertTrue("填充后的模板应包含表名", filledTemplate.contains("TEST_TABLE"));
        assertTrue("填充后的模板应包含主键", filledTemplate.contains("ID"));
    }
    
    /**
     * 测试获取输出路径
     */
    @Test
    public void testGetOutputPath() {
        String outputPath = filler.getOutputPath(testTable);
        assertNotNull("输出路径不应为空", outputPath);
        assertTrue("输出路径应包含表名", outputPath.contains("test_table"));
    }
    
    /**
     * 测试异常处理
     */
    @Test
    public void testExceptionHandling() {
        // 创建一个无效的表结构
        TableStructure invalidTable = new TableStructure();
        // 不设置任何属性
        
        try {
            filler.fillTemplate(invalidTable);
            // 应该不会抛出异常，但会返回空字符串
            // 如果抛出异常，测试将失败
        } catch (Exception e) {
            fail("不应抛出异常，而是返回空字符串: " + e.getMessage());
        }
    }
    
    /**
     * 测试文件不存在异常
     */
    @Test(expected = TemplateFillerException.class)
    public void testFileNotFound() throws TemplateFillerException {
        // 创建一个不存在的文件路径
        String nonExistentFile = "non_existent_file.xlsx";
        
        // 应该抛出 TemplateFillerException
        new SqlGenerationService().generateSql(nonExistentFile);
    }
}
