package cn.lycodeing.certificate.factory;

public interface DNSProviderFactory {

    /**
     * 添加域名解析记录
     *
     * @param domainName 域名
     * @param rr         主机记录
     * @param type       记录类型 A/CNAME/TXT
     * @param value      记录值
     * @param ttl        生效时间(秒)
     */
    void addDomainRecord(String domainName, String rr, String type, String value, long ttl) throws Exception;

    /**
     * 删除域名解析记录
     *
     * @param domainName 域名
     * @param rr         主机记录
     * @param type       记录类型 A/CNAME/TXT
     */
    void deleteSubDomainRecord(String domainName, String rr, String type) throws Exception;
}
