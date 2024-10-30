package cn.lycodeing.certificate.processor.impl;

import cn.lycodeing.certificate.context.AliYunContext;
import cn.lycodeing.certificate.context.Context;
import cn.lycodeing.certificate.processor.CertPostProcessor;
import cn.lycodeing.certificate.utils.FileUtil;
import cn.lycodeing.certificate.utils.GsonUtil;
import com.aliyun.cas20200407.Client;
import com.aliyun.cas20200407.models.UploadUserCertificateRequest;
import com.aliyun.cas20200407.models.UploadUserCertificateResponse;
import com.aliyun.cdn20180510.models.SetCdnDomainSSLCertificateRequest;
import com.aliyun.cdn20180510.models.SetCdnDomainSSLCertificateResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

import static cn.lycodeing.certificate.constant.CommonConstant.CERT_SUFFIX;
import static cn.lycodeing.certificate.constant.CommonConstant.KEY_SUFFIX;

@Slf4j
public class AliYunCertPostProcessor implements CertPostProcessor {
    private static final String SSL_API_ENDPOINT = "cas.aliyuncs.com";
    private static final String CDN_API_ENDPOINT = "cdn.aliyuncs.com";

    private Client sslClient;

    private com.aliyun.cdn20180510.Client cdnClient;

    private AliYunCertPostProcessor() {
    }

    public static AliYunCertPostProcessor create() {
        return new AliYunCertPostProcessor();
    }

    @Override
    public void postProcess(Context context) {
        try {
            AliYunContext aliYunContext = GsonUtil.fromJson(context.getPostProcessorData(), AliYunContext.class);
            this.sslClient = createSslClient(aliYunContext.getAccessKey(), aliYunContext.getAccessSecret());
            this.cdnClient = createCdnClient(aliYunContext.getAccessKey(), aliYunContext.getAccessSecret());
            Long certId = uploadSsl(aliYunContext, getCert(context), getPrivateKey(context));
            updateCdnSsl(aliYunContext.getCdnDomain(), certId);
        } catch (TeaException ex) {
            log.error("AliYunCertPostProcessor Error: {} ,Diagnostic address: {}", ex.getMessage(), ex.getData().get("Recommend"), ex);
        } catch (Exception e) {
            log.error("AliYunCertPostProcessor Error: {}", e.getMessage(), e);
        }
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

    public Long uploadSsl(AliYunContext aliYunContext, String cert, String privateKey) throws Exception {
        log.info("开始上传SSL证书，证书别名为：{}", aliYunContext.getAlisaName());
        UploadUserCertificateRequest request = new UploadUserCertificateRequest();
        request.setName(aliYunContext.getAlisaName());
        request.setCert(cert);
        request.setKey(privateKey);
        UploadUserCertificateResponse response = sslClient.uploadUserCertificateWithOptions(request, new RuntimeOptions());
        return response.getBody().getCertId();
    }

    private String getCert(Context context) throws IOException {
        return FileUtil.readFileAsString(context.getCertPath() + context.getOutput().get("crtFileName") + CERT_SUFFIX);
    }

    private String getPrivateKey(Context context) throws IOException {
        return FileUtil.readFileAsString(context.getCertPath() + context.getOutput().get("keyFileName") + KEY_SUFFIX);
    }


    public Client createSslClient(String accessKeyId, String accessKeySecret) throws Exception {
        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setEndpoint(SSL_API_ENDPOINT)
                .setAccessKeySecret(accessKeySecret);
        return new Client(config);
    }


    public static com.aliyun.cdn20180510.Client createCdnClient(String accessKeyId, String accessKeySecret) throws Exception {
        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setEndpoint(CDN_API_ENDPOINT)
                .setAccessKeySecret(accessKeySecret);
        return new com.aliyun.cdn20180510.Client(config);
    }
}
