package cn.lycodeing.certificate.client.ssl;

import lombok.extern.slf4j.Slf4j;
import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.AccountBuilder;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.exception.AcmeException;

import java.security.KeyPair;

@Slf4j
public class LetsEncryptCertClient {

    /**
     * 在ACME服务器上查找或注册一个新帐户。
     */
    public static Account findOrRegisterAccount(Session session, KeyPair accountKey, String email, String apiKey) throws AcmeException {
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
            throw new RuntimeException("Registered new user error", e);
        }
    }
}
