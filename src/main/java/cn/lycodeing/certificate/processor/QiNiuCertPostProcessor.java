package cn.lycodeing.certificate.processor;

import cn.lycodeing.certificate.context.Context;
import cn.lycodeing.certificate.context.QiNiuContext;
import cn.lycodeing.certificate.enums.PostProcessorTypeEnum;
import cn.lycodeing.certificate.utils.FileUtil;
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
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static cn.lycodeing.certificate.constant.CommonConstant.CERT_SUFFIX;
import static cn.lycodeing.certificate.constant.CommonConstant.KEY_SUFFIX;

@Slf4j
public class QiNiuCertPostProcessor implements CertPostProcessor {

    private static final String END_POINT = "http://api.qiniu.com";
    private static final String UPLOAD_URL = "/sslcert";
    private static final String CDN_CERT_UPDATE_URL = "/domain/%s/httpsconf";

    private static final Type CERT_RESPONSE_TYPE = new com.google.gson.reflect.TypeToken<Map<String, String>>() {
    }.getType();

    @Override
    public void postProcess(Context context) {
        log.info("开始上传证书............");
        try {
            QiNiuContext qiNiuContext = GsonUtil.fromJson(context.getPostProcessorData(), QiNiuContext.class);
            SslRequest sslRequest = createSslRequest(context, qiNiuContext);
            String responseStr = uploadCertificate(sslRequest, qiNiuContext);
            handleUploadResponse(responseStr);
            log.info("证书上传成功...........");


            String certId = extractCertId(responseStr);
            updateCdnCert(certId, qiNiuContext);
        } catch (Exception e) {
            log.error("上传证书失败", e);
            throw new RuntimeException(e);
        }
    }

    private SslRequest createSslRequest(Context context, QiNiuContext qiNiuContext) throws IOException {
        return SslRequest.builder()
                .name(qiNiuContext.getCommonName())
                .commonName(qiNiuContext.getCommonName())
                .privateKey(getPrivateKey(context))
                .certificate(getCert(context))
                .build();
    }

    private String getCert(Context context) throws IOException {
        return FileUtil.readFileAsString(context.getCertPath() + context.getOutput().get("crtFileName") + CERT_SUFFIX);
    }

    private String getPrivateKey(Context context) throws IOException {
        return FileUtil.readFileAsString(context.getCertPath() + context.getOutput().get("keyFileName") + KEY_SUFFIX);
    }

    private String uploadCertificate(SslRequest sslRequest, QiNiuContext qiNiuContext) throws IOException {
        Auth auth = Auth.create(qiNiuContext.getAccessKey(), qiNiuContext.getAccessSecret());
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

    private void updateCdnCert(String certId, QiNiuContext qiNiuContext) throws IOException {
        log.info("开始更新CDN证书............");
        if (StringUtils.isEmpty(qiNiuContext.getCndDomain())) {
            log.info("未配置CDN域名,跳过CDN证书更新");
            return;
        }
        String cdnUrl = END_POINT + String.format(CDN_CERT_UPDATE_URL, qiNiuContext.getCndDomain());
        CdnRequest cdnRequest = new CdnRequest(certId);
        String cdnData = GsonUtil.toJson(cdnRequest);
        Auth auth = Auth.create(qiNiuContext.getAccessKey(), qiNiuContext.getAccessSecret());
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

    @Override
    public boolean isPostProcessorType(PostProcessorTypeEnum postProcessorType) {
        return PostProcessorTypeEnum.QI_NIU.equals(postProcessorType);
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
