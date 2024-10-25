package cn.lycodeing.certificate;

import cn.lycodeing.certificate.context.Context;
import cn.lycodeing.certificate.context.QiNiuContext;
import cn.lycodeing.certificate.enums.PostProcessorTypeEnum;
import cn.lycodeing.certificate.utils.GsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class CertificateApplication {

    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException {
        CoreService.execute(buildContext(getEnvs()), List.of(PostProcessorTypeEnum.QI_NIU));
    }


    /**
     * 读取环境变量
     */
//    public static Map<String, Object> getEnvs() {
//        Map<String, Object> envs = new HashMap<>();
//        envs.put(SystemConstant.DOMAIN_KEY, System.getenv(SystemConstant.DOMAIN_KEY));
//        envs.put(SystemConstant.CERT_PATH_KEY, System.getenv(SystemConstant.CERT_PATH_KEY));
//        envs.put(SystemConstant.EMAIL_KEY, System.getenv(SystemConstant.EMAIL_KEY));
//        envs.put(SystemConstant.DNS_TYPE_KEY, System.getenv(SystemConstant.DNS_TYPE_KEY));
//        envs.put(SystemConstant.ACCESS_KEY_KEY, System.getenv(SystemConstant.ACCESS_KEY_KEY));
//        envs.put(SystemConstant.ACCESS_SECRET_KEY, System.getenv(SystemConstant.ACCESS_SECRET_KEY));
//        envs.put(SystemConstant.CERT_PROVIDER_KEY, System.getenv(SystemConstant.CERT_PROVIDER_KEY));
//        envs.put(SystemConstant.API_KEY_KEY, System.getenv(SystemConstant.API_KEY_KEY));
//        envs.put(SystemConstant.DOMAINS_KEY, List.of(System.getenv(SystemConstant.DOMAINS_KEY).split(",")));
//        envs.put(SystemConstant.POST_PROCESSOR_DATA_KET, System.getenv(SystemConstant.POST_PROCESSOR_DATA_KET));
//        return envs;
//    }
    private static Map<String, Object> getEnvs() {
        QiNiuContext qiNiuContext = new QiNiuContext();
        qiNiuContext.setAccessKey("KqeXCE61CSkwpMjYbVQE2zlXBXwr9pefZ1mo_w6W");
        qiNiuContext.setAccessSecret("uPX28OIPTeQa8TdS-q8QtnWPULijXba5YXOnlitJ");
        qiNiuContext.setDomain("lycodeing");
        qiNiuContext.setCommonName("通用证书");
        qiNiuContext.setCndDomain("oss.lycodeing.cn");
        Map<String, Object> envs = new HashMap<>();
        envs.put("certId", UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        envs.put("domain", "lycodeing.cn");
        envs.put("logType", "DEBUG");
        envs.put("certPath", "/Users/lycodeing/Downloads/");
        envs.put("email", "195669754598@qq.com");
        envs.put("dnsType", "TENCENT_DNS");  // 选择使用枚举值的原始名称
        envs.put("accessKey", "AKIDhHeomY9kHclhQwFSSN7elfMpBQVN6dxA");
        envs.put("accessSecret", "6ze6Lr9GIH8obREzhHk7RcaUSHwRr8EB");
        envs.put("certProvider", "ZERO_SSL");  // 选择使用枚举值的原始名称
        envs.put("apiKey", "2d3316efcfafc578b793069ecad9d15d");
        envs.put("domains", "*.lycodeing.cn");
        envs.put("postProcessorData", GsonUtil.toJson(qiNiuContext));
        return envs;
    }

    public static Context buildContext(Map<String, Object> envs) {
        Context context = new Context();
        context.setDomain((String) envs.get("domain"));
        context.setCertPath((String) envs.get("certPath"));
        context.setEmail((String) envs.get("email"));
        context.setDnsType((String) envs.get("dnsType"));
        context.setAccessKey((String) envs.get("accessKey"));
        context.setAccessSecret((String) envs.get("accessSecret"));
        context.setCertProvider((String) envs.get("certProvider"));
        context.setApiKey((String) envs.get("apiKey"));
        context.setDomains(List.of(((String) envs.get("domains")).split(",")));
        context.setPostProcessorData((String) envs.get("postProcessorData"));
        return context;
    }
}
