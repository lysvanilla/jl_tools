package cn.sunline.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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


}
