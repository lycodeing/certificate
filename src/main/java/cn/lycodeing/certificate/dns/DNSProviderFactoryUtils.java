package cn.lycodeing.certificate.dns;

import cn.lycodeing.certificate.dns.impl.AliyunDNSProviderFactory;
import cn.lycodeing.certificate.dns.impl.HuaWeiDNSProviderFactory;
import cn.lycodeing.certificate.dns.impl.TencentDNSProviderFactory;
import cn.lycodeing.certificate.dns.impl.WestDNSProviderFactory;
import cn.lycodeing.certificate.enums.DnsEnum;

public class DNSProviderFactoryUtils {

    public static DNSProviderFactory createDnsProviderFactory(DnsEnum dnsEnum, String accessKey, String accessSecret) throws Exception {
        return switch (dnsEnum) {
            case ALI_DNS -> new AliyunDNSProviderFactory(accessKey, accessSecret);
            case TENCENT_DNS -> new TencentDNSProviderFactory(accessKey, accessSecret);
            case WEST_DNS -> new WestDNSProviderFactory(accessKey, accessSecret);
            case HUAWEI_DNS -> new HuaWeiDNSProviderFactory(accessKey, accessSecret);
            default -> throw new Exception("暂不支持该DNS服务商");
        };
    }
}
