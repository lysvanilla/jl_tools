package cn.sunline.table.service;

import cn.sunline.table.template.TemplateFillerException;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * SqlGenerationService 测试类
 */
public class SqlGenerationServiceTest {
    
    private SqlGenerationService service;
    
    @Before
    public void setUp() {
        service = new SqlGenerationService();
    }
    
    /**
     * 测试参数验证
     */
    @Test(expected = TemplateFillerException.class)
    public void testParameterValidation() throws TemplateFillerException {
        // 创建一个空的参数映射
        HashMap<String, String> emptyArgs = new HashMap<>();
        
        // 应该抛出 TemplateFillerException
        service.generateSql(emptyArgs);
    }
    
    /**
     * 测试文件不存在
     */
    @Test(expected = TemplateFillerException.class)
    public void testFileNotFound() throws TemplateFillerException {
        // 创建一个包含不存在文件的参数映射
        HashMap<String, String> args = new HashMap<>();
        args.put("file_name", "non_existent_file.xlsx");
        
        // 应该抛出 TemplateFillerException
        service.generateSql(args);
    }
    
    /**
     * 测试参数映射方法
     */
    @Test
    public void testGenerateSqlWithArgsMap() {
        // 创建一个包含有效文件的参数映射
        HashMap<String, String> args = new HashMap<>();
        args.put("file_name", "src/test/resources/test_table.xlsx");
        
        try {
            // 如果测试资源文件存在，则不应抛出异常
            if (new java.io.File(args.get("file_name")).exists()) {
                service.generateSql(args);
            } else {
                // 如果测试资源文件不存在，则跳过测试
                System.out.println("测试资源文件不存在，跳过测试");
            }
        } catch (TemplateFillerException e) {
            // 如果是由于文件不存在导致的异常，则忽略
            if (!e.getMessage().contains("文件不存在")) {
                fail("不应抛出非文件不存在异常: " + e.getMessage());
            }
        }
    }
}
