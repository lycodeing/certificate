package cn.lycodeing.certificate.factory.impl;

import cn.lycodeing.certificate.factory.DNSProviderFactory;

public class TencentDNSProviderFactory implements DNSProviderFactory {

    public TencentDNSProviderFactory(String accessKeyId, String accessKeySecret){

    }

    @Override
    public void addDomainRecord(String domainName, String rr, String type, String value, long ttl) throws Exception {

    }

    @Override
    public void deleteSubDomainRecord(String domainName, String rr, String type) throws Exception {

    }
}
