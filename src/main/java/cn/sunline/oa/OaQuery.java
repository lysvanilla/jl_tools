package cn.sunline.oa;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.FastExcel;
import cn.idev.excel.write.metadata.WriteSheet;
import cn.resume.entity.Resume;
import cn.sunline.util.BasicInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class OaQuery {
    // OAuth认证地址
    private static final String AUTH_URL = "https://iboss.sunline.cn/PCMC/authentication/system/login";
    // 定义模板文件路径，使用 BasicInfo 类中的 tpl_path 拼接模板文件所在目录和文件名
    private static final String TPL_PATH = BasicInfo.TPL_PATH + "excel" + File.separator + "日报清单模板.xlsx";
    // 定义基础导出路径，使用 BasicInfo 类的方法获取
    private static final String BASIC_EXPORT_PATH = BasicInfo.getBasicExportPath("");

    public static void main(String[] args) {
        // 第一步：获取访问令牌
        String accessToken = getAccessToken();
        if (accessToken == null) {
            System.out.println("获取Token失败");
            return;
        }
        List<DailyReport> dailyReports = getAdDyDailyReport(accessToken);
        writeIndexExcel(dailyReports,TPL_PATH,BASIC_EXPORT_PATH+"日报清单.xlsx");
        System.out.println("aa");

    }

    /**
     * 获取OAuth访问令牌
     */
    private static String getAccessToken() {
        try {
            // 方案1: 使用JSON格式发送数据
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
            //System.out.println("Response body: " + response.body());

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

    public static List<DailyReport> getAdDyDailyReport(String token) {
        // 请求 URL
        String url = "https://iboss.sunline.cn/AD/adDyDailyReport/query/page";
        List<DailyReport> dailyReports = new ArrayList<>();
        // 构建请求体，根据实际 Payload 内容设置
        JSONObject requestBody = new JSONObject();
        requestBody.set("currentPage", 1);
        requestBody.set("empCode", "00603");
        requestBody.set("endDate", "2025-07-31");
        requestBody.set("pageSize", 100);
        //requestBody.set("projectName", "湖南银行指标管理平台2025");
        requestBody.set("startDate", "2025-05-01");

        // 发送 POST 请求
        HttpResponse response = HttpRequest.post(url)
                // 设置请求头，按需补充完整，这里列出关键的
                .header("Content-Type", "application/json")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Token", token) // 重要，需替换
                // 设置请求体
                .body(requestBody.toString())
                // 执行请求
                .execute();

        // 处理响应
        if (response.isOk()) { // 判断响应是否正常
            String result = response.body();
            JSONObject json = new JSONObject(response.body());
            JSONObject data = json.getJSONObject("pageParam");
            Integer totalPage = data.getInt("totalPage");
            Integer currentPage = 0;
            while (currentPage <= totalPage) {
                currentPage ++;
                System.out.println("处理第" + currentPage+"页");
                List<DailyReport> dailyReportsByPage = getAdDyDailyReportByPage(token,currentPage,requestBody);
                dailyReports.addAll(dailyReportsByPage);
            }


            //System.out.println("响应结果：" + dataArray);
        } else {
            System.err.println("请求失败，状态码：" + response.getStatus());
        }

        return dailyReports;
    }

    public static List<DailyReport> getAdDyDailyReportByPage(String token,Integer currentPage,JSONObject requestBody) {
        // 请求 URL
        String url = "https://iboss.sunline.cn/AD/adDyDailyReport/query/page";
        List<DailyReport> dailyReports = new ArrayList<>();
        // 构建请求体，根据实际 Payload 内容设置
        requestBody.set("currentPage", currentPage);

        // 发送 POST 请求
        HttpResponse response = HttpRequest.post(url)
                // 设置请求头，按需补充完整，这里列出关键的
                .header("Content-Type", "application/json")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Token", token) // 重要，需替换
                // 设置请求体
                .body(requestBody.toString())
                // 执行请求
                .execute();

        // 处理响应
        if (response.isOk()) { // 判断响应是否正常
            String result = response.body();
            JSONObject json = new JSONObject(response.body());
            JSONArray dataArray = json.getJSONArray("data");


            // 遍历数组示例，也可根据需求进一步处理每个元素
            for (Object item : dataArray) {
                JSONObject itemObj = (JSONObject) item;
                // 转换JSON字符串为DailyReport对象
                DailyReport dailyReport = JSONUtil.toBean(itemObj, DailyReport.class);
                String workLocationName = dailyReport.getWorkLocationName();
                String baseAddressName = dailyReport.getBaseAddressName();
                String ifTrip= "是";
                if(workLocationName != null || workLocationName.equals(baseAddressName)){
                    ifTrip ="否";
                }
                double  attendanceDays =1;
                String timeline = dailyReport.getTimeline();
                if (!timeline.equals("T01")){
                    attendanceDays = 0.5;
                }
                dailyReport.setIfTrip(ifTrip);
                dailyReports.add(dailyReport);
                dailyReport.setAttendanceDays(attendanceDays);
                //System.out.println("auditDirectorEmpCode: " + itemObj.getStr("auditDirectorEmpCode"));
                //System.out.println("auditDirectorEmpCode: " +dailyReport.getAuditDirectorEmpCode());
            }
            //System.out.println("响应结果：" + dataArray);
        } else {
            System.err.println("请求失败，状态码：" + response.getStatus());
        }

        return dailyReports;
    }

    public static void writeIndexExcel(List<DailyReport> dailyReports, String templatePath, String outputPath) {
        // 创建模板文件和输出文件的 File 对象
        File templateFile = new File(templatePath);
        File outputFile = new File(outputPath);

        // 检查模板文件是否存在
        if (!templateFile.exists()) {
            // 若不存在，记录错误日志
            log.error("Excel 模板文件不存在，路径：{}", templatePath);
            return;
        }

        ExcelWriter excelWriter = null;
        try {
            // 创建 ExcelWriter 对象，使用模板文件进行写入操作
            excelWriter = FastExcel.write(outputPath).withTemplate(templatePath).build();
            // 创建写入工作表对象，指定工作表名称为 "指标数据"
            WriteSheet task_sheet = FastExcel.writerSheet("日报信息").build();
            // 记录开始向 Excel 文件写入数据的日志，包含指标信息数量和输出路径
            log.info("开始向 Excel 文件写入 {} 条指标信息，输出路径：{}", dailyReports.size(), outputPath);
            excelWriter.fill(dailyReports, task_sheet);
            // 记录成功向 Excel 文件写入数据的日志，包含输出路径
            log.info("成功向 Excel 文件写入数据，输出路径：{}", outputPath);
        } catch (Exception e) {
            // 记录写入 Excel 文件时出现异常的日志，包含输出路径和异常信息
            log.error("写入 Excel 文件时出现异常，输出路径：{}", outputPath, e);
        } finally {
            if (excelWriter != null) {
                try {
                    // 关闭 ExcelWriter 对象
                    excelWriter.close();
                } catch (Exception e) {
                    // 记录关闭 ExcelWriter 对象时出现异常的日志，包含输出路径和异常信息
                    log.error("关闭 ExcelWriter 时出现异常，输出路径：{}", outputPath, e);
                }
            }
        }
        // 记录转换成功的日志，包含输出路径
        log.info("转换成功：[{}]", outputPath);
    }


}