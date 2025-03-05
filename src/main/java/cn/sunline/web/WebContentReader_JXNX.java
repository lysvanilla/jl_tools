package cn.sunline.web;
import cn.hutool.http.HttpUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.Map;

public class WebContentReader_JXNX {

    //江西省农信 http://www.jxxyzx.cn/
    //以下是网页名称、网点地址、发布日期，请根据网点地址提取项目名称和中标候选人以及中标金额，并通过markdown的表格形式返回网页名称、网点地址、发布日期、项目名称、中标候选人、中标金额
    public static Map<String, String> extractLinksFromPage(String url) {
        Map<String, String> linkMap = new LinkedHashMap<>();
        try {
            // 使用 Hutool 的 HttpUtil 发送 GET 请求获取页面内容
            String html = HttpUtil.get(url);
            // 使用 Jsoup 解析 HTML 内容
            Document doc = Jsoup.parse(html);

            Elements lis = doc.select("ul.list_fl_fr2");

            for (Element li : lis) {
                Elements a_lis = li.select("a");
                for (Element a_top : a_lis) {
                    Element a = a_top.selectFirst("a");
                    String href = a.attr("href");
                    String name = a.text();
                    String time = li.selectFirst("span").text();

                    if (!name.isEmpty() && !href.isEmpty() && name.contains("中标")) {
                        // 将名称和对应的 href 存储到 HashMap 中
                        linkMap.put(name, href+"\t"+time);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return linkMap;
    }



    public static void main(String[] args) {
        String url = "http://www.jxxyzx.cn/";
        Map<String, String> allLinksMap = extractLinksFromPage(url);
        // 遍历 HashMap 并打印结果
        int index_no = 1;
        for (Map.Entry<String, String> entry : allLinksMap.entrySet()) {
            System.out.println(index_no+"\t" + entry.getKey() + "\thttp://www.hnnxs.com" + entry.getValue());
            // System.out.println("http://www.scrcu.com" + entry.getValue());
            index_no++;
        }
        System.out.println("11");
    }
}