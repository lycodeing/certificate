package cn.lycodeing.certificate.enums;

import lombok.Getter;

@Getter
public enum CertProviderEnum {
    LETS_ENCRYPT("acme://letsencrypt.org"),
    ZERO_SSL("acme://zerossl.com");


    private final String caURI;

    CertProviderEnum(String caURI) {
        this.caURI = caURI;
    }



}
