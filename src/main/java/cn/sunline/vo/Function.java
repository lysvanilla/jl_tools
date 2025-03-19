package cn.sunline.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Function {
    private String functionNameCn;
    private String functionNameEn;
    private String functionDescriptions;
    private String fileNameLabel;
    private String modelFileNameLabel;

    public Function(String functionNameCn, String functionNameEn, String functionDescriptions, String fileNameLabel) {
        this.functionNameCn = functionNameCn;
        this.functionNameEn = functionNameEn;
        this.functionDescriptions = functionDescriptions;
        this.fileNameLabel = fileNameLabel;
    }
}