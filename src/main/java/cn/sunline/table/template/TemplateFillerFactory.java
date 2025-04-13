package cn.sunline.table.template;

import lombok.extern.slf4j.Slf4j;

/**
 * 模板填充器工厂类，用于创建不同类型的模板填充器
 */
@Slf4j
public class TemplateFillerFactory {
    
    /**
     * 模板类型枚举
     */
    public enum TemplateType {
        DDL,
        INSERT
    }
    
    /**
     * 创建模板填充器
     * @param type 模板类型
     * @return 模板填充器
     */
    public static AbstractTemplateFiller createFiller(TemplateType type) {
        switch (type) {
            case DDL:
                return new DdlTemplateFiller();
            case INSERT:
                return new InsertTemplateFiller();
            default:
                log.error("不支持的模板类型: {}", type);
                throw new IllegalArgumentException("不支持的模板类型: " + type);
        }
    }
    
    /**
     * 创建DDL模板填充器
     * @return DDL模板填充器
     */
    public static DdlTemplateFiller createDdlFiller() {
        return (DdlTemplateFiller) createFiller(TemplateType.DDL);
    }
    
    /**
     * 创建Insert模板填充器
     * @return Insert模板填充器
     */
    public static InsertTemplateFiller createInsertFiller() {
        return (InsertTemplateFiller) createFiller(TemplateType.INSERT);
    }
}
