package cn.sunline;

import cn.idev.excel.FastExcel;
import cn.sunline.vo.IndexInfo;
import cn.sunline.vo.LexiconInfo;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.internal.StringUtil;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class LexiconInfoReader {
    public static void main(String[] args) {
        String filePath = "C:\\Users\\lysva\\Desktop\\物理化工具.xlsx";
        filePath = "C:\\Users\\lysva\\Desktop\\物理化工具-公司1.xlsx";
        //List<LexiconInfo> indexInfoList = LexiconInfoReader.readExcel(filePath);
        /*List<LexiconInfo> filteredList = indexInfoList.stream()
                .collect(Collectors.toList());*/
        HashMap<String, String> resultMap = LexiconInfoReader.convertListToMap(filePath);
        System.out.println(resultMap.get("实现"));
        System.out.println("aa");
       /* for (IndexInfo indexInfo : indexInfoList) {
            System.out.println(indexInfo);
        }*/
    }
    public static List<LexiconInfo> readExcel(String filePath) {
        File file = new File(filePath);
        return FastExcel.read(file)
                .sheet("词库")
                .head(LexiconInfo.class)
                .doReadSync();
    }
    public static HashMap<String, String> convertListToMap(String filePath) {
        List<LexiconInfo> indexInfoList = LexiconInfoReader.readExcel(filePath);
        HashMap<String, String> resultMap = LexiconInfoReader.convertListToMap(indexInfoList);
        return resultMap;
    }

    public static HashMap<String, String> convertListToMap(List<LexiconInfo> lexiconInfoList) {
        HashMap<String, String> resultMap = new HashMap<>();
        for (LexiconInfo info : lexiconInfoList) {
            String englishAbbreviation  = info.getEnglishAbbreviation();
            String chineseName = info.getChineseName();
            if (StringUtils.isEmpty(englishAbbreviation)){
                continue;
            }
            // 将 chineseName 和 englishAbbreviation 作为映射关系写入 HashMap
            if (StringUtils.isNotEmpty(chineseName) ){
                resultMap.put(chineseName, englishAbbreviation);
            }

            // 获取同义词列表
            String synonyms = info.getSynonyms();
            if (synonyms != null) {
                List<String> synonymList = Arrays.asList(synonyms.split("\\|"));
                // 遍历同义词列表，将每个同义词和 englishAbbreviation 作为映射关系写入 HashMap
                if (synonymList != null) {
                    for (String synonym : synonymList) {
                        resultMap.put(synonym, englishAbbreviation);
                    }
                }
            }
        }
        return resultMap;
    }

}
