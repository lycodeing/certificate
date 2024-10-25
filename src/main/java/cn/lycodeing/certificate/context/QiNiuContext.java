package cn.lycodeing.certificate.context;

import lombok.Data;

@Data
public class QiNiuContext {
    /**
     * accessKey
     */
    private String accessKey;

    /**
     * accessSecret
     */
    private String accessSecret;

    /**
     * 域名
     */
    private String domain;


    /**
     * 需要更新的cdn域名
     */
    private String cndDomain;

    /**
     * 名称
     */
    private String commonName;
}
