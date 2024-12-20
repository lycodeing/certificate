package cn.lycodeing.certificate.dns.impl;

import cn.lycodeing.certificate.dns.DNSProviderFactory;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.dnspod.v20210323.DnspodClient;
import com.tencentcloudapi.dnspod.v20210323.models.CreateRecordRequest;
import com.tencentcloudapi.dnspod.v20210323.models.CreateRecordResponse;
import com.tencentcloudapi.dnspod.v20210323.models.DeleteRecordRequest;

public class TencentDNSProviderFactory implements DNSProviderFactory {

    private final String DNS_ENDPOINT = "dnspod.tencentcloudapi.com";


    private final DnspodClient client;

    private Long recordId;

    private String domainName;

    public TencentDNSProviderFactory(String accessKeyId, String accessKeySecret) {
        Credential credential = new Credential(accessKeyId, accessKeySecret);
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint(DNS_ENDPOINT);
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        client = new DnspodClient(credential, "", clientProfile);
    }

    @Override
    public void addDomainRecord(String domainName, String rr, String type, String value, long ttl) throws Exception {
        CreateRecordRequest req = new CreateRecordRequest();
        req.setDomain(domainName);
        req.setRecordType(type);
        req.setValue(value);
        req.setSubDomain(rr);
        req.setRecordLine("默认");
        req.setTTL(ttl);
        CreateRecordResponse resp = client.CreateRecord(req);
        recordId = resp.getRecordId();
        this.domainName = domainName;
    }

    @Override
    public void deleteSubDomainRecord() throws Exception {
        DeleteRecordRequest req = new DeleteRecordRequest();
        req.setRecordId(recordId);
        req.setDomain(domainName);
        client.DeleteRecord(req);
    }
}
