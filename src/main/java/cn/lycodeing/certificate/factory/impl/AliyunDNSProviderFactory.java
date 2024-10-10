package cn.lycodeing.certificate.handers;

import com.aliyun.alidns20150109.Client;
import com.aliyun.alidns20150109.models.AddDomainRecordRequest;
import com.aliyun.alidns20150109.models.DeleteSubDomainRecordsRequest;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;

public class AliyunDNSProviderFactory implements DNSProviderFactory {

    private final static String ENDPOINT = "alidns.cn-shenzhen.aliyuncs.com";


    private final Client client;

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
        client.addDomainRecordWithOptions(request, runtime);
    }

    @Override
    public void deleteSubDomainRecord(String domainName, String rr, String type) throws Exception {
        RuntimeOptions runtime = new RuntimeOptions();
        DeleteSubDomainRecordsRequest request = new DeleteSubDomainRecordsRequest()
                .setDomainName(domainName)
                .setRR(rr)
                .setType(type);
        client.deleteSubDomainRecordsWithOptions(request, runtime);
    }
}
