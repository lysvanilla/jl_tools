package cn.sunline.http;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;

public class HutoolOAuthClient {

    public static void main(String[] args) {
        // 目标URL（包含查询参数）
        String url = "https://172.26.0.118:32352/oauth/token?client_id=long_time_user";

        try {
            // 发送POST请求（自动处理HTTPS）
            HttpResponse response = HttpRequest.post(url)
                    // 禁用SSL验证（仅测试环境使用）
                    .setSSLProtocol("SSLv3")
                    // 设置表单参数
                    .form("client_secret", "secret")
                    .form("username", "admin")
                    .form("password", "admin")
                    .form("grant_type", "password") // 必须的OAuth参数
                    .timeout(5000) // 设置超时时间
                    .execute();

            /*
                // 自定义SSL配置（生产环境推荐）
                HttpRequest.post(url)
                    .setSSLSocketFactory(new TrustAllSSLSocketFactory()) // 自定义证书验证逻辑

                // 添加请求头
                .header("X-Custom-Header", "value")

                // 使用代理
                .setProxy("127.0.0.1", 8080)

                // 异步请求
                HttpRequest.post(url).async().thenAccept(res -> {
                    System.out.println("异步响应: " + res.body());
                });
             */

            // 处理响应
            if (response.isOk()) {
                JSONObject json = new JSONObject(response.body());
                System.out.println("访问令牌: " + json.getStr("access_token"));
                System.out.println("刷新令牌: " + json.getStr("refresh_token"));
            } else {
                System.out.println("请求失败: " + response.getStatus());
                System.out.println("响应内容: " + response.body());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}