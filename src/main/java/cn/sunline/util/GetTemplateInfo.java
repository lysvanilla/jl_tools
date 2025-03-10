package cn.sunline.util;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * GetTemplateInfo 类主要用于处理模板文件相关信息，
 * 包括从模板文件中提取包含特定占位符的行、对字符串进行切割处理以及移除字符串中首次出现的指定字符等操作。
 */
@Slf4j
public class GetTemplateInfo {
    // 从 BasicInfo 工具类获取数据库类型，作为常量使用
    //private static final String DB_TYPE = BasicInfo.getBasicPara("db_type");
    // 构建基础 SQL 模板文件所在的路径
    //private static final String BASE_SQL_TPL_PATH = System.getProperty("user.dir") + "/config/" + DB_TYPE + "/";
    // 定义用于分割字符串的关键字列表，包含多种 SQL 关键字及分隔符
    private static final List<String> SPLIT_STR_LIST = ListUtil.toLinkedList(
            ";",
            "and ",
            "or ",
            "inner join ",
            "full join ",
            "left join ",
            "right join ",
            "AND ",
            "OR ",
            "INNER JOIN ",
            "FULL JOIN ",
            "LEFT JOIN ",
            "RIGHT JOIN ",
            "||"
    );

    /**
     * 程序入口方法，用于测试从模板文件中提取包含特定占位符行的功能。
     *
     * @param args 命令行参数，此处未使用
     */
    public static void main(String[] args) {
        // 调用 getCircleLine 方法从指定模板文件中提取包含特定占位符的行
        //List<String> circleLines = getCircleLine(BASE_SQL_TPL_PATH + "itl_table_ddl.sql");
        // 记录从模板文件中提取到的包含特定占位符的行的数量
        //log.info("从模板文件 [{}] 中提取到 [{}] 条包含 '@' 的行", BASE_SQL_TPL_PATH + "itl_table_ddl.sql", circleLines.size());
    }

    /**
     * 从指定的模板文件中提取包含 '@' 符号的行。
     *
     * @param fileName 模板文件的完整路径
     * @return 包含 '@' 符号的行的列表，如果文件读取失败或文件中无符合条件的行则返回空列表
     */
    public static List<String> getCircleLine(String fileName) {
        // 初始化一个空列表，用于存储包含 '@' 符号的行
        List<String> circleLineList = new ArrayList<>();
        try {
            // 读取指定文件的全部内容
            String tplFile = new FileReader(fileName).readString();
            // 将文件内容按行分割成字符串数组
            String[] tplFileArr = tplFile.split("\n");
            // 遍历每一行
            for (String line : tplFileArr) {
                // 检查当前行是否包含 '@' 符号
                if (line.contains("@")) {
                    // 若结果列表中不包含该行，则添加到列表中
                    if (!circleLineList.contains(line)) {
                        circleLineList.add(line);
                    }
                }
            }
            // 记录成功从文件中提取到的包含 '@' 符号的行的数量
            //log.info("从文件 [{}] 中成功提取到 [{}] 条包含 '@' 的行", fileName, circleLineList.size());
        } catch (Exception e) {
            // 若读取文件过程中出现异常，记录错误信息
            log.error("读取文件 [{}] 时发生异常: {}", fileName, e.getMessage(), e);
        }
        return circleLineList;
    }

    /**
     * 对输入的字符串进行切割处理，如果字符串以分割关键字列表中的某个关键字开头，
     * 则移除该关键字并在相应位置填充空格。
     *
     * @param str 待处理的字符串
     * @return 处理后的字符串，如果未匹配到分割关键字则返回原字符串
     */
    public static String cutStr(String str) {
        // 初始化结果为原始字符串
        String result = str;
        // 去除字符串前后的空白字符
        String trimmedStr = str.trim();
        // 遍历分割关键字列表
        for (String splitStr : SPLIT_STR_LIST) {
            // 检查处理后的字符串是否以当前分割关键字开头
            if (trimmedStr.startsWith(splitStr)) {
                // 若匹配，进行字符串切割和填充操作
                result = StrUtil.subBefore(str, splitStr, false) +
                        StrUtil.fill("", ' ', splitStr.length(), false) +
                        StrUtil.subAfter(str, splitStr, false);
                // 记录切割前后的字符串信息，方便调试
                log.debug("字符串 [{}] 切割后变为 [{}]", str, result);
                break;
            }
        }
        return result;
    }

    /**
     * 移除字符串中首次出现的指定字符，并在该位置填充一个空格。
     *
     * @param str 待处理的字符串
     * @param ch  要移除的字符
     * @return 处理后的字符串，如果字符串中不包含指定字符则返回原字符串
     */
    public static String removeFirstOccurence(String str, char ch) {
        // 查找指定字符在字符串中首次出现的位置
        int index = str.indexOf(ch);
        if (index == -1) {
            // 若未找到指定字符，记录信息并返回原字符串
            log.debug("字符串 [{}] 中未找到字符 [{}]，不做处理", str, ch);
            return str;
        } else {
            // 若找到，移除该字符并在相应位置填充空格
            String result = str.substring(0, index) + " " + str.substring(index + 1);
            // 记录处理前后的字符串信息，方便调试
            log.debug("字符串 [{}] 移除字符 [{}] 后变为 [{}]", str, ch, result);
            return result;
        }
    }
}