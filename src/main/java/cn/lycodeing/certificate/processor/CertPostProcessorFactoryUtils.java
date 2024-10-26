package cn.lycodeing.certificate.processor;

import cn.lycodeing.certificate.enums.PostProcessorTypeEnum;

public class CertPostProcessorFactoryUtils {
    public static CertPostProcessor getCertificatePostProcessor(PostProcessorTypeEnum postProcessorType) {
        return switch (postProcessorType) {
            case SFTP -> new SFtpCertPostProcessor();
            case SSH -> new SSHCertPostProcessor();
            case QI_NIU -> new QiNiuCertPostProcessor();
            case TENCENT -> new TencentCertPostProcessor();
            default -> null;
        };
    }
}
