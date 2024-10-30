package cn.lycodeing.certificate.processor;

import cn.lycodeing.certificate.enums.PostProcessorTypeEnum;
import cn.lycodeing.certificate.processor.impl.*;

public class CertPostProcessorFactoryUtils {
    public static CertPostProcessor getCertificatePostProcessor(PostProcessorTypeEnum postProcessorType) {
        return switch (postProcessorType) {
            case SFTP -> SFtpCertPostProcessor.create();
            case SSH -> SSHCertPostProcessor.create();
            case QI_NIU -> QiNiuCertPostProcessor.create();
            case TENCENT -> TencentCertPostProcessor.create();
            case ALIYUN -> AliYunCertPostProcessor.create();
            case HUAWEI -> HuaWeiCertPostProcessor.create();
            case U_CLOUD -> null;
        };
    }
}
