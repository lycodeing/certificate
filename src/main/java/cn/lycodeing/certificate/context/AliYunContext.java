package cn.lycodeing.certificate.context;

import lombok.Data;

@Data
public class AliYunContext {

    /**
     * 阿里云accessKey
     */
    private String accessKey;
    /**
     * 阿里云accessSecret
     */
    private String accessSecret;
    /**
     * 域名
     */
    private String domain;

    /**
     * 证书别名
     */
    private String alisaName;

    /**
     * CDN加速域名
     */
    private String cdnDomain;
}
