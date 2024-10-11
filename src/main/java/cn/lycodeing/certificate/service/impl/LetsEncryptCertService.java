package cn.lycodeing.certificate.service.impl;

import cn.lycodeing.certificate.enums.CertProviderEnum;
import cn.lycodeing.certificate.service.AbstractCertService;
import lombok.extern.slf4j.Slf4j;
import org.shredzone.acme4j.*;
import org.shredzone.acme4j.exception.AcmeException;
import org.springframework.stereotype.Service;

import java.security.KeyPair;

@Slf4j
@Service
public class LetsEncryptCertService extends AbstractCertService {

    private static final int TIMEOUT = 60;
    private static final String CA_URI = "acme://letsencrypt.org";


    /**
     * 在ACME服务器上查找或注册一个新帐户。
     */
    public Account findOrRegisterAccount(Session session, KeyPair accountKey, String email, String apiKey) throws AcmeException {
        try {
            AccountBuilder builder = new AccountBuilder()
                    .addEmail(email)
                    .agreeToTermsOfService()
                    .useKeyPair(accountKey);

            Account account = builder.create(session);
            log.info("Registered new user: {}", account.getLocation());
            return account;
        } catch (Exception e) {
            log.error("Registered new user error: {}", e.getMessage());
            throw new RuntimeException("Registered new user error",e);
        }
    }

    @Override
    public boolean isCertType(CertProviderEnum certType) {
        return CertProviderEnum.LETS_ENCRYPT.equals(certType);
    }

    public LetsEncryptCertService() {
        setCertType(CertProviderEnum.LETS_ENCRYPT);
    }
}
