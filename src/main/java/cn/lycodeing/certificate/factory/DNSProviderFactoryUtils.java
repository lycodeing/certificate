package cn.lycodeing.certificate.factory;

import cn.lycodeing.certificate.enums.DnsEnum;
import cn.lycodeing.certificate.factory.impl.AliyunDNSProviderFactory;
import cn.lycodeing.certificate.factory.impl.TencentDNSProviderFactory;
import cn.lycodeing.certificate.factory.impl.WestDNSProviderFactory;

public class DNSProviderFactoryUtils {

    public static DNSProviderFactory createDnsProviderFactory(DnsEnum dnsEnum, String accessKey, String accessSecret) throws Exception {
        return switch (dnsEnum) {
            case ALI_DNS -> new AliyunDNSProviderFactory(accessKey, accessSecret);
            case TENCENT_DNS -> new TencentDNSProviderFactory(accessKey, accessSecret);
            case WEST_DNS -> new WestDNSProviderFactory(accessKey, accessSecret);
            default -> throw new Exception("暂不支持该DNS服务商");
        };
    }
}
