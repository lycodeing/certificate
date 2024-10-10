package cn.lycodeing.certificate;

import cn.lycodeing.certificate.dto.CertDTO;
import cn.lycodeing.certificate.enums.CertProviderEnum;
import cn.lycodeing.certificate.enums.DnsEnum;
import cn.lycodeing.certificate.service.ICertService;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class CertificateApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(CertificateApplication.class, args);


        CertDTO certDTO = getCertDTO();
        for (ICertService certService : applicationContext.getBeansOfType(ICertService.class).values()) {
            if (certService.isCertType(certDTO.getCertProvider())) {
                certService.createCert(certDTO);
            }
        }
    }

    @NotNull
    private static CertDTO getCertDTO() {
        CertDTO certDTO = new CertDTO();
        certDTO.setDomain("lycodeing.cn");
        certDTO.setCertPath("/Users/lycodeing/IdeaProjects/certificate/src/main/resources/ssl");
        certDTO.setEmail("test@qq.com");
        certDTO.setDnsType(DnsEnum.ALI_DNS);
        certDTO.setAccessKey("");
        certDTO.setAccessSecret("");
        certDTO.setCertProvider(CertProviderEnum.ZERO_SSL);
        certDTO.setApiKey("2d3316efcfafc578b793069ecad9d15d");
        certDTO.setDomains(List.of("mm.lycodeing.cn","nn.lycodeing.cn"));
        return certDTO;
    }


}
