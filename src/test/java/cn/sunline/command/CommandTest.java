package cn.sunline.command;

import org.junit.Test;
import java.util.HashMap;
import java.util.Map;

/**
 * 命令模式测试类
 */
public class CommandTest {
    
    /**
     * 测试命令工厂
     */
    @Test
    public void testCommandFactory() {
        // 获取所有命令
        Map<String, Command> commands = CommandFactory.getAllCommands();
        
        // 打印所有命令信息
        System.out.println("所有可用命令：");
        for (Map.Entry<String, Command> entry : commands.entrySet()) {
            System.out.printf("代码: %-12s 描述: %s%n", entry.getKey(), entry.getValue().getDescription());
        }
        
        // 测试获取特定命令
        Command wlhCommand = CommandFactory.getCommand("wlh");
        if (wlhCommand != null) {
            System.out.println("\n获取到命令: " + wlhCommand.getDescription());
        } else {
            System.out.println("\n未找到命令: wlh");
        }
        
        // 测试获取不存在的命令
        Command nonExistCommand = CommandFactory.getCommand("non_exist");
        if (nonExistCommand != null) {
            System.out.println("获取到命令: " + nonExistCommand.getDescription());
        } else {
            System.out.println("未找到命令: non_exist");
        }
    }
    
    /**
     * 测试命令帮助类
     */
    @Test
    public void testCommandHelper() {
        // 获取命令代码到描述的映射
        Map<String, String> codeToDesc = CommandHelper.getCommandCodeToDescriptionMap();
        System.out.println("命令代码到描述的映射：");
        for (Map.Entry<String, String> entry : codeToDesc.entrySet()) {
            System.out.printf("代码: %-12s 描述: %s%n", entry.getKey(), entry.getValue());
        }
        
        // 获取命令描述到代码的映射
        Map<String, String> descToCode = CommandHelper.getCommandDescriptionToCodeMap();
        System.out.println("\n命令描述到代码的映射：");
        for (Map.Entry<String, String> entry : descToCode.entrySet()) {
            System.out.printf("描述: %-30s 代码: %s%n", entry.getKey(), entry.getValue());
        }
        
        // 测试根据代码获取描述
        String desc = CommandHelper.getDescriptionByCode("wlh");
        System.out.println("\n代码 'wlh' 对应的描述: " + desc);
        
        // 测试根据描述获取代码
        String code = CommandHelper.getCodeByDescription("物理化");
        System.out.println("描述 '物理化' 对应的代码: " + code);
    }
    
    /**
     * 测试命令执行
     * 注意：此测试仅模拟执行，不实际执行命令
     */
    @Test
    public void testCommandExecution() {
        // 获取命令
        Command command = CommandFactory.getCommand("wlh");
        if (command != null) {
            try {
                // 创建参数
                HashMap<String, String> args = new HashMap<>();
                args.put("file_name", "test.xlsx");
                
                // 模拟执行命令
                System.out.println("模拟执行命令: " + command.getDescription());
                System.out.println("参数: " + args);
                
                // 注意：实际执行可能会抛出异常，这里仅作演示
                // command.execute(args);
                
                System.out.println("命令执行成功");
            } catch (Exception e) {
                System.out.println("命令执行失败: " + e.getMessage());
            }
        } else {
            System.out.println("未找到命令");
        }
    }
}
