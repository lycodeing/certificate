package cn.lycodeing.certificate.client.cdn;

import cn.lycodeing.certificate.context.CdnTaskData;
import cn.lycodeing.certificate.utils.GsonUtil;
import cn.lycodeing.certificate.utils.HttpClientUtil;
import com.google.gson.annotations.SerializedName;
import com.qiniu.util.Auth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;


@Slf4j
public class QiNiuCdnClient {
    private static final QiNiuCdnClient INSTANCE = new QiNiuCdnClient();
    private static final String END_POINT = "http://api.qiniu.com";
    private static final String UPLOAD_URL = "/sslcert";
    private static final String CDN_CERT_UPDATE_URL = "/domain/%s/httpsconf";


    private QiNiuCdnClient() {}

    public static QiNiuCdnClient getInstance() {
        return INSTANCE;
    }

    public int executeJob(CdnTaskData cdnTaskData, String cert, String key) {
        try {
            SslRequest sslRequest = createSslRequest(cdnTaskData, cert, key);
            String responseStr = uploadCertificate(sslRequest, cdnTaskData);
            handleUploadResponse(responseStr);
            log.info("Certificate uploaded successfully ...........");
            String certId = extractCertId(responseStr);
            updateCdnCert(certId, cdnTaskData);
        } catch (Exception e) {
            log.error("Failed to upload certificate", e);
            return -1;
        }
        return 0;
    }


    private SslRequest createSslRequest(CdnTaskData postProcessorData, String cert, String key) throws IOException {
        return SslRequest.builder()
                .name(postProcessorData.getDomain())
                .commonName(postProcessorData.getAlisaName())
                .privateKey(key)
                .certificate(cert)
                .build();
    }

    private String uploadCertificate(SslRequest sslRequest, CdnTaskData postProcessorData) throws IOException {
        Auth auth = Auth.create(postProcessorData.getAccessKey(), postProcessorData.getAccessSecret());
        String accessToken = getAccessToken(auth, END_POINT + UPLOAD_URL, "POST", GsonUtil.toJson(sslRequest), "application/json");
        return HttpClientUtil.sendPost(END_POINT + UPLOAD_URL, GsonUtil.toJson(sslRequest).getBytes(),
                Map.of("Authorization", accessToken));
    }

    private void handleUploadResponse(String responseStr) {
        SslResponse response = GsonUtil.fromJson(responseStr, SslResponse.class);
        if (!isSuccessResponse(response)) {
            log.error("证书上传失败,{}", responseStr);
            throw new RuntimeException("证书上传失败");
        }
    }

    private String getAccessToken(Auth auth, String url, String method, String requestData, String contentType) {
        return (String) auth.authorizationV2(url, method, requestData.getBytes(StandardCharsets.UTF_8), contentType).get("Authorization");
    }

    private String extractCertId(String responseStr) {
        SslResponse response = GsonUtil.fromJson(responseStr, SslResponse.class);
        return response != null ? response.getCertId() : null;
    }

    private void updateCdnCert(String certId, CdnTaskData postProcessorData) throws IOException {
        log.info("开始更新CDN证书............");
        if (StringUtils.isEmpty(postProcessorData.getCdnDomain())) {
            log.info("未配置CDN域名,跳过CDN证书更新");
            return;
        }
        String cdnUrl = END_POINT + String.format(CDN_CERT_UPDATE_URL, postProcessorData.getCdnDomain());
        CdnRequest cdnRequest = new CdnRequest(certId);
        String cdnData = GsonUtil.toJson(cdnRequest);
        Auth auth = Auth.create(postProcessorData.getAccessKey(), postProcessorData.getAccessSecret());
        String cdnAccessToken = getAccessToken(auth, cdnUrl, "PUT", cdnData, "application/json");

        String cdnRespStr = HttpClientUtil.sendPut(cdnUrl, cdnData.getBytes(StandardCharsets.UTF_8),
                Map.of("Authorization", cdnAccessToken));

        CdnResponse cdnResponse = GsonUtil.fromJson(cdnRespStr, CdnResponse.class);
        handleCdnResponse(cdnResponse);
    }

    private void handleCdnResponse(CdnResponse cdnResponse) {
        if (!isSuccessResponse(cdnResponse)) {
            log.error("CDN证书更新失败,{}", cdnResponse);
            throw new RuntimeException("CDN证书更新失败");
        }
        log.info("CDN证书更新成功");
    }

    private boolean isSuccessResponse(SslResponse response) {
        return response != null && response.code != null && response.code == 200;
    }

    private boolean isSuccessResponse(CdnResponse response) {
        return response != null && (response.code == null || response.code == 200);
    }


    @Data
    @Builder
    static class SslRequest {
        private String name;
        @SerializedName("common_name")
        private String commonName;
        @SerializedName("pri")
        private String privateKey;
        @SerializedName("ca")
        private String certificate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class CdnRequest {
        private String certId;
        private boolean forceHttps = false;
        private boolean http2Enable = false;

        public CdnRequest(String certId) {
            this.certId = certId;
        }
    }

    @Data
    static class SslResponse {
        private Integer code;
        private String error;
        @SerializedName("certID")
        private String certId;
    }

    @Data
    static class CdnResponse {
        private Integer code;
    }
}
