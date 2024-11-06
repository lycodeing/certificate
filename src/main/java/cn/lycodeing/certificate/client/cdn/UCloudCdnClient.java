package cn.lycodeing.certificate.client.cdn;

import cn.lycodeing.certificate.context.CdnTaskData;
import cn.lycodeing.certificate.utils.GsonUtil;
import cn.lycodeing.certificate.utils.HttpClientUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.Md5Crypt;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Slf4j
public class UCloudCdnClient {

    private static final UCloudCdnClient INSTANCE = new UCloudCdnClient();

    private static final String API_ENDPOINT = "https://api.ucloud.cn/";


    private UCloudCdnClient(){}

    public static UCloudCdnClient getInstance() {
        return INSTANCE;
    }


    public int executeJob(CdnTaskData cdnTaskData, String cert, String key) {
        try {
            Map<String, String> sslParams = getSslParams(cdnTaskData, cert, key);
            Integer certId = uploadSSl(sslParams);
            String domainId = getCdnList(cdnTaskData);
            updateCdnSsl(cdnTaskData, domainId, certId);
        } catch (Exception e) {
            log.error("Parse Context Error: {}", e.getMessage(), e);
            return -1;
        }
        return 0;
    }


    private Integer uploadSSl(Map<String, String> sslParams) throws IOException {
        log.info("Upload Normal Certificate");
        log.debug("Upload Normal Certificate Request: {}", sslParams);
        String respStr = HttpClientUtil.sendPost(API_ENDPOINT, sslParams, null);
        log.debug("Upload Normal Certificate Response: {}", respStr);
        UploadNormalCertificateResponse response = GsonUtil.fromJson(respStr, UploadNormalCertificateResponse.class);
        if (response.getRetCode() != 0) {
            log.error("Upload Normal Certificate Failed, RetCode: {} , ErrorMsg:{}", response.getRetCode(), response.getMessage());
            throw new RuntimeException("Upload Normal Certificate Failed: " + response.getMessage());
        }
        log.info("Upload Normal Certificate Success");
        return response.getCertificateID();
    }


    public void updateCdnSsl(CdnTaskData processorData, String domainId, Integer certId) throws Exception {
        Map<String, String> updateCdnParams = getUpdateCdnParams(processorData, domainId, certId);
        String respStr = HttpClientUtil.sendPost(API_ENDPOINT, updateCdnParams, null);
        UpdateUCdnDomainHttpsConfigV2Response response = GsonUtil.fromJson(respStr, UpdateUCdnDomainHttpsConfigV2Response.class);
        log.debug("Update Cdn SSL Response: {}", respStr);
        if (response.getRetCode() != 0) {
            log.error("Update Cdn SSL Failed, RetCode: {} , ErrorMsg:{}", response.getRetCode(), response.getMessage());
            throw new RuntimeException("Update Cdn SSL Failed: " + response.getMessage());
        }
        log.info("Update Cdn SSL Success");
    }


    public String getCdnList(CdnTaskData processorData) throws Exception {
        log.info("Get Cdn List ...............");
        Map<String, String> cdnListParams = getCdnListParams(processorData);
        log.debug("Get Cdn List Request: {}", cdnListParams);
        String respStr = HttpClientUtil.sendPost(API_ENDPOINT, cdnListParams, null);
        GetUCdnDomainConfigResponse response = GsonUtil.fromJson(respStr, GetUCdnDomainConfigResponse.class);
        log.debug("getCdnList Response: {}", respStr);
        if (response.getRetCode() != 0) {
            log.error("getCdnList Failed, RetCode: {} , ErrorMsg:{}", response.getRetCode(), response.getMessage());
            throw new RuntimeException("getCdnList Failed: " + response.getMessage());
        }
        if (response.getDomainList().isEmpty()) {
            log.warn("No Cdn Domain Found");
            throw new RuntimeException("No Cdn Domain Found");
        }
        DomainConfigInfo info = response.getDomainList().stream().findFirst().get();
        log.info("Cdn DomainId: {}, Cdn domain:{}", info.getDomainId(), processorData.getCdnDomain());
        return info.getDomainId();

    }


    private Map<String, String> getUpdateCdnParams(CdnTaskData processorData, String domainId, Integer certId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("Action", "UpdateUcdnDomainHttpsConfigV2");
        params.put("DomainId", domainId);
        params.put("CertId", String.valueOf(certId));
        params.put("PublicKey", processorData.getAccessKey());
        params.put("HttpsStatusCn", "enable");
        params.put("CertType", "ussl");
        params.put("CertName", processorData.getAlisaName());
        String signature = generateSignature(params, processorData.getAccessSecret());
        params.put("Signature", signature);
        return params;
    }


    public Map<String, String> getCdnListParams(CdnTaskData processorData) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("Action", "GetUcdnDomainConfig");
        params.put("Domain.N", processorData.getCdnDomain());
        params.put("PublicKey", processorData.getAccessKey());
        String signature = generateSignature(params, processorData.getAccessSecret());
        params.put("Signature", signature);
        return params;
    }


    public Map<String, String> getSslParams(CdnTaskData context, String publicKey, String privateKey) throws Exception {
        Map<String, String> params = new HashMap<>(7);
        String publicKeyToBase64 = Base64.getEncoder().encodeToString(publicKey.getBytes(StandardCharsets.UTF_8));
        String privateKeyToBase64 = Base64.getEncoder().encodeToString(privateKey.getBytes(StandardCharsets.UTF_8));
        params.put("Action", "UploadNormalCertificate");
        params.put("CertificateName", context.getAlisaName());
        params.put("SslPublicKey", publicKeyToBase64);
        params.put("SslPrivateKey", privateKeyToBase64);
        params.put("SslMD5", Md5Crypt.md5Crypt((publicKeyToBase64 + privateKeyToBase64).getBytes()));
        params.put("PublicKey", context.getAccessKey());
        String signature = generateSignature(params, context.getAccessSecret());
        params.put("Signature", signature);
        return params;
    }


    // 生成签名
    public static String generateSignature(Map<String, String> params, String privateKey) throws Exception {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        StringBuilder stringBuilder = new StringBuilder();
        for (String key : keys) {
            Object value = params.get(key);
            String valueStr = value != null ? value.toString() : "";
            stringBuilder.append(key).append(valueStr);
        }
        String concatenatedString = stringBuilder + privateKey;
        return sha1(concatenatedString);
    }

    private static String sha1(String plainText) throws NoSuchAlgorithmException, Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        byte[] hashBytes = messageDigest.digest(plainText.getBytes());
        return byteArrayToHexString(hashBytes);
    }

    private static String byteArrayToHexString(byte[] bytes) {
        BigInteger integer = new BigInteger(1, bytes);
        String formatted = String.format("%0" + (bytes.length << 1) + "X", integer);
        return formatted.toLowerCase();
    }


    @Data
    public static class UploadNormalCertificateResponse {

        private int RetCode;
        private String Action;
        private int CertificateID;
        private String LongResourceID;
        private String Message;
    }


    @Data
    public static class GetUCdnDomainConfigResponse {
        private int RetCode;
        private String Action;
        private List<DomainConfigInfo> DomainList;
        private String Message;
    }

    @Data
    public static class DomainConfigInfo {
        private String AreaCode;

        private String CdnType;

        private String Status;

        private String Cname;

        private int CreateTime;

        private String TestUrl;

        private String HttpsStatusCn;

        private String HttpsStatusAbroad;

        private String CertNameCn;

        private String DomainId;

        private String Domain;
    }

    @Data
    public static class UpdateUCdnDomainHttpsConfigV2Response {
        private String Action;

        private String Message;

        private int RetCode;
    }

}
