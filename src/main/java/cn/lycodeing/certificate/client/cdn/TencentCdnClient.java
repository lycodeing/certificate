package cn.lycodeing.certificate.client.cdn;

import cn.lycodeing.certificate.context.CdnTaskData;
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

import java.util.Map;

@Slf4j
public class TencentCdnClient {

    private static final TencentCdnClient INSTANCE = new TencentCdnClient();

    private static final String SSL_ENDPOINT = "ssl.tencentcloudapi.com";

    private static final String CDN_ENDPOINT = "cdn.tencentcloudapi.com";

    private Credential credential;

    private TencentCdnClient() {
    }

    public static TencentCdnClient getInstance() {
        return INSTANCE;
    }

    public int executeJob(CdnTaskData cdnTaskData, String cert, String key) {
        try {
            credential = new Credential(cdnTaskData.getAccessKey(), cdnTaskData.getAccessSecret());
            String certId = uploadSsl(cdnTaskData.getAlisaName(), key, cert);
            updateCdnSsl(cdnTaskData.getCdnDomain(), certId);
        } catch (TencentCloudSDKException e) {
            log.error("Sending request failed, {} , requestId:{}", e.getMessage(), e.getRequestId(), e);
        } catch (Exception e) {
            log.error("TencentCdnTask Error: {}", e.getMessage(), e);
            return -1;
        }
        return 0;
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
}
