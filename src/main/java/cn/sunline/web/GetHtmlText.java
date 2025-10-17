package cn.sunline.web;

import cn.hutool.http.HttpUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class GetHtmlText {
    public static void main(String[] args) {
        String url = "http://www.hunan-bank.com/96599/2025-08/08/article_2025080814495411689.shtml";
        System.out.println(getHtmlText(url));
    }

    public static String getHtmlText(String url) {
        String html = HttpUtil.get(url);
        // 使用 Jsoup 解析 HTML 内容
        Document doc = Jsoup.parse(html);
        String htmlText = "";

        //Element lis = doc.selectFirst("div[style='width:100%;overflow-x: auto;']");
        Element lis = doc.getElementById("contentBox");

        if (lis != null) {
            String htmlContent = lis.html();

            // 2. 将 <br>、<br/> 标签替换为换行符 \n
            // 若有 <p> 标签，可同时替换为换行符（根据实际网页结构调整）

            htmlText = htmlContent
                    .replaceAll("<br\\s*/?>", "\n")  // 替换<br>或<br/>为换行
                    .replaceAll("</p>", "\n")        // 替换</p>为换行（可选，根据网页结构调整）
                    .replaceAll("<[^>]+>", "")        // 去除所有HTML标签
                    .replaceAll("\\s*\n\\s*", "\n")  // 去除每行前后的空白（如空格、制表符）
                    .replaceAll("\n+", "\n")         // 合并连续换行（去除空白行）
                    .trim();
        }
        return htmlText;
    }
}
