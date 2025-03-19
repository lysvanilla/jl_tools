package cn.sunline.function;

import cn.sunline.vo.Function;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;

@Slf4j
public class FunctionReader {

    public static LinkedHashMap<String, Function> readFunctionFile(String filePath) {
        LinkedHashMap<String, Function> functionMap = new LinkedHashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // 读取并跳过header行
            String headerLine = reader.readLine();
            if (headerLine == null) {
                log.error("文件为空");
                return functionMap;
            }
            
            // 读取数据行
            String line;
            while ((line = reader.readLine()) != null) {
                Function function = parseLine(line);
                if (function != null && function.getFunctionNameEn() != null) {
                    functionMap.put(function.getFunctionNameEn(), function);
                }
            }
            
            log.info("成功读取 {} 个功能配置", functionMap.size());
            
        } catch (IOException e) {
            log.error("读取function.txt文件失败", e);
        }
        
        return functionMap;
    }
    
    private static Function parseLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        
        Function function = new Function();
        try {
            // 使用状态机解析带引号的CSV
            StringBuilder currentField = new StringBuilder();
            boolean inQuotes = false;
            int fieldIndex = 0;
            
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                
                if (c == '"') {
                    if (inQuotes) {
                        // 检查是否是转义的引号
                        if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                            currentField.append('"');
                            i++; // 跳过下一个引号
                        } else {
                            inQuotes = false;
                        }
                    } else {
                        inQuotes = true;
                    }
                } else if (c == ',' && !inQuotes) {
                    // 字段结束，设置相应的属性
                    setField(function, fieldIndex, currentField.toString());
                    currentField.setLength(0);
                    fieldIndex++;
                } else {
                    currentField.append(c);
                }
            }
            
            // 处理最后一个字段
            if (currentField.length() > 0) {
                setField(function, fieldIndex, currentField.toString());
            }
            
        } catch (Exception e) {
            log.error("解析行失败: {}", line, e);
            return null;
        }
        
        return function;
    }
    
    private static void setField(Function function, int fieldIndex, String value) {
        switch (fieldIndex) {
            case 0:
                function.setFunctionNameCn(value.trim());
                break;
            case 1:
                function.setFunctionNameEn(value.trim());
                break;
            case 2:
                function.setFunctionDescriptions(value.trim());
                break;
            case 3:
                function.setFileNameLabel(value.trim());
                break;
        }
    }
    
    // 测试方法
    public static void main(String[] args) {
        String filePath = "config/function.txt";
        LinkedHashMap<String, Function> functionMap = readFunctionFile(filePath);
        functionMap.forEach((key, value) -> {
            System.out.println("Key: " + key);
            System.out.println("Value: " + value);
        });
    }
} 