package cn.lycodeing.certificate.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class HttpClientUtil {
    private static final CloseableHttpClient httpClient;

    static {
        httpClient = HttpClients.createDefault();

        // 注册一个关闭钩子，在 JVM 关闭时关闭 HttpClient
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                log.error("关闭 HttpClient 失败", e);
            }
        }));
    }

    /**
     * 发送 GET 请求
     *
     * @param url    请求的 URL
     * @param params 请求的参数
     * @return 响应的字符串
     * @throws IOException        如果发生 I/O 错误
     * @throws URISyntaxException 如果 URL 不符合语法
     */
    public static String sendGet(String url, Map<String, String> params) throws IOException, URISyntaxException {
        URI uri = getUri(url, params);
        HttpGet httpGet = new HttpGet(uri);
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        }
    }


    /**
     * 发送 POST 请求
     *
     * @param url    请求的 URL
     * @param params 请求的参数
     * @return 响应的字符串
     * @throws IOException        如果发生 I/O 错误
     * @throws URISyntaxException 如果 URL 不符合语法
     */
    public static String sendPost(String url, Map<String, String> params) throws IOException, URISyntaxException {
        URI uri = getUri(url, params);
        HttpPost httpGet = new HttpPost(uri);
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        }
    }

    private static URI getUri(String url, Map<String, String> params) throws URISyntaxException {
        URIBuilder builder = new URIBuilder(url);
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.addParameter(entry.getKey(), entry.getValue());
            }
        }
        URI uri = builder.build();
        return uri;
    }


    /**
     * 发送 POST 请求
     *
     * @param url     请求的 URL
     * @param data    请求体数据
     * @param headers 请求头
     * @return 响应的字符串
     * @throws IOException 如果发生 I/O 错误
     */
    public static String sendPost(String url, String data, Map<String, String> headers) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        if (data != null) {
            httpPost.setEntity(new StringEntity(data, "UTF-8"));
        }
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
        }
        httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        }
    }

    public static String sendPost(String url, byte[] data, Map<String, String> headers) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        if (data != null) {
            httpPost.setEntity(new ByteArrayEntity(data));
        }
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
        }
        httpPost.setHeader("Content-Type", "application/json");
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        }
    }

    public static String sendPut(String url, byte[] data, Map<String, String> headers) throws IOException {
        HttpPut httpPut = new HttpPut(url);
        if (data != null) {
            httpPut.setEntity(new ByteArrayEntity(data));
        }
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPut.setHeader(entry.getKey(), entry.getValue());
            }
        }
        httpPut.setHeader("Content-Type", "application/json");
        try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        }
    }

    /**
     * 发送 POST 请求
     * 默认请求头为 Content-Type: application/x-www-form-urlencoded
     */
    public static String sendPost(String url, Map<String, String> data, Map<String, String> headers) throws IOException {
        HttpPost httpPost = new HttpPost(url);

        // 设置 headers
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
        }
        // 设置 Content-Type 为 application/x-www-form-urlencoded
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

        // 将 map 数据转为 NameValuePair 列表
        if (data != null) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            for (Map.Entry<String, String> entry : data.entrySet()) {
                nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            // 创建 UrlEncodedFormEntity 并与请求关联
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, StandardCharsets.UTF_8));
        }

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            // 返回响应内容
            return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

        }
    }

}
