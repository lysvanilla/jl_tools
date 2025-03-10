package cn.sunline.vo.etl;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtlUpdateRecord {
    @ExcelProperty("日期")
    private String date;

    @ExcelProperty("更新人")
    private String updater;

    @ExcelProperty("说明")
    private String description;
}
