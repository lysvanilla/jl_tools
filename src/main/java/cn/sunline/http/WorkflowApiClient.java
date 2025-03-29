package cn.sunline.http;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;

public class WorkflowApiClient {
    // OAuth认证地址
    private static final String AUTH_URL = "https://172.26.0.118:32352/oauth/token";
    // 工作流API地址模板
    private static final String API_URL_TEMPLATE = "http://your-api-domain/studio/api/workflow/v1/tasks/%s/actions/addTaskDependency";

    public static void main(String[] args) {
        // 第一步：获取访问令牌
        String accessToken = getAccessToken();
        if (accessToken == null) {
            System.out.println("获取Token失败");
            return;
        }

        // 第二步：调用工作流API
        String sourceTaskId = "TASK_123";  // 源任务编号
        String relation = "YES";   //依赖关系,可⽤值:YES,NO,OR,WEAK
        String targetTaskId = "TASK_456";  // ⽬标任务编号

        boolean result = addTaskDependency(accessToken, sourceTaskId, relation, targetTaskId);  // 调用API添加任务依赖关系
        System.out.println("操作结果: " + (result ? "成功" : "失败"));
    }

    /**
     * 获取OAuth访问令牌
     */
    private static String getAccessToken() {
        try {
            HttpResponse response = HttpRequest.post(AUTH_URL)
                    .form("client_id", "long_time_user")  //需要根据实际情况修改参数值
                    .form("client_secret", "secret")
                    .form("username", "admin")   //需要根据实际情况修改参数值
                    .form("password", "admin")   //需要根据实际情况修改参数值
                    .form("grant_type", "password")
                    .setSSLProtocol("SSLv3") // 仅测试环境使用
                    .execute();

            if (response.isOk()) {
                JSONObject json = new JSONObject(response.body());
                return json.getStr("access_token");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 添加任务依赖关系
     */
    private static boolean addTaskDependency(String accessToken, String sourceTaskId,
                                             String relation, String targetTaskId) {
        try {
            // 构造API地址
            String apiUrl = String.format(API_URL_TEMPLATE, sourceTaskId);

            // 发送PUT请求
            HttpResponse response = HttpRequest.put(apiUrl)
                    .header("Authorization", "Bearer " + accessToken)
                    .form("relation", relation)
                    .form("targetTaskId", targetTaskId)
                    .execute();

            // 处理响应
            if (response.getStatus() == 200 || response.getStatus() == 201) {
                JSONObject result = new JSONObject(response.body());
                return result.getBool("success");
            }
            System.out.println("请求失败，状态码：" + response.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}