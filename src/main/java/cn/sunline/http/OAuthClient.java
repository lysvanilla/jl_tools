package cn.sunline.http;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.util.ArrayList;
import java.util.List;

public class OAuthClient {

    public static void main(String[] args) {
        try {
            // 创建信任所有证书的HttpClient
            SSLContext sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
                    .build();

            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                    sslContext, NoopHostnameVerifier.INSTANCE);

            HttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(sslSocketFactory)
                    .build();

            // 创建POST请求
            HttpPost httpPost = new HttpPost(
                    "https://172.26.0.118:32352/oauth/token?client_id=long_time_user&client_seq");

            // 设置请求参数（表单格式）
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("client_secret", "secret"));
            params.add(new BasicNameValuePair("username", "admin"));
            params.add(new BasicNameValuePair("password", "admin"));
            params.add(new BasicNameValuePair("grant_type", "password")); // 通常需要包含grant_type

            httpPost.setEntity(new UrlEncodedFormEntity(params));

            // 发送请求并处理响应
            HttpResponse response = httpClient.execute(httpPost);

            String responseBody = EntityUtils.toString(response.getEntity());
            System.out.println("Response Code: " + response.getStatusLine().getStatusCode());
            System.out.println("Response Body: " + responseBody);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}