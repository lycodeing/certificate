package cn.lycodeing.certificate.service;

import cn.lycodeing.certificate.enums.CertProviderEnum;
import cn.lycodeing.certificate.service.impl.LetsEncryptCertService;
import cn.lycodeing.certificate.service.impl.ZeroSslCertService;

public class CertFactoryUtils {
    public static ICertService getCertService(CertProviderEnum certProviderEnum) {
        return switch (certProviderEnum) {
            case LETS_ENCRYPT -> new LetsEncryptCertService();
            case ZERO_SSL -> new ZeroSslCertService();
        };
    }
}
