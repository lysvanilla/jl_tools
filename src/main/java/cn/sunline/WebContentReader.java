package cn.sunline;
import cn.hutool.http.HttpUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class WebContentReader {

    public static Map<String, String> extractLinksFromPage(String url) {
        Map<String, String> linkMap = new LinkedHashMap<>();
        try {
            // 使用 Hutool 的 HttpUtil 发送 GET 请求获取页面内容
            String html = HttpUtil.get(url);
            // 使用 Jsoup 解析 HTML 内容
            Document doc = Jsoup.parse(html);

            Elements lis = doc.select("li.cl");

            for (Element li : lis) {
                Element a = li.selectFirst("a.left");
                String href = a.attr("href");
                String name = a.text();
                String time = li.selectFirst("span.right").text();

                if (!name.isEmpty() && !href.isEmpty() && name.contains("结果")) {
                    // 将名称和对应的 href 存储到 HashMap 中
                    linkMap.put(name, href+"\t"+time);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return linkMap;
    }

    public static Map<String, String> extractLinksByPage(int startPage, int endPage) {
        Map<String, String> allLinksMap = new LinkedHashMap<>();
        String baseUrl = "http://www.scrcu.com/other/zbcg/index_";
        for (int page = startPage; page <= endPage; page++) {
            String url = baseUrl + page + ".html";
            Map<String, String> pageLinksMap = extractLinksFromPage(url);
            allLinksMap.putAll(pageLinksMap);
        }
        return allLinksMap;
    }

    public static void main(String[] args) {
        int startPage = 1;
        int endPage = 841;  //841
        Map<String, String> allLinksMap = extractLinksByPage(startPage, endPage);
        // 遍历 HashMap 并打印结果
        int index_no = 1;
        for (Map.Entry<String, String> entry : allLinksMap.entrySet()) {
            System.out.println(index_no+"\t" + entry.getKey() + "\thttp://www.scrcu.com" + entry.getValue());
            // System.out.println("http://www.scrcu.com" + entry.getValue());
            index_no++;
        }
        System.out.println("11");
    }
}