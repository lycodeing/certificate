package cn.lycodeing.certificate.context;

import lombok.Data;

@Data
public class HuaWeiContext {

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
     * 备注名称
     */
    private String alisaName;

    /**
     * 需要更新的cdn域名
     */
    private String cdnDomain;
}
