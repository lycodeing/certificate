package cn.lycodeing.certificate.client.cdn;

import cn.lycodeing.certificate.context.CdnTaskData;
import cn.lycodeing.certificate.utils.GsonUtil;
import com.huaweicloud.sdk.core.auth.GlobalCredentials;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.scm.v3.ScmClient;
import com.huaweicloud.sdk.scm.v3.model.*;
import com.huaweicloud.sdk.scm.v3.region.ScmRegion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class HuaWeiCdnClient {

    private static final HuaWeiCdnClient INSTANCE = new HuaWeiCdnClient();

    private ScmClient scmClient;

    private HuaWeiCdnClient() {
    }

    public static HuaWeiCdnClient getInstance() {
        return INSTANCE;
    }


    public int executeJob(CdnTaskData cdnTaskData, String cert, String key) {
        try {
            ICredential credentials = new GlobalCredentials()
                    .withAk(cdnTaskData.getAccessKey())
                    .withSk(cdnTaskData.getAccessSecret());
            scmClient = ScmClient.newBuilder()
                    .withCredential(credentials)
                    .withRegion(ScmRegion.valueOf("cn-north-4"))
                    .build();
            String certId = uploadSsl(cdnTaskData.getAlisaName(), cert, key);
            updateCdn(certId, cdnTaskData.getCdnDomain());
        } catch (Exception e) {
            log.error("HuaWeiCdnClient Error: {}", e.getMessage(), e);
            return -1;
        }
        return 0;
    }


    public String uploadSsl(String alisaName, String privateKey, String publicKey) {
        ImportCertificateRequest request = new ImportCertificateRequest();
        ImportCertificateRequestBody requestBody = new ImportCertificateRequestBody();
        requestBody.setPrivateKey(privateKey);
        requestBody.setCertificate(publicKey);
        requestBody.setName(alisaName);
        request.setBody(requestBody);
        ImportCertificateResponse response = scmClient.importCertificate(request);
        return response.getCertificateId();
    }


    public void updateCdn(String certId, String cdnDomain) {
        DeployCertificateRequest request = new DeployCertificateRequest();
        request.withCertificateId(certId);
        DeployCertificateRequestBody body = new DeployCertificateRequestBody();
        List<DeployedResource> listbodyResources = new ArrayList<>();
        listbodyResources.add(
                new DeployedResource()
                        .withDomainName(cdnDomain)
        );
        body.withResources(listbodyResources);
        body.withServiceName("CDN");
        request.withBody(body);
        DeployCertificateResponse response = scmClient.deployCertificate(request);
        if (CollectionUtils.isNotEmpty(response.getFailureList())) {
            log.error("CDN证书部署失败: {}", GsonUtil.toJson(response.getFailureList()));
        }
    }
}
