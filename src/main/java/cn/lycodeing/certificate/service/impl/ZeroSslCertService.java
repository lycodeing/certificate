package cn.lycodeing.certificate.service.impl;

import cn.lycodeing.certificate.enums.CertProviderEnum;
import cn.lycodeing.certificate.service.AbstractCertService;
import cn.lycodeing.certificate.utils.GsonUtil;
import cn.lycodeing.certificate.utils.HttpClientUtil;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.AccountBuilder;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.exception.AcmeException;

import java.lang.reflect.Type;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ZeroSslCertService extends AbstractCertService {
    /**
     * 默认超时时间
     */
    private static final int TIMEOUT = 60;
    private static final String KID_NAME = "eab_kid";
    private static final String ENCODED_MACKEY_NAME = "eab_hmac_key";
    private static final String EAB_URL = "https://api.zerossl.com/acme/eab-credentials";
    private static final Type TYPE = new TypeToken<Map<String, String>>() {}.getType();

    /**
     * 在ACME服务器上查找或注册一个新帐户。
     */
    public Account findOrRegisterAccount(Session session, KeyPair accountKey, String email, String apiKey) throws AcmeException {
        Map<String, String> result;
        try {
            Map<String, String> params = new HashMap<>();
            params.put("access_key", apiKey);
            String responseStr = HttpClientUtil.sendPost(EAB_URL, params);
            log.info("eab info:{}", responseStr);
            result = GsonUtil.fromJson(responseStr, TYPE);
        } catch (Exception e) {
            log.error("获取eab信息失败", e);
            throw new RuntimeException("获取eab信息失败");
        }
        AccountBuilder builder = new AccountBuilder()
                .addEmail(email)
                .agreeToTermsOfService()
                .withKeyIdentifier(result.get(KID_NAME), result.get(ENCODED_MACKEY_NAME))
                .useKeyPair(accountKey);

        Account account = builder.create(session);
        log.info("Registered new user: {}", account.getLocation());
        return account;
    }

    @Override
    public boolean isCertType(CertProviderEnum certType) {
        setCertType(CertProviderEnum.ZERO_SSL);
        return CertProviderEnum.ZERO_SSL.equals(certType);
    }


    public ZeroSslCertService() {
        setCertType(CertProviderEnum.ZERO_SSL);
    }
}
