package cn.sunline.web;

import cn.hutool.http.HttpUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.List;

public class Test {
    public static void main(String[] args) {
        // 使用 Hutool 的 HttpUtil 发送 GET 请求获取页面内容
        String url = "https://www.hunan-bank.com/96599/2025-07/23/article_2025072316222654852.shtml";
        String html = HttpUtil.get(url);
        // 使用 Jsoup 解析 HTML 内容
        Document doc = Jsoup.parse(html);

        Element lis = doc.selectFirst("div[style='width:100%;overflow-x: auto;']");
        String name = lis.text();
        //System.out.println(name);

        String htmlContent = lis.html();

        // 2. 将 <br>、<br/> 标签替换为换行符 \n
        // 若有 <p> 标签，可同时替换为换行符（根据实际网页结构调整）
        String textWithLineBreaks1 = htmlContent
                .replaceAll("<br\\s*/?>", "\n")  // 处理 <br> 或 <br/>
                .replaceAll("</p>", "\n")        // 处理 </p> 结束标签（可选）
                .replaceAll("<[^>]+>", "");      // 去除其他所有 HTML 标签

        String textWithLineBreaks = htmlContent
                .replaceAll("<br\\s*/?>", "\n")  // 替换<br>或<br/>为换行
                .replaceAll("</p>", "\n")        // 替换</p>为换行（可选，根据网页结构调整）
                .replaceAll("<[^>]+>", "")        // 去除所有HTML标签
                .replaceAll("\\s*\n\\s*", "\n")  // 去除每行前后的空白（如空格、制表符）
                .replaceAll("\n+", "\n")         // 合并连续换行（去除空白行）
                .trim();                         // 去除首尾的空白和换行

        // 输出保留换行的文本
        System.out.println(textWithLineBreaks);


    }
}

