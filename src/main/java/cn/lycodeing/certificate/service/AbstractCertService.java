package cn.lycodeing.certificate.service;

import cn.lycodeing.certificate.context.Context;
import cn.lycodeing.certificate.enums.CertProviderEnum;
import cn.lycodeing.certificate.enums.DnsEnum;
import cn.lycodeing.certificate.factory.DNSProviderFactory;
import cn.lycodeing.certificate.factory.DNSProviderFactoryUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.shredzone.acme4j.*;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.util.KeyPairUtils;

import java.io.File;
import java.io.FileWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.List;

@Data
@Slf4j
public abstract class AbstractCertService implements ICertService {

    private static final Duration TIMEOUT = Duration.ofSeconds(180);

    private static final String TYPE = "TXT";

    private DNSProviderFactory dnsProviderFactory;

    private CertProviderEnum certType;

    public void setDNSProviderFactory(DnsEnum dns, String accessKey, String accessSecret) {
        try {
            dnsProviderFactory = DNSProviderFactoryUtils.createDnsProviderFactory(
                    dns,
                    accessKey,
                    accessSecret
            );
        } catch (Exception e) {
            log.error("Failed to query the Dns resolution provider, {}", e.getMessage(), e);
            throw new RuntimeException("Failed to query the Dns resolution provider");
        }
    }


    @Override
    public void createCert(Context context) {
        long startTime = System.currentTimeMillis();
        log.info("The current certificate vendor is:{}", context.getCertProvider());
        setDNSProviderFactory(context.getDnsType(), context.getAccessKey(), context.getAccessSecret());
        // 查询当前申请证书的记录
        try {
            Certificate certificate = obtainCertificate(certType.getCaURI(), context.getDomain(), context.getCertPath(), context.getEmail(), context.getDomains(), context.getApiKey());
            log.info("Success! The certificate for domains {} has been generated!", context.getDomains());
            log.info("Certificate URL: {}", certificate.getLocation());
        } catch (Exception ex) {
            log.error("Failed to generate certificate for domains {},{}", context.getDomain(), ex.getMessage(), ex);
        }
        log.info("Total time: {} ms", System.currentTimeMillis() - startTime);
        log.info("Certificate generation completed");
    }


    private Certificate obtainCertificate(String caUrl, String domain, String certPath, String email, List<String> domains, String apiKey) throws Exception {
        KeyPair rsaKeyPair = createKeyPair();

        Session session = new Session(caUrl);
        Account acct = findOrRegisterAccount(session, rsaKeyPair, email, apiKey);

        KeyPair domainKeyPair = createKeyPair();
        Order order = acct.newOrder().domains(domains).create();

        authorizeDomains(order, domain);

        order.execute(domainKeyPair);

        order.waitForCompletion(TIMEOUT);

        validateOrder(order);

        Certificate certificate = order.getCertificate();

        writeCertificates(certificate, domainKeyPair, certPath, domain);
        return certificate;
    }

    private void validateOrder(Order order) throws AcmeException {
        if (!order.getStatus().equals(Status.VALID)) {
            log.error("Order has failed, reason: {}", order.getError());
            throw new AcmeException("Order failed... Giving up.");
        }
    }

    private void authorizeDomains(Order order, String domain) throws Exception {
        for (Authorization auth : order.getAuthorizations()) {
            authorize(auth, domain);
        }
        order.waitUntilReady(TIMEOUT);
    }


    private void authorize(Authorization auth, String domain) throws Exception {

        String subDomainRR = getSubDomainRR(Dns01Challenge.RECORD_NAME_PREFIX, domain, auth.getIdentifier().getDomain());
        log.info("Checking authorization for subDomainRR {}", subDomainRR);
        try {
            Challenge challenge = checkAndTriggerChallenge(auth, domain, subDomainRR);
            if (challenge == null || challenge.getStatus() != Status.VALID) {
                String errorMsg = challenge != null ? challenge.getError()
                        .map(Problem::getDetail)
                        .map(String::valueOf)
                        .orElse("unknown") : "unknown";
                log.error("Challenge for domain {} failed, reason: {}", auth.getIdentifier().getDomain(), errorMsg);
                throw new AcmeException(errorMsg);
            }
        } catch (Exception e) {
            log.error("Challenge for domain {} failed, reason: {}", auth.getIdentifier().getDomain(), e.getMessage(), e);
            throw new AcmeException("Challenge failed... Giving up.");
        } finally {
            dnsProviderFactory.deleteSubDomainRecord();
            log.info("DeleteSubDomainRecord type:{} ,domainName:{} , rr:{} SUCCESS", TYPE, domain, subDomainRR);
        }
        log.info("Challenge for domain {} has been completed", auth.getIdentifier().getDomain());
    }

    private Challenge checkAndTriggerChallenge(Authorization auth, String domain, String subDomainRR) throws Exception {
        if (auth.getStatus() != Status.PENDING) return null;

        Dns01Challenge challenge = auth.findChallenge(Dns01Challenge.TYPE).map(Dns01Challenge.class::cast).orElse(null);
        if (challenge == null) {
            throw new AcmeException("No DNS challenge found for the domain " + auth.getIdentifier().getDomain());
        }

        dnsProviderFactory.addDomainRecord(domain, subDomainRR, TYPE, challenge.getDigest(), 600L);
        log.info("AddDomainRecord type:{}, rr: {} , value:{} SUCCESS", TYPE, subDomainRR, challenge.getDigest());
        challenge.trigger();

        challenge.waitForCompletion(TIMEOUT);
        return challenge;
    }

    /**
     * 写入证书和密钥
     */
    private void writeCertificates(Certificate certificate, KeyPair domainKeyPair, String certPath, String domain) throws Exception {
        long currentTimeMillis = System.currentTimeMillis();
        File certFile = new File(certPath, domain + "." + currentTimeMillis + ".cert");
        FileWriter fw = new FileWriter(certFile);
        try {
            certificate.writeCertificate(fw);
        } finally {
            fw.close();
        }
        log.info("Wrote certificate to {}", certFile);

        File keyFile = new File(certPath, domain + "." + currentTimeMillis + ".key");
        fw = new FileWriter(keyFile);
        try {
            KeyPairUtils.writeKeyPair(domainKeyPair, fw);
        } finally {
            fw.close();
        }
        log.info("Wrote key to {}", keyFile);

    }


    /**
     * 匹配子域名.前面的部分
     */

    private String getSubDomainRR(String prefix, String domain, String subDomain) {
        if (domain.equals(subDomain)) {
            return prefix;
        }
        String subStr = subDomain.split(domain)[0].split("\\.")[0];
        return prefix + "." + subStr;
    }


    /**
     * 生成RSA密钥对
     */
    private KeyPair createKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }


    /**
     * 在ACME服务器上查找或注册一个新帐户。
     */
    protected abstract Account findOrRegisterAccount(Session session, KeyPair accountKey, String email, String apiKey) throws AcmeException;
}
