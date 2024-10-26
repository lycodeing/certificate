package cn.lycodeing.certificate.processor;

import cn.lycodeing.certificate.context.Context;
import cn.lycodeing.certificate.context.TencentContext;
import cn.lycodeing.certificate.enums.PostProcessorTypeEnum;
import cn.lycodeing.certificate.utils.FileUtil;
import cn.lycodeing.certificate.utils.GsonUtil;
import com.tencentcloudapi.cdn.v20180606.CdnClient;
import com.tencentcloudapi.cdn.v20180606.models.ModifyDomainConfigRequest;
import com.tencentcloudapi.cdn.v20180606.models.ModifyDomainConfigResponse;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.ssl.v20191205.SslClient;
import com.tencentcloudapi.ssl.v20191205.models.UploadCertificateRequest;
import com.tencentcloudapi.ssl.v20191205.models.UploadCertificateResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Map;

import static cn.lycodeing.certificate.constant.CommonConstant.CERT_SUFFIX;
import static cn.lycodeing.certificate.constant.CommonConstant.KEY_SUFFIX;

@Slf4j
public class TencentCertPostProcessor implements CertPostProcessor {

    private static final String SSL_ENDPOINT = "ssl.tencentcloudapi.com";

    private static final String CDN_ENDPOINT = "cdn.tencentcloudapi.com";

    private Credential credential;

    @Override
    public void postProcess(Context context) {
        try {
            TencentContext tencentContext = GsonUtil.fromJson(context.getPostProcessorData(), TencentContext.class);
            credential = new Credential(tencentContext.getAccessKey(), tencentContext.getAccessSecret());
            String certId = uploadSsl(tencentContext.getAlisaName(), getPrivateKey(context), getCert(context));
            updateCdnSsl(tencentContext.getCdnDomain(), certId);
        } catch (TencentCloudSDKException e) {
            log.error("Sending request failed, {} , requestId:{}", e.getMessage(), e.getRequestId(), e);
        } catch (Exception e) {
            log.error("TencentCertPostProcessor Error: {}", e.getMessage());
        }
    }

    private String uploadSsl(String aliasName, String privateKey, String publicKey) throws TencentCloudSDKException {
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint(SSL_ENDPOINT);
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        // 实例化要请求产品的client对象,clientProfile是可选的
        SslClient client = new SslClient(credential, "", clientProfile);
        // 实例化一个请求对象,每个接口都会对应一个request对象
        UploadCertificateRequest req = new UploadCertificateRequest();
        req.setCertificatePublicKey(publicKey);
        req.setCertificatePrivateKey(privateKey);
        req.setAlias(aliasName);
        // 返回的resp是一个UploadCertificateResponse的实例，与请求对象对应
        UploadCertificateResponse resp = client.UploadCertificate(req);
        String certificateId = resp.getCertificateId();
        log.info("证书上传成功，证书ID为：{}", certificateId);
        return certificateId;
    }


    private void updateCdnSsl(String cdnDomain, String certificateId) throws TencentCloudSDKException {
        if (StringUtils.isEmpty(cdnDomain)) {
            log.warn("未配置CDN域名，跳过CDN证书更新");
            return;
        }
        log.info("开始更新CDN证书, cdn域名为：{} ,证书ID：{}", cdnDomain, certificateId);
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint(CDN_ENDPOINT);
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        CdnClient client = new CdnClient(credential, "", clientProfile);
        // 实例化一个请求对象,每个接口都会对应一个request对象
        ModifyDomainConfigRequest req = new ModifyDomainConfigRequest();
        req.setDomain(cdnDomain);
        req.setRoute("Https.CertInfo.CertId");
        req.setValue(GsonUtil.toJson(Map.of("update", certificateId)));
        // 返回的resp是一个ModifyDomainConfigResponse的实例，与请求对象对应
        ModifyDomainConfigResponse resp = client.ModifyDomainConfig(req);
        String requestId = resp.getRequestId();
        log.info("CDN证书更新成功，requestId为：{}", requestId);
    }


    private String getCert(Context context) throws IOException {
        return FileUtil.readFileAsString(context.getCertPath() + context.getOutput().get("crtFileName") + CERT_SUFFIX);
    }

    private String getPrivateKey(Context context) throws IOException {
        return FileUtil.readFileAsString(context.getCertPath() + context.getOutput().get("keyFileName") + KEY_SUFFIX);
    }


    @Override
    public boolean isPostProcessorType(PostProcessorTypeEnum postProcessorType) {
        return false;
    }
}
