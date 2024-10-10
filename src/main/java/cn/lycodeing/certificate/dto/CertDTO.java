package cn.lycodeing.certificate.dto;

import cn.lycodeing.certificate.enums.CertProviderEnum;
import cn.lycodeing.certificate.enums.DnsEnum;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CertDTO {

    /**
     * 域名
     */
    private String domain;

    /**
     * 证书临时保存地址
     */
    private String certPath;

    /**
     * 证书供应商
     */
    private CertProviderEnum certProvider;

    /**
     * 证书供应商的API密钥或认证信息
     */
    private String apiKey;

    /**
     * 账号邮箱
     */
    private String email;

    /**
     * 申请证书的子域名地址
     */
    private List<String> domains;

    /**
     * dns解析的供应商
     */
    private DnsEnum dnsType;

    /**
     * 账号公钥
     */
    private String accessKey;


    /**
     * 账号密钥
     */
    private String accessSecret;
}
