package cn.sunline.table;

import cn.hutool.core.io.FileUtil;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.FastExcel;
import cn.idev.excel.write.metadata.WriteSheet;
import cn.sunline.util.BasicInfo;
import cn.sunline.vo.SplitWordsFailure;
import cn.sunline.vo.TranslationResult;
import cn.sunline.vo.TranslationResultFull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
// 定义一个中文到英文的翻译器类
public class ChineseToEnglishTranslator {

    private static final String tpl_path = BasicInfo.TPL_PATH+"excel"+File.separator+"物理化翻译模板.xlsx";
    private static HashMap<String, String> translationDict = LexiconInfoReader.convertListToMap(tpl_path);
    private static final String basicExportPath = BasicInfo.getBasicExportPath("");
    private static LinkedHashMap<String, SplitWordsFailure> splitWordsFailureMap = new LinkedHashMap<>();
    private static HashMap<String, SplitWordsFailure> splitWordsLeftFailureMap = new HashMap<>();
    private static HashMap<String, SplitWordsFailure> splitWordsRightFailureMap = new HashMap<>();
    //public static final Log log = BasicInfo.log;

    public static void main(String[] args) {

        String filePath = "C:\\Users\\lysva\\Desktop\\物理化工具.xlsx";
        filePath = "C:\\Users\\lysva\\Desktop\\物理化工具.xlsx";
        filePath = "D:\\Users\\Documents\\WXWork\\1688851370921495\\Cache\\File\\2025-03\\物理化工具.xlsx";
        // 用于存储中文到英文的映射字典

       /* ChineseToEnglishTranslator translator = new ChineseToEnglishTranslator();
        *//*String input = "交易日期实现邹智实现邹";
        TranslationResultFull translationResultFull = translator.translateChinese(input);
        // 打印从左到右的翻译结果
        System.out.println("翻译结果: " + translationResultFull.toString());*//*
        List<TranslationResultFull> chineseList = translator.translateChineseMain(filePath);*/
        writeTranslatorExcel(filePath);
        /*System.out.println(tpl_path);
        System.out.println(basicExportPath);*/
    }

    public void writeTranslatorExcel(HashMap<String,String> args_map){
        String file_name=args_map.get("file_name");
        if (!FileUtil.exist(file_name)){
            log.error("file_name参数对应的文件不存在,[{}]",file_name);
            return;
        }
        writeTranslatorExcel(file_name);
    }

    public List<TranslationResultFull> translateChineseMain(String filePath) {
        //translationDict = LexiconInfoReader.convertListToMap(filePath);
        List<TranslationResultFull> chineseListResult = new ArrayList<>();
        List<TranslationResultFull> chineseList = readChineseFromExcel(filePath);
        for (TranslationResultFull result : chineseList) {
            TranslationResultFull translationResultFull = translateChinese(result.getChinese());
            chineseListResult.add(translationResultFull);
        }

        return chineseListResult;
    }

    public static void writeTranslatorExcel(String filePath){
        ChineseToEnglishTranslator translator = new ChineseToEnglishTranslator();
        List<TranslationResultFull> chineseList = translator.translateChineseMain(filePath);
        String fileName = FileUtil.mainName(filePath);
        String extFileName = FileUtil.extName(filePath);
        writeExcel(chineseList,tpl_path,basicExportPath+fileName+"-翻译结果"+BasicInfo.CURRENT_DATE+"."+extFileName);
    }

    public static void writeExcel(List<TranslationResultFull> translationResultFullList, String templatePath, String outputPath) {
        File templateFile = new File(templatePath);
        File outputFile = new File(outputPath);
        //System.out.println(FileUtil.exist(templatePath)+"\t"+templatePath);
        //System.out.println(FileUtil.exist(outputPath)+"\t"+outputPath);
        try(ExcelWriter excelWriter = FastExcel.write(outputPath).withTemplate(templatePath).build()){
            WriteSheet task_sheet = FastExcel.writerSheet("物理化结果").build();
            excelWriter.fill(translationResultFullList,task_sheet);

            splitWordsFailureMap.putAll(splitWordsLeftFailureMap);
            splitWordsFailureMap.putAll(splitWordsRightFailureMap);

            if (splitWordsFailureMap.size() > 0){
                WriteSheet split_sheet = FastExcel.writerSheet("词根缺失清单").build();
                // 使用 Stream API 将 Map 中的值收集到 List 中
                List<SplitWordsFailure> splitWordsFailureList = splitWordsFailureMap.values().stream()
                        .peek(failure -> failure.setRelatedFieldsStr(String.join("\n", failure.getRelatedFields())))
                        .collect(Collectors.toCollection(ArrayList::new));
                excelWriter.fill(splitWordsFailureList,split_sheet);
            }
        }
        log.info("物理化成功：[{}]",outputPath);
    }

    public static List<TranslationResultFull> readChineseFromExcel(String filePath) {
        File file = new File(filePath);
        return FastExcel.read(file)
                .sheet("待物理化清单")
                .head(TranslationResultFull.class)
                .doReadSync();
    }

    // 从左到右进行翻译的方法，接收一个需要翻译的中文字符串作为输入
    public TranslationResult translateLeftToRight(String input) {
        // 用于存储翻译后的英文结果
        List<String> translated = new ArrayList<>();
        // 用于存储拆分后的中文词，以特定格式表示
        List<String> splitWords = new ArrayList<>();
        // 用于存储未匹配上的中文拆词
        List<String> unmatched = new ArrayList<>();
        // 用于暂存连续未匹配的字符
        StringBuilder tempUnmatched = new StringBuilder();

        // 当输入字符串不为空时，继续进行翻译操作
        while (!input.isEmpty()) {
            // 标记是否找到了匹配的词
            boolean matchFound = false;
            // 记录匹配到的词
            String matchedWord = "";

            // 尝试从输入字符串的开头截取不同长度的子串，从最长子串开始尝试匹配
            for (int i = input.length(); i > 0; i--) {
                // 获取当前截取的子串
                String currentSubstring = input.substring(0, i);
                // 如果字典中包含该子串
                if (translationDict.containsKey(currentSubstring)) {
                    // 标记找到匹配的词
                    matchFound = true;
                    // 记录匹配到的词
                    matchedWord = currentSubstring;
                    break;
                }
            }

            // 如果找到了匹配的词
            if (matchFound) {
                // 处理暂存的未匹配字符
                if (tempUnmatched.length() > 0) {
                    unmatched.add(tempUnmatched.toString());
                    tempUnmatched.setLength(0);
                }
                // 将匹配到的词对应的英文翻译添加到翻译结果中
                translated.add(translationDict.get(matchedWord));
                // 将匹配到的词添加到拆分结果中
                splitWords.add(matchedWord);
                // 从输入字符串中移除已经匹配的部分
                input = input.substring(matchedWord.length());
            } else {
                // 如果没有找到匹配的词，将输入字符串的第一个字符添加到暂存的未匹配字符中
                tempUnmatched.append(input.charAt(0));
                // 从输入字符串中移除第一个字符
                input = input.substring(1);
            }
        }

        // 处理最后剩余的暂存未匹配字符
        if (tempUnmatched.length() > 0) {
            unmatched.add(tempUnmatched.toString());
        }
        // 创建一个翻译结果对象，包含翻译后的英文、拆分结果和未匹配结果
        return new TranslationResult(String.join("_", translated), String.join("#", splitWords), String.join("#", unmatched));

    }

    // 从右到左进行翻译的方法，接收一个需要翻译的中文字符串作为输入
    public TranslationResult translateRightToLeft(String input) {
        // 用于存储翻译后的英文结果
        List<String> translated = new ArrayList<>();
        // 用于存储拆分后的中文词，以特定格式表示
        List<String> splitWords = new ArrayList<>();
        // 用于存储未匹配上的中文拆词
        List<String> unmatched = new ArrayList<>();
        // 用于暂存连续未匹配的字符
        StringBuilder tempUnmatched = new StringBuilder();

        // 当输入字符串不为空时，继续进行翻译操作
        while (!input.isEmpty()) {
            // 标记是否找到了匹配的词
            boolean matchFound = false;
            // 记录匹配到的词
            String matchedWord = "";

            // 尝试从输入字符串的末尾截取不同长度的子串，从最长子串开始尝试匹配
            for (int i = input.length(); i > 0; i--) {
                // 获取当前截取的子串
                String currentSubstring = input.substring(input.length() - i);
                // 如果字典中包含该子串
                if (translationDict.containsKey(currentSubstring)) {
                    // 标记找到匹配的词
                    matchFound = true;
                    // 记录匹配到的词
                    matchedWord = currentSubstring;
                    break;
                }
            }

            // 如果找到了匹配的词
            if (matchFound) {
                // 处理暂存的未匹配字符
                if (tempUnmatched.length() > 0) {
                    unmatched.add(0, tempUnmatched.toString());
                    tempUnmatched.setLength(0);
                }
                // 将匹配到的词对应的英文翻译添加到翻译结果中
                translated.add(0, translationDict.get(matchedWord));
                // 将匹配到的词添加到拆分结果中
                splitWords.add(0, matchedWord);
                // 从输入字符串中移除已经匹配的部分
                input = input.substring(0, input.length() - matchedWord.length());
            } else {
                // 如果没有找到匹配的词，将输入字符串的最后一个字符添加到暂存的未匹配字符中
                tempUnmatched.insert(0, input.charAt(input.length() - 1));
                // 从输入字符串中移除最后一个字符
                input = input.substring(0, input.length() - 1);
            }
        }

        // 处理最后剩余的暂存未匹配字符
        if (tempUnmatched.length() > 0) {
            unmatched.add(0, tempUnmatched.toString());
        }

        // 创建一个翻译结果对象，包含翻译后的英文、拆分结果和未匹配结果
        return new TranslationResult(String.join("_", translated), String.join("#", splitWords), String.join("#", unmatched));
    }


    public TranslationResultFull  translateChinese(String input){
        ChineseToEnglishTranslator translator = new ChineseToEnglishTranslator();
        String result = removeSpecialCharacters(input);
        // 调用从左到右的翻译方法，得到翻译结果
        TranslationResult leftToRightResult = translator.translateLeftToRight(result);
        String leftTranslation = leftToRightResult.getTranslatedText();
        String leftSplitWords = leftToRightResult.getSplitWords();
        String leftUnmatchedWords = leftToRightResult.getUnmatchedWords();
        String isLeftUnmatchedWords = leftUnmatchedWords.isEmpty() ? "否" : "缺词根";
        if (StringUtils.isNotEmpty(leftUnmatchedWords)){
            List<String> splitWordsFailure = Arrays.asList(leftUnmatchedWords.toString().split("#"));
            //splitWordsFailure.forEach(element -> splitWordsRightFailureMap.put("向右缺词根"+element,new SplitWordsFailure(element,"向右缺词根")));
            splitWordsFailure.forEach(element -> {
                String key = "向右缺词根" + element;
                if (!splitWordsRightFailureMap.containsKey(key)) {
                    SplitWordsFailure splitWordsFailure1 = new SplitWordsFailure(element, "向右缺词根");
                    splitWordsFailure1.addRelatedField(input);
                    splitWordsRightFailureMap.put(key, splitWordsFailure1);
                }else{
                    splitWordsRightFailureMap.get(key).addRelatedField(input);
                }
            });
        }


        // 调用从右到左的翻译方法，得到翻译结果
        TranslationResult rightToLeftResult = translator.translateRightToLeft(result);

        String rightTranslation = rightToLeftResult.getTranslatedText();
        String rightSplitWords = rightToLeftResult.getSplitWords();
        String rightUnmatchedWords = rightToLeftResult.getUnmatchedWords();
        String isRightUnmatchedWords = rightUnmatchedWords.isEmpty() ? "否" : "缺词根";
        if (StringUtils.isNotEmpty(rightUnmatchedWords)){
            List<String> splitWordsFailure = Arrays.asList(rightUnmatchedWords.toString().split("#"));
            //splitWordsFailure.forEach(element -> splitWordsLeftFailureMap.put("向左缺词根"+element,new SplitWordsFailure(element,"向左缺词根")));
            splitWordsFailure.forEach(element -> {
                String key = "向右缺词根" + element;
                if (!splitWordsLeftFailureMap.containsKey(key)) {
                    SplitWordsFailure splitWordsFailure1 = new SplitWordsFailure(element, "向左缺词根");
                    splitWordsFailure1.addRelatedField(input);
                    splitWordsLeftFailureMap.put(key, splitWordsFailure1);
                }else{
                    splitWordsLeftFailureMap.get(key).addRelatedField(input);
                }
            });
        }


        String isTranslationSame = leftTranslation.equals(rightTranslation) ? "相同" : "不相同";
        String isLeftOrRightUnmatchedWords = leftUnmatchedWords.isEmpty() && rightUnmatchedWords.isEmpty() ? "否" : "缺词根";

        TranslationResultFull translationResultFull = new TranslationResultFull(input,leftTranslation,leftSplitWords,leftUnmatchedWords,isLeftUnmatchedWords,rightTranslation,rightSplitWords,rightUnmatchedWords,isRightUnmatchedWords,isTranslationSame,isLeftOrRightUnmatchedWords);

        return translationResultFull;
    }

    public static String removeSpecialCharacters(String input) {
        // 使用正则表达式匹配除字母、数字和中文之外的所有字符并替换为空字符串
        return input.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "");
    }

}