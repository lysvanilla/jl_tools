package cn.person;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.FileOutputStream;
import java.io.IOException;

public class ChineseToPinyinWord {

    public static void convertToWordWithPinyin(String chineseText, String outputPath) {
        try (XWPFDocument document = new XWPFDocument()) {
            // 设置拼音格式
            HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
            format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
            format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

            // 按行处理文本
            String[] lines = chineseText.split("\n");
            for (String line : lines) {
                // 创建拼音行
                XWPFParagraph pinyinParagraph = document.createParagraph();
                pinyinParagraph.setAlignment(ParagraphAlignment.LEFT);

                // 创建汉字行
                XWPFParagraph charParagraph = document.createParagraph();
                charParagraph.setAlignment(ParagraphAlignment.LEFT);

                // 处理每个中文字符
                for (char c : line.toCharArray()) {
                    if (Character.toString(c).matches("[\\u4E00-\\u9FA5]+")) {
                        // 获取拼音
                        String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                        if (pinyinArray != null && pinyinArray.length > 0) {
                            String pinyin = pinyinArray[0];

                            // 添加拼音
                            XWPFRun pinyinRun = pinyinParagraph.createRun();
                            pinyinRun.setText(pinyin);
                            pinyinRun.setFontSize(8);
                            pinyinRun.setBold(true);
                            pinyinRun.setFontFamily("Courier New");

                            // 添加拼音后的空格
                            XWPFRun pinyinSpace = pinyinParagraph.createRun();
                            pinyinSpace.setText(" ");
                            pinyinSpace.setFontFamily("Courier New");

                            // 添加汉字
                            XWPFRun charRun = charParagraph.createRun();
                            charRun.setText(String.valueOf(c));
                            charRun.setFontSize(12);
                            charRun.setFontFamily("SimSun");

                            // 添加汉字后的空格
                            XWPFRun charSpace = charParagraph.createRun();
                            charSpace.setText(" ");
                            charSpace.setFontFamily("Courier New");
                        }
                    } else {
                        // 非中文字符直接添加
                        XWPFRun pinyinRun = pinyinParagraph.createRun();
                        pinyinRun.setText(" ");
                        pinyinRun.setFontFamily("Courier New");

                        XWPFRun charRun = charParagraph.createRun();
                        charRun.setText(String.valueOf(c));
                        charRun.setFontSize(12);
                        charRun.setFontFamily("SimSun");

                        // 添加字符后的空格
                        XWPFRun charSpace = charParagraph.createRun();
                        charSpace.setText(" ");
                        charSpace.setFontFamily("Courier New");
                    }
                }
            }

            // 保存文档
            try (FileOutputStream out = new FileOutputStream(outputPath)) {
                document.write(out);
            }

        } catch (IOException | BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String chineseText = "学习的时候一定要用心，\n" +
                "就像你在玩游戏的时候特别专注一样。\n" +
                "要是作业简单，那就要细心，\n" +
                "千万别粗心大意，\n" +
                "要是作业有点难，那就要有耐心，\n" +
                "慢慢来，一步一步把它搞定。\n" +
                "妈妈想告诉你，分数虽然不是衡量你是不是最棒的唯一标准，\n" +
                "但它确实能直接反映出你这段时间学得怎么样。\n" +
                "只要你用心去做，就一定能看到自己的进步。\n" +
                "妈妈特别期待你每一个小小的成长和变化，\n" +
                "相信你一定可以做到的！";
        String outputPath = "output.docx";
        convertToWordWithPinyin(chineseText, outputPath);
        System.out.println("文档已生成：" + outputPath);
    }
}