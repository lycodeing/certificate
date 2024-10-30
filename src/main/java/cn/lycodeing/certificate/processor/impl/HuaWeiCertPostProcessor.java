package cn.lycodeing.certificate.processor.impl;

import cn.lycodeing.certificate.context.Context;
import cn.lycodeing.certificate.context.HuaWeiContext;
import cn.lycodeing.certificate.processor.CertPostProcessor;
import cn.lycodeing.certificate.utils.FileUtil;
import cn.lycodeing.certificate.utils.GsonUtil;
import com.huaweicloud.sdk.core.auth.GlobalCredentials;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.scm.v3.ScmClient;
import com.huaweicloud.sdk.scm.v3.model.*;
import com.huaweicloud.sdk.scm.v3.region.ScmRegion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static cn.lycodeing.certificate.constant.CommonConstant.CERT_SUFFIX;
import static cn.lycodeing.certificate.constant.CommonConstant.KEY_SUFFIX;

@Slf4j
public class HuaWeiCertPostProcessor implements CertPostProcessor {


    private ScmClient scmClient;

    private HuaWeiCertPostProcessor(){}


    public static HuaWeiCertPostProcessor create() {
        return new HuaWeiCertPostProcessor();
    }

    @Override
    public void postProcess(Context context) {
        try {
            HuaWeiContext huaWeiContext = GsonUtil.fromJson(context.getPostProcessorData(), HuaWeiContext.class);
            ICredential credentials = new GlobalCredentials()
                    .withAk(huaWeiContext.getAccessKey())
                    .withSk(huaWeiContext.getAccessSecret());
            scmClient = ScmClient.newBuilder()
                    .withCredential(credentials)
                    .withRegion(ScmRegion.valueOf("cn-north-4"))
                    .build();
            String certId = uploadSsl(huaWeiContext.getAlisaName(), getPrivateKey(context), getCert(context));
            updateCdn(certId, huaWeiContext.getCdnDomain());
        } catch (Exception e) {
            log.error("HuaWeiCertPostProcessor Error: {}", e.getMessage());
        }
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

    private String getCert(Context context) throws IOException {
        return FileUtil.readFileAsString(context.getCertPath() + context.getOutput().get("crtFileName") + CERT_SUFFIX);
    }

    private String getPrivateKey(Context context) throws IOException {
        return FileUtil.readFileAsString(context.getCertPath() + context.getOutput().get("keyFileName") + KEY_SUFFIX);
    }
}
