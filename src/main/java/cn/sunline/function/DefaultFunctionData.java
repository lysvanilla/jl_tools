package cn.sunline.function;

import cn.sunline.vo.Function;
import java.util.LinkedHashMap;

/**
 * 提供默认的Function数据
 */
public class DefaultFunctionData {

    /**
     * 获取默认的Function配置数据
     * @return LinkedHashMap<String, Function> 包含预定义Function对象的Map
     */
    public static LinkedHashMap<String, Function> getDefaultFunctions() {
        LinkedHashMap<String, Function> functionMap = new LinkedHashMap<>();

        Function wlhFunction = new Function("物理化", "wlh",
                "将Excel文件中的字段中文翻译为英文，并输出拆词匹配结果",
                "* 输入待物理化文件file_name:");
        Function ddlFunction = new Function("物理模型生成DDL建表语句", "ddl",
                "根据物理模型Excel生成DDL建表语句、简单的insert语句",
                "* 输入物理模型文件file_name:");
        Function dmlFunction = new Function("映射文档生成DML脚本", "dml",
                "根据映射文档Excel生成DML脚本",
                "* 输入映射文档文件或者文件夹file_name:");
        Function genMappFunction = new Function("接口层物理模型生成映射文档", "gen_mapp",
                "根据接口层表结构生成接口层映射文档",
                "* 输入接口层物理模型文件file_name:");
        Function genTableFunction = new Function("映射文档生成物理模型初稿", "gen_table",
                "根据映射文档生成物理模型初稿",
                "* 输入映射文档文件或者文件夹file_name:");
        Function suppMappFunction = new Function("根据物理模型补充映射文档", "supp_mapp",
                "根据物理模型的表结构信息，更新映射文档中的字段英文名、过滤条件",
                "* 输入映射文档文件或者文件夹file_name:","* 输入物理模型文件model_file_name:");
        Function updateMappFunction = new Function("更新映射文档到最新模板", "update_mapp",
                "更新已有的映射文档",
                "* 输入映射文档文件或者文件夹file_name:");
        Function getRelaTabFunction = new Function("根据映射文档获取模型依赖表", "get_rela_tab",
                "读取映射文档中的表关联关系中的配置的源表英文名来识别依赖关系并生成Excel",
                "* 输入映射文档文件称或者文件夹file_name:");
        Function zbFunction = new Function("指标过程Excel文档转换标准模板", "zb",
                "将风控指标转换为行里指标标准格式的模板",
                "* 输入指标过程Excel文件file_name:");
        Function cfFunction = new Function("EXCEL拆分", "cf",
                "将Excel文件按规则拆分为多个文件",
                "* 输入待拆分Excel文件file_name:");
        Function hbFunction = new Function("EXCEL合并", "hb",
                "将多个Excel文件合并为单一文件",
                "* 输入待合并Excel文件file_name:");

        functionMap.put(wlhFunction.getFunctionNameCn(), wlhFunction);
        functionMap.put(ddlFunction.getFunctionNameCn(), ddlFunction);
        functionMap.put(dmlFunction.getFunctionNameCn(), dmlFunction);
        functionMap.put(genMappFunction.getFunctionNameCn(), genMappFunction);
        functionMap.put(genTableFunction.getFunctionNameCn(), genTableFunction);
        functionMap.put(suppMappFunction.getFunctionNameCn(), suppMappFunction);
        functionMap.put(updateMappFunction.getFunctionNameCn(), updateMappFunction);
        functionMap.put(getRelaTabFunction.getFunctionNameCn(), getRelaTabFunction);
        functionMap.put(zbFunction.getFunctionNameCn(), zbFunction);
        functionMap.put(cfFunction.getFunctionNameCn(), cfFunction);
        functionMap.put(hbFunction.getFunctionNameCn(), hbFunction);

        
        return functionMap;
    }
    
    /**
     * 测试方法
     */
    public static void main(String[] args) {
        LinkedHashMap<String, Function> functionMap = getDefaultFunctions();
        System.out.println("默认功能配置:");
        functionMap.forEach((key, value) -> {
            System.out.println("\n功能键: " + key);
            System.out.println("功能名称(中文): " + value.getFunctionNameCn());
            System.out.println("功能名称(英文): " + value.getFunctionNameEn());
            System.out.println("功能描述: " + value.getFunctionDescriptions());
            System.out.println("文件标签: " + value.getFileNameLabel());
            System.out.println("文件标签: " + value.getModelFileNameLabel());
        });
    }
} 