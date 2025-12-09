package cn.sunline.oa;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.FastExcel;
import cn.idev.excel.write.metadata.WriteSheet;
import cn.sunline.util.BasicInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class OaManagerQuery {
    // OAuth认证地址
    private static final String AUTH_URL = "https://iboss.sunline.cn/PCMC/authentication/system/login";
    // 定义模板文件路径，使用 BasicInfo 类中的 tpl_path 拼接模板文件所在目录和文件名
    private static final String TPL_PATH = BasicInfo.TPL_PATH + "excel" + File.separator + "日报清单模板.xlsx";
    // 定义基础导出路径，使用 BasicInfo 类的方法获取
    private static final String BASIC_EXPORT_PATH = BasicInfo.getBasicExportPath("");

    // 最大查询区间（3个月，单位：天）
    private static final int MAX_INTERVAL_DAYS = 88;

    public static void main(String[] args) {
        // 第一步：获取访问令牌
        String accessToken = getAccessToken();
        if (accessToken == null) {
            System.out.println("获取Token失败");
            return;
        }
        // 示例查询日期范围，可根据实际需求修改
        String startDate = "2025-01-01";
        String endDate = "2025-12-31";
        String workName = "唐凯平";
        List<DailyReport> dailyReports = getAdDyDailyReport(accessToken, startDate, endDate,workName);
        writeIndexExcel(dailyReports, TPL_PATH, BASIC_EXPORT_PATH + "日报清单.xlsx");
        System.out.println("导出完成");
    }

    /**
     * 获取OAuth访问令牌
     */
    private static String getAccessToken() {
        try {
            JSONObject requestData = new JSONObject();
            requestData.set("username", "zouzhi");
            requestData.set("password", "4tZ3fJ9QKTVpZxZYVVowdw==");
            requestData.set("verifyCode", "");

            HttpResponse response = HttpRequest.post(AUTH_URL)
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .body(requestData.toString())
                    .execute();

            System.out.println("Response status: " + response.getStatus());

            if (response.isOk()) {
                JSONObject json = new JSONObject(response.body());
                JSONObject data = json.getJSONObject("data");
                return data.getStr("access_token");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 按日期范围查询日报，自动处理超过3个月的区间
     */
    public static List<DailyReport> getAdDyDailyReport(String token, String startDate, String endDate, String workName) {
        List<DailyReport> allReports = new ArrayList<>();
        // 拆分日期区间
        List<DateRange> dateRanges = splitDateRange(startDate, endDate);

        for (DateRange range : dateRanges) {
            log.info("查询区间: {} 至 {}", range.start, range.end);
            // 构建该区间的请求参数
            JSONObject requestBody = new JSONObject();
            requestBody.set("currentPage", 1);
            requestBody.set("workName", workName);
            requestBody.set("startDate", range.start);
            requestBody.set("endDate", range.end);
            requestBody.set("pageSize", 100);

            // 处理该区间的分页查询
            HttpResponse response = HttpRequest.post("https://iboss.sunline.cn/AD/adDyDailyReport/query/manage/page?permission=manage")
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Token", token)
                    .body(requestBody.toString())
                    .execute();

            if (response.isOk()) {
                JSONObject json = new JSONObject(response.body());
                JSONObject pageParam = json.getJSONObject("pageParam");
                Integer totalPage = pageParam.getInt("totalPage", 0);
                Integer currentPage = 0;

                while (currentPage < totalPage) {
                    currentPage++;
                    log.info("处理区间 {} 至 {} 的第 {} 页数据", range.start, range.end, currentPage);
                    List<DailyReport> pageReports = getAdDyDailyReportByPage(token, currentPage, requestBody);
                    allReports.addAll(pageReports);
                }
            } else {
                log.error("查询区间 {} 至 {} 失败，状态码：{}", range.start, range.end, response.getStatus());
            }
        }

        return allReports;
    }

    /**
     * 拆分日期范围为多个3个月以内的区间
     */
    private static List<DateRange> splitDateRange(String startDateStr, String endDateStr) {
        List<DateRange> ranges = new ArrayList<>();
        Date startDate = DateUtil.parse(startDateStr);
        Date endDate = DateUtil.parse(endDateStr);

        // 如果区间小于等于3个月，直接返回
        if (DateUtil.betweenDay(startDate, endDate,true) <= MAX_INTERVAL_DAYS) {
            ranges.add(new DateRange(startDateStr, endDateStr));
            return ranges;
        }

        // 否则按3个月拆分
        Date currentStart = startDate;
        while (currentStart.before(endDate)) {
            // 计算当前区间结束日期（当前开始日期+3个月减1天，或总结束日期）
            // 先计算3个月后的日期
            Date threeMonthsLater = DateUtil.offsetMonth(currentStart, 3);
            // 减去1天得到3个月内的最后一天
            Date currentEnd = DateUtil.offsetDay(threeMonthsLater, -1);

            if (currentEnd.after(endDate) || currentEnd.equals(endDate)) {
                currentEnd = endDate;
            }

            ranges.add(new DateRange(
                    DateUtil.format(currentStart, "yyyy-MM-dd"),
                    DateUtil.format(currentEnd, "yyyy-MM-dd")
            ));

            // 下一个区间开始日期为当前结束日期的第二天
            currentStart = DateUtil.offsetDay(currentEnd, 1);
        }

        return ranges;
    }

    /**
     * 按页查询日报数据
     */
    public static List<DailyReport> getAdDyDailyReportByPage(String token, Integer currentPage, JSONObject requestBody) {
        String url = "https://iboss.sunline.cn/AD/adDyDailyReport/query/manage/page?permission=manage";
        List<DailyReport> dailyReports = new ArrayList<>();
        requestBody.set("currentPage", currentPage);

        HttpResponse response = HttpRequest.post(url)
                .header("Content-Type", "application/json")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Token", token)
                .body(requestBody.toString())
                .execute();

        if (response.isOk()) {
            JSONObject json = new JSONObject(response.body());
            JSONArray dataArray = json.getJSONArray("data");

            for (Object item : dataArray) {
                JSONObject itemObj = (JSONObject) item;
                DailyReport dailyReport = JSONUtil.toBean(itemObj, DailyReport.class);
                String workLocationName = dailyReport.getWorkLocationName();
                String baseAddressName = dailyReport.getBaseAddressName();

                // 处理出差状态
                String ifTrip = "是";
                if (workLocationName != null && workLocationName.equals(baseAddressName)) {
                    ifTrip = "否";
                }
                dailyReport.setIfTrip(ifTrip);

                // 处理出勤天数
                double attendanceDays = 1;
                String timeline = dailyReport.getTimeline();
                if (timeline != null && !timeline.equals("T01")) {
                    attendanceDays = 0.5;
                }
                dailyReport.setAttendanceDays(attendanceDays);

                dailyReports.add(dailyReport);
            }
        } else {
            System.err.println("请求失败，状态码：" + response.getStatus());
        }

        return dailyReports;
    }

    /**
     * 写入Excel文件
     */
    public static void writeIndexExcel(List<DailyReport> dailyReports, String templatePath, String outputPath) {
        File templateFile = new File(templatePath);
        if (!templateFile.exists()) {
            log.error("Excel 模板文件不存在，路径：{}", templatePath);
            return;
        }

        ExcelWriter excelWriter = null;
        try {
            excelWriter = FastExcel.write(outputPath).withTemplate(templatePath).build();
            WriteSheet task_sheet = FastExcel.writerSheet("日报信息").build();
            log.info("开始向 Excel 文件写入 {} 条指标信息，输出路径：{}", dailyReports.size(), outputPath);
            excelWriter.fill(dailyReports, task_sheet);
            log.info("成功向 Excel 文件写入数据，输出路径：{}", outputPath);
        } catch (Exception e) {
            log.error("写入 Excel 文件时出现异常，输出路径：{}", outputPath, e);
        } finally {
            if (excelWriter != null) {
                try {
                    excelWriter.close();
                } catch (Exception e) {
                    log.error("关闭 ExcelWriter 时出现异常，输出路径：{}", outputPath, e);
                }
            }
        }
        log.info("转换成功：[{}]", outputPath);
    }

    /**
     * 日期范围内部类
     */
    private static class DateRange {
        String start;
        String end;

        DateRange(String start, String end) {
            this.start = start;
            this.end = end;
        }
    }
}