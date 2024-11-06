package cn.lycodeing.certificate.dns.impl;

import cn.lycodeing.certificate.dns.DNSProviderFactory;
import cn.lycodeing.certificate.utils.GsonUtil;
import cn.lycodeing.certificate.utils.HttpClientUtil;
import lombok.Data;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Map;

public class WestDNSProviderFactory implements DNSProviderFactory {

    private static final String ENDPOINT = " https://api.west.cn/API/v2/domain/dns";
    private String token;

    private String domainName;
    private Long recordId;

    public WestDNSProviderFactory(String accessKeyId, String accessKeySecret) throws Exception {
        this.token = DigestUtils.md5Hex(accessKeyId + accessKeySecret + System.currentTimeMillis());

    }

    @Override
    public void addDomainRecord(String domainName, String rr, String type, String value, long ttl) throws Exception {
        this.domainName = domainName;
        Map<String, String> data = Map.of(
                "act", "adddnsrecord",
                "domain", domainName,
                "host", rr,
                "type", type,
                "value", value,
                "ttl", ttl + "",
                "level", "10",
                "line", "",
                "token", token
        );
        String responseStr = HttpClientUtil.sendPost(ENDPOINT, data, null);
        WestResponse westResponse = GsonUtil.fromJson(responseStr, WestResponse.class);
        if (westResponse.getResult() != 200) {
            throw new RuntimeException("添加域名解析失败");
        }
        recordId = westResponse.getData().getId();
    }

    @Override
    public void deleteSubDomainRecord() throws Exception {
        Map<String, String> data = Map.of(
                "act", "deldnsrecord",
                "domain", this.domainName,
                "id", this.recordId + "",
                "token", this.token
        );
        String responseStr = HttpClientUtil.sendPost(ENDPOINT, data, null);
        WestResponse westResponse = GsonUtil.fromJson(responseStr, WestResponse.class);
        if (westResponse.getResult() != 200) {
            throw new RuntimeException("删除域名解析失败");
        }

    }

    @Data
    static class WestResponse {
        private Integer result;

        private String clientid;

        private WestDataResponse data;
    }

    @Data
    static class WestDataResponse {
        /**
         * 解析ID
         */
        private Long id;
    }
}

