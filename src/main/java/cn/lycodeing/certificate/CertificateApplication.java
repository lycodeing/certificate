package cn.lycodeing.certificate;

import cn.lycodeing.certificate.constant.SystemConstant;
import cn.lycodeing.certificate.context.Context;
import cn.lycodeing.certificate.context.SFtpContext;
import cn.lycodeing.certificate.enums.PostProcessorTypeEnum;
import cn.lycodeing.certificate.utils.GsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CertificateApplication {

    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException {
        Map<String, Object> envs = getEnvs();
        log.info("环境变量:{}", envs);
        new Context();
        Context context;
        String json = GsonUtil.toJson(envs);
        context = GsonUtil.fromJson(json, Context.class);
        SFtpContext sFtpContext = new SFtpContext();
        sFtpContext.setHost("43.156.185.238");
        sFtpContext.setPort(22);
        sFtpContext.setUser("root");
        sFtpContext.setPassword("liyan.99");
        sFtpContext.setFileName("lycodeing.cn");
        sFtpContext.setTargetPath("/root/nginx/ssl/");
        context.setPostProcessorData(GsonUtil.toJson(sFtpContext));
        CoreService.execute(context, List.of(PostProcessorTypeEnum.SFTP));
    }


    /**
     * 读取环境变量
     */
    public static Map<String, Object> getEnvs() {
        Map<String, Object> envs = new HashMap<>();
        envs.put(SystemConstant.DOMAIN_KEY, System.getenv(SystemConstant.DOMAIN_KEY));
        envs.put(SystemConstant.CERT_PATH_KEY, System.getenv(SystemConstant.CERT_PATH_KEY));
        envs.put(SystemConstant.EMAIL_KEY, System.getenv(SystemConstant.EMAIL_KEY));
        envs.put(SystemConstant.DNS_TYPE_KEY, System.getenv(SystemConstant.DNS_TYPE_KEY));
        envs.put(SystemConstant.ACCESS_KEY_KEY, System.getenv(SystemConstant.ACCESS_KEY_KEY));
        envs.put(SystemConstant.ACCESS_SECRET_KEY, System.getenv(SystemConstant.ACCESS_SECRET_KEY));
        envs.put(SystemConstant.CERT_PROVIDER_KEY, System.getenv(SystemConstant.CERT_PROVIDER_KEY));
        envs.put(SystemConstant.API_KEY_KEY, System.getenv(SystemConstant.API_KEY_KEY));
        envs.put(SystemConstant.DOMAINS_KEY, List.of(System.getenv(SystemConstant.DOMAINS_KEY).split(",")));
        envs.put(SystemConstant.POST_PROCESSOR_DATA_KET, System.getenv(SystemConstant.POST_PROCESSOR_DATA_KET));
        return envs;
    }

}
