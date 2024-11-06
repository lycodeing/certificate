package cn.lycodeing.certificate.dns.impl;

import cn.lycodeing.certificate.dns.DNSProviderFactory;
import com.huaweicloud.sdk.core.auth.BasicCredentials;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.dns.v2.DnsClient;
import com.huaweicloud.sdk.dns.v2.model.*;
import com.huaweicloud.sdk.dns.v2.region.DnsRegion;

import java.util.Collections;

public class HuaWeiDNSProviderFactory implements DNSProviderFactory {
    private final DnsClient client;

    private String zoneId;

    private String recordId;

    public HuaWeiDNSProviderFactory(String accessKeyId, String accessKeySecret) {
        ICredential auth = new BasicCredentials()
                .withAk(accessKeyId)
                .withSk(accessKeySecret);
        client = DnsClient.newBuilder()
                .withCredential(auth)
                .withRegion(DnsRegion.valueOf("cn-south-1"))
                .build();
    }

    @Override
    public void addDomainRecord(String domainName, String rr, String type, String value, long ttl) throws Exception {
        setZoneId(domainName);
        CreateRecordSetRequest request = new CreateRecordSetRequest();
        CreateRecordSetRequestBody body = new CreateRecordSetRequestBody();
        String val = "\"" + value + "\"";
        body.withRecords(Collections.singletonList(val));
        body.withTtl(Integer.parseInt(String.valueOf(ttl)));
        body.withType(type);
        body.withDescription("自动生成dns解析");
        body.withName(rr + "." + domainName + ".");
        request.withBody(body);
        request.setZoneId(this.zoneId);
        CreateRecordSetResponse response = client.createRecordSet(request);
        recordId = response.getId();
        // 避免dns解析未生效
        Thread.sleep(1000 * 10);

    }

    private void setZoneId(String domainName) {
        ListPublicZonesRequest request = new ListPublicZonesRequest();
        ListPublicZonesResponse response = client.listPublicZones(request);
        PublicZoneResp zoneResp = response.getZones().stream()
                .filter(zone -> zone.getName().contains(domainName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("zone not found"));
        this.zoneId = zoneResp.getId();
    }

    @Override
    public void deleteSubDomainRecord() throws Exception {
        DeleteRecordSetRequest request = new DeleteRecordSetRequest();
        request.setZoneId(this.zoneId);
        request.setRecordsetId(recordId);
        client.deleteRecordSet(request);
    }
}
