package cn.lycodeing.certificate;

import cn.lycodeing.certificate.context.Context;
import cn.lycodeing.certificate.enums.CertProviderEnum;
import cn.lycodeing.certificate.enums.DnsEnum;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

@SpringBootApplication
public class CertificateApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(CertificateApplication.class, args);


        Context certDTO = getCertDTO();
    }

    @NotNull
    private static Context getCertDTO() {
        Context certDTO = new Context();
        certDTO.setDomain("lycodeing.cn");
        certDTO.setCertPath("/Users/lycodeing/Downloads/");
        certDTO.setEmail("195669754598@qq.com");
        certDTO.setDnsType(DnsEnum.TENCENT_DNS);
        certDTO.setAccessKey("AKID1wmU0KIHBuR2jQ030kLG1byAAxXu9cBh");
        certDTO.setAccessSecret("SistiKmFXtDPct7H3fCj6LOG1d3GH7pN");
        certDTO.setCertProvider(CertProviderEnum.ZERO_SSL);
        certDTO.setApiKey("2d3316efcfafc578b793069ecad9d15d");
        certDTO.setDomains(List.of("*.lycodeing.cn"));
        return certDTO;
    }


}
