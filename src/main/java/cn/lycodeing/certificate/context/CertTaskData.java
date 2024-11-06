package cn.lycodeing.certificate.context;

import cn.lycodeing.certificate.enums.CertProviderEnum;
import lombok.Data;

import java.util.List;

@Data
public class CertTaskData {

    /**
     * 域名
     */
    private String domain;

    /**
     * 证书临时保存地址
     */
    private String certPath;

    /**
     * 账号邮箱
     */
    private String email;

    /**
     * 申请证书的子域名地址
     */
    private List<String> domains;

    /**
     * DNS解析信息
     */
    private DnsData dns;

    private CertProviderData certProvider;

    public boolean isValid() {
        return domain != null && domains != null && !domains.isEmpty();
    }


    /**
     * DNS解析信息
     */
    @Data
    public static class DnsData {
        private String accessKey;
        private String accessSecret;
        /**
         * 类型
         */
        private String dnsType;

        public DnsData(String accessKey, String accessSecret, String dnsType) {
            this.accessKey = accessKey;
            this.accessSecret = accessSecret;
            this.dnsType = dnsType;
        }
    }

    @Data
    public static class CertProviderData {
        /**
         * 证书供应商
         */
        private CertProviderEnum type;

        /**
         * 证书供应商的API密钥或认证信息
         */
        private String apiKey;

        public CertProviderData(CertProviderEnum type) {
            this.type = type;
        }

        public CertProviderData(CertProviderEnum type, String apiKey) {
            this.type = type;
            this.apiKey = apiKey;
        }
    }
}
