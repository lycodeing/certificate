package cn.lycodeing.certificate.processor;

import cn.lycodeing.certificate.enums.PostProcessorTypeEnum;

public class CertificatePostProcessorFactoryUtils {
    public static CertificatePostProcessor getCertificatePostProcessor(PostProcessorTypeEnum postProcessorType) {
        return switch (postProcessorType) {
            case SFTP -> new SFtpCertificatePostProcessor();
            case SSH -> new SSHCertificatePostProcessor();
            case QI_NIU -> new QiNiuCertificatePostProcessor();
            default -> null;
        };
    }
}
