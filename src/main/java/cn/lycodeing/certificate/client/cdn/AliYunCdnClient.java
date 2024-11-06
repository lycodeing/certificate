package cn.lycodeing.certificate.client.cdn;

import cn.lycodeing.certificate.context.CdnTaskData;
import com.aliyun.cas20200407.Client;
import com.aliyun.cas20200407.models.UploadUserCertificateRequest;
import com.aliyun.cas20200407.models.UploadUserCertificateResponse;
import com.aliyun.cdn20180510.models.SetCdnDomainSSLCertificateRequest;
import com.aliyun.cdn20180510.models.SetCdnDomainSSLCertificateResponse;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class AliYunCdnClient {

    private static final AliYunCdnClient INSTANCE = new AliYunCdnClient();

    private static final String SSL_API_ENDPOINT = "cas.aliyuncs.com";
    private static final String CDN_API_ENDPOINT = "cdn.aliyuncs.com";

    private Client sslClient;

    private com.aliyun.cdn20180510.Client cdnClient;


    private AliYunCdnClient(){}

    public static AliYunCdnClient getInstance() {
        return INSTANCE;
    }


    public int executeJob(CdnTaskData processorData, String cert, String privateKey) {
        try {
            this.sslClient = createSslClient(processorData.getAccessKey(), processorData.getAccessSecret());
            this.cdnClient = createCdnClient(processorData.getAccessKey(), processorData.getAccessSecret());
            Long certId = uploadSsl(processorData, cert, privateKey);
            updateCdnSsl(processorData.getCdnDomain(), certId);
        } catch (Exception e) {
            log.error("AliYunCdnClient Error: {}", e.getMessage(), e);
            return -1;
        }
        return 0;

    }

    private void updateCdnSsl(String cdnDomain, Long certId) throws Exception {
        log.info("证书上传已完成,开始准备更新CDN证书, cdn域名为：{} ,证书ID：{}", cdnDomain, certId);
        if (StringUtils.isEmpty(cdnDomain)) {
            log.warn("未配置CDN域名，跳过CDN证书更新");
            return;
        }
        SetCdnDomainSSLCertificateRequest request = new SetCdnDomainSSLCertificateRequest();
        request.setCertId(certId);
        request.setDomainName(cdnDomain);
        request.setSSLProtocol("on");
        request.setCertRegion("cn-hangzhou");
        SetCdnDomainSSLCertificateResponse res = cdnClient.setCdnDomainSSLCertificateWithOptions(request, new RuntimeOptions());
        if (res.getStatusCode() != 200) {
            log.error("CDN证书更新失败，请求ID：{}", res.getBody().getRequestId());
        }
        log.info("CDN证书更新成功，请求ID：{}", res.getBody().getRequestId());
    }


    private Long uploadSsl(CdnTaskData cdnPostProcessorData, String cert, String privateKey) throws Exception {
        log.info("开始上传SSL证书，证书别名为：{}", cdnPostProcessorData.getAlisaName());
        UploadUserCertificateRequest request = new UploadUserCertificateRequest();
        request.setName(cdnPostProcessorData.getAlisaName());
        request.setCert(cert);
        request.setKey(privateKey);
        UploadUserCertificateResponse response = sslClient.uploadUserCertificateWithOptions(request, new RuntimeOptions());
        return response.getBody().getCertId();
    }


    private Client createSslClient(String accessKeyId, String accessKeySecret) throws Exception {
        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setEndpoint(SSL_API_ENDPOINT)
                .setAccessKeySecret(accessKeySecret);
        return new Client(config);
    }


    private static com.aliyun.cdn20180510.Client createCdnClient(String accessKeyId, String accessKeySecret) throws Exception {
        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setEndpoint(CDN_API_ENDPOINT)
                .setAccessKeySecret(accessKeySecret);
        return new com.aliyun.cdn20180510.Client(config);
    }

}
