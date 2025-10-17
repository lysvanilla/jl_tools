package cn.sunline.web;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WordBankExtractor {
    public static void main(String[] args) {
        // 检查参数
        /*if (args.length == 0) {
            System.out.println("请提供Word文档路径作为参数");
            System.out.println("用法: java WordBankExtractor <文档路径>");
            return;
        }*/

        //String filePath = args[0];
        String filePath = "D:\\BaiduSyncdisk\\工作目录\\商机\\20250728广东华兴湖仓一体\\【标书商务+技术框架】广东华兴银行湖仓一体化信创改造及实施项目V0.6.docx";
        File file = new File(filePath);

        // 检查文件是否存在
        if (!file.exists()) {
            System.out.println("文件不存在: " + filePath);
            return;
        }

        try {
            // 读取文档内容
            String content = readWordDocument(filePath);
            if (content == null || content.isEmpty()) {
                System.out.println("文档内容为空或无法读取");
                return;
            }

            // 提取"银行"前面的5个字符
            List<String> results = new ArrayList<>();
            /*List<String> results1 = extractCharactersBeforeBank(content,"银行");
            results.addAll(results1);*/
            List<String> results2 = extractCharactersBeforeBank(content,"银行");
            results.addAll(results2);
            // 输出结果
            System.out.println("找到 " + results.size() + " 处包含'银行'的位置:");
            for (int i = 0; i < results.size(); i++) {
                System.out.println("位置 " + (i + 1) + ": " + results.get(i));
            }

        } catch (Exception e) {
            System.out.println("处理文档时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 读取Word文档内容
     * @param filePath 文档路径
     * @return 文档内容字符串
     * @throws IOException 读写异常
     */
    private static String readWordDocument(String filePath) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        String content = null;

        // 根据文件扩展名判断文档类型
        if (filePath.endsWith(".doc")) {
            // 处理.doc格式
            HWPFDocument document = new HWPFDocument(fis);
            WordExtractor extractor = new WordExtractor(document);
            content = extractor.getText();
            extractor.close();
        } else if (filePath.endsWith(".docx")) {
            // 处理.docx格式
            XWPFDocument document = new XWPFDocument(fis);
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            content = extractor.getText();
            extractor.close();
        } else {
            throw new IllegalArgumentException("不支持的文件格式，仅支持.doc和.docx");
        }

        fis.close();
        return content;
    }

    /**
     * 提取所有"银行"前面的5个字符
     * @param content 文档内容
     * @return 提取结果列表
     */
    private static List<String> extractCharactersBeforeBank(String content,String target) {
        List<String> results = new ArrayList<>();
        //String target = "银行";
        int index = content.indexOf(target);

        while (index != -1) {
            // 计算起始位置，确保不会越界
            int start = Math.max(0, index - 10);
            // 提取从start到index之间的字符
            String prefix = content.substring(start, index);

            // 如果不足5个字符，用空格补齐显示，方便查看
            if (prefix.length() < 10) {
                // 兼容 Java 8 的空格补齐方式
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 10 - prefix.length(); i++) {
                    sb.append(' ');
                }
                prefix = sb.toString() + prefix;
            }

            results.add(prefix + " -> "+target);

            // 继续查找下一个"银行"
            index = content.indexOf(target, index + target.length());
        }

        return results;
    }
}
