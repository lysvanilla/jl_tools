package cn.sunline.web;
import cn.hutool.http.HttpUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.Map;

import static cn.sunline.web.GetHtmlText.getHtmlText;

public class WebContentReader_HNYH {

    //湖南银行 https://www.hunan-bank.com/96599/gywx/zbtb/15acd84e-1.shtml
    //以下是网页名称、网点地址、发布日期，请根据网点地址提取项目名称和中标候选人以及中标金额，并通过markdown的表格形式返回网页名称、网点地址、发布日期、项目名称、中标候选人、中标金额
    public static Map<String, String> extractLinksFromPage(String url) {
        Map<String, String> linkMap = new LinkedHashMap<>();
        try {
            // 使用 Hutool 的 HttpUtil 发送 GET 请求获取页面内容
            String html = HttpUtil.get(url);
            // 使用 Jsoup 解析 HTML 内容
            Document doc = Jsoup.parse(html);

            Elements lis = doc.select("div.list");

            for (Element li : lis) {
                Elements a_lis = li.select("a");
                for (Element a_top : a_lis) {
                    Element a = a_top.selectFirst("a");
                    String href = a.attr("href");
                    String name = a.text();
                    String time = a.selectFirst("p.time").text();

                    if (!name.isEmpty() && !href.isEmpty() && name.contains("中标")) {
                        String texturl = "http://www.hunan-bank.com" + href;
                        System.out.println(texturl);
                        String htmlText = getHtmlText(texturl);
                        // 将名称和对应的 href 存储到 HashMap 中
                        linkMap.put(name, href+"\t"+time+"\t"+htmlText);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return linkMap;
    }

    public static Map<String, String> extractLinksByPage(int startPage, int endPage) {
        Map<String, String> allLinksMap = new LinkedHashMap<>();
        String baseUrl = "https://www.hunan-bank.com/96599/gywx/zbtb/15acd84e-";
        for (int page = startPage; page <= endPage; page++) {
            String url = baseUrl + page + ".shtml";
            Map<String, String> pageLinksMap = extractLinksFromPage(url);
            allLinksMap.putAll(pageLinksMap);
        }
        return allLinksMap;
    }

    public static void main(String[] args) {
        int startPage = 1;
        int endPage = 1;
        Map<String, String> allLinksMap = extractLinksByPage(startPage, endPage);
        // 遍历 HashMap 并打印结果
        int index_no = 1;
        for (Map.Entry<String, String> entry : allLinksMap.entrySet()) {
            System.out.println(index_no+"\t" + entry.getKey() + "\thttp://www.hunan-bank.com" + entry.getValue());
            // System.out.println("http://www.scrcu.com" + entry.getValue());
            index_no++;
        }
        System.out.println("11");
    }
}