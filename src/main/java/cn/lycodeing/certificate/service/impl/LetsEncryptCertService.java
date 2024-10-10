package cn.lycodeing.certificate.service.impl;

import cn.lycodeing.certificate.enums.DnsEnum;
import cn.lycodeing.certificate.factory.DNSProviderFactory;
import cn.lycodeing.certificate.factory.DNSProviderFactoryUtils;
import cn.lycodeing.certificate.service.ICertService;
import lombok.extern.slf4j.Slf4j;
import org.shredzone.acme4j.*;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

@Slf4j
@Service
public class CertService implements ICertService {

    private static final int TIMEOUT = 60;
    private static final String CA_URI = "https://acme-v02.api.letsencrypt.org/directory";

    private static final DNSProviderFactory dnsProviderFactory;

    static {
        try {
            dnsProviderFactory = DNSProviderFactoryUtils.createDnsProviderFactory(
                    DnsEnum.ALI_DNS,
                    "LTAI5tDYGSQ6saKD4SJpyfj8",
                    "WzjR9PDfXlhmmilJevzzb4PaDY5gKQ"
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createCert(String domain, String certPath, String email, String... domains) {
        try {
            Certificate certificate = obtainCertificate(domain, certPath, email, domains);
            log.info("Success! The certificate for domains {} has been generated!", domains);
            log.info("Certificate URL: {}", certificate.getLocation());
        } catch (Exception ex) {
            log.error("Failed to generate certificate for domains {},{}", domains, ex.getMessage(), ex);
        } finally {
            log.info("Done");
        }
    }

    private Certificate obtainCertificate(String domain, String certPath, String email, String[] domains) throws Exception {
        KeyPair rsaKeyPair = createKeyPair();

        Session session = new Session(CA_URI);
        Account acct = findOrRegisterAccount(session, rsaKeyPair, email);

        KeyPair domainKeyPair = createKeyPair();
        Order order = acct.newOrder().domains(domains).create();

        authorizeDomains(order, domain);

        order.execute(domainKeyPair);
        order.waitForCompletion(Duration.ofSeconds(TIMEOUT));

        validateOrder(order);

        Certificate certificate = order.getCertificate();

        writeCertificates(certificate, domainKeyPair, certPath);
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
        order.waitUntilReady(Duration.ofSeconds(TIMEOUT));
    }


    private void authorize(Authorization auth, String domain) throws Exception {
        String subDomainRR = Dns01Challenge.RECORD_NAME_PREFIX + "." + auth.getIdentifier().getDomain().split("\\.")[0];

        Challenge challenge = checkAndTriggerChallenge(auth, domain, subDomainRR);
        if (challenge == null || challenge.getStatus() != Status.VALID) {
            log.error("Challenge for domain {} failed, reason: {}", auth.getIdentifier().getDomain(), challenge != null ? challenge.getError() : "unknown");
            throw new AcmeException("Challenge failed... Giving up.");
        }

        log.info("Challenge for domain {} has been completed", auth.getIdentifier().getDomain());
        dnsProviderFactory.deleteSubDomainRecord(domain, subDomainRR, "TXT");
    }

    private Challenge checkAndTriggerChallenge(Authorization auth, String domain, String subDomainRR) throws Exception {
        if (auth.getStatus() != Status.PENDING) return null;

        Dns01Challenge challenge = auth.findChallenge(Dns01Challenge.TYPE).map(Dns01Challenge.class::cast).orElse(null);
        if (challenge == null) {
            throw new AcmeException("No DNS challenge found for the domain " + auth.getIdentifier().getDomain());
        }

        dnsProviderFactory.addDomainRecord(domain, subDomainRR, "TXT", challenge.getDigest(), 600L);
        challenge.trigger();

        challenge.waitForCompletion(Duration.ofSeconds(TIMEOUT));
        return challenge;
    }

    /**
     * 写入证书和密钥
     */
    private void writeCertificates(Certificate certificate, KeyPair domainKeyPair, String certPath) throws Exception {
        File certFile = new File(certPath, "domain.cert");
        FileWriter fw = new FileWriter(certFile);
        try {
            certificate.writeCertificate(fw);
        } finally {
            fw.close();
        }
        log.info("Wrote certificate to {}", certFile);

        File keyFile = new File(certPath, "domain.key");
        fw = new FileWriter(keyFile);
        try {
            KeyPairUtils.writeKeyPair(domainKeyPair, fw);
        } finally {
            fw.close();
        }
        log.info("Wrote key to {}", keyFile);

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
    private Account findOrRegisterAccount(Session session, KeyPair accountKey, String email) throws AcmeException {
        AccountBuilder builder = new AccountBuilder()
                .addEmail(email)
                .agreeToTermsOfService()
                .useKeyPair(accountKey);

        Account account = builder.create(session);
        log.info("Registered new user: {}", account.getLocation());

        return account;
    }
}
