package cn.lycodeing.certificate.enums;

import cn.lycodeing.certificate.service.impl.LetsEncryptCertService;
import lombok.Getter;

@Getter
public enum CertTypeEnum {
    LETS_ENCRYPT("acme://letsencrypt.org"),
    ZERO_SSL("acme://zerossl.com");


    private final String caURI;

    CertTypeEnum(String caURI) {
        this.caURI = caURI;
    }
}
