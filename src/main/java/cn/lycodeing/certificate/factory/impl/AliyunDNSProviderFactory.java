package cn.lycodeing.certificate.factory.impl;

import cn.lycodeing.certificate.factory.DNSProviderFactory;
import com.aliyun.alidns20150109.Client;
import com.aliyun.alidns20150109.models.AddDomainRecordRequest;
import com.aliyun.alidns20150109.models.AddDomainRecordResponse;
import com.aliyun.alidns20150109.models.DeleteDomainRecordRequest;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AliyunDNSProviderFactory implements DNSProviderFactory {

    private final static String ENDPOINT = "alidns.cn-shenzhen.aliyuncs.com";

    private final Client client;

    private String recordId;


    public AliyunDNSProviderFactory(String accessKeyId, String accessKeySecret) throws Exception {
        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret)
                .setEndpoint(ENDPOINT);

        client = new Client(config);
    }


    @Override
    public void addDomainRecord(String domainName, String rr, String type, String value, long ttl) throws Exception {
        AddDomainRecordRequest request = new AddDomainRecordRequest()
                .setLang("en")
                .setDomainName(domainName)
                .setRR(rr)
                .setType(type)
                .setValue(value)
                .setTTL(ttl);
        RuntimeOptions runtime = new RuntimeOptions();
        AddDomainRecordResponse addDomainRecordResponse = client.addDomainRecordWithOptions(request, runtime);
        recordId = addDomainRecordResponse.body.recordId;
    }

    @Override
    public void deleteSubDomainRecord() throws Exception {
        DeleteDomainRecordRequest request = new DeleteDomainRecordRequest();
        request.setRecordId(recordId);
        request.setLang("en");
        client.deleteDomainRecord(request);
    }
}
