package cn.sunline.service;

import cn.sunline.vo.Function;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * CommandFunctionService 测试类
 */
public class CommandFunctionServiceTest {
    
    private CommandFunctionService service;
    
    @Before
    public void setUp() {
        service = new CommandFunctionService();
    }
    
    /**
     * 测试获取所有功能名称
     */
    @Test
    public void testGetAllFunctionNames() {
        List<String> functionNames = service.getAllFunctionNames();
        
        // 验证功能名称列表不为空
        assertNotNull("功能名称列表不应为空", functionNames);
        assertFalse("功能名称列表不应为空", functionNames.isEmpty());
        
        // 打印功能名称
        System.out.println("功能名称列表:");
        for (String name : functionNames) {
            System.out.println("- " + name);
        }
    }
    
    /**
     * 测试获取功能对象
     */
    @Test
    public void testGetFunction() {
        // 获取所有功能名称
        List<String> functionNames = service.getAllFunctionNames();
        
        // 验证每个功能名称都能获取到对应的功能对象
        for (String name : functionNames) {
            Function function = service.getFunction(name);
            assertNotNull("功能 '" + name + "' 应该存在", function);
            assertEquals("功能名称应该匹配", name, function.getFunctionNameCn());
            
            // 打印功能信息
            System.out.println("\n功能: " + name);
            System.out.println("代码: " + function.getFunctionNameEn());
            System.out.println("描述: " + function.getFunctionDescriptions());
        }
        
        // 测试获取不存在的功能
        Function nonExistFunction = service.getFunction("不存在的功能");
        assertNull("不存在的功能应该返回 null", nonExistFunction);
    }
    
    /**
     * 测试功能执行（模拟测试，不实际执行）
     */
    @Test
    public void testExecuteFunction() {
        // 由于实际执行需要文件和环境，这里只测试参数验证逻辑
        
        try {
            // 测试空功能名称
            try {
                service.executeFunction("", "test.xlsx", "");
                fail("应该抛出异常：功能名称为空");
            } catch (Exception e) {
                // 预期会抛出异常
                assertTrue("异常消息应包含'功能名称不能为空'", e.getMessage().contains("功能名称不能为空"));
            }
            
            // 测试空文件名
            try {
                service.executeFunction("物理化", "", "");
                fail("应该抛出异常：文件名为空");
            } catch (Exception e) {
                // 预期会抛出异常
                assertTrue("异常消息应包含'文件名不能为空'", e.getMessage().contains("文件名不能为空"));
            }
            
            // 测试不存在的功能
            try {
                service.executeFunction("不存在的功能", "test.xlsx", "");
                fail("应该抛出异常：不支持的功能");
            } catch (Exception e) {
                // 预期会抛出异常
                assertTrue("异常消息应包含'不支持的功能'", e.getMessage().contains("不支持的功能"));
            }
            
            // 注意：不测试实际执行，因为这需要实际的文件和环境
            
        } catch (Exception e) {
            fail("测试过程中发生意外异常: " + e.getMessage());
        }
    }
}
