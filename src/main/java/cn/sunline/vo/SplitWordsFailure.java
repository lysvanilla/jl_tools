package cn.sunline.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SplitWordsFailure {
    // 使用 @ExcelProperty 注解标记该属性在 Excel 中的表头为“缺词根”
    @ExcelProperty("缺词根")
    private String missingRoot;

    // 使用 @ExcelProperty 注解标记该属性在 Excel 中的表头为“拆解方式”
    @ExcelProperty("拆解方式")
    private String decompositionMethod;

    @ExcelProperty("关联字段")
    private String relatedFieldsStr;

    public SplitWordsFailure(String missingRoot, String decompositionMethod) {
        this.missingRoot = missingRoot;
        this.decompositionMethod = decompositionMethod;
    }

    private LinkedHashSet<String> relatedFields;

    public void addRelatedField(String relatedField) {
        if (relatedFields == null){
            this.relatedFields = new LinkedHashSet<>();
        }
        this.relatedFields.add(relatedField);
    }
}
