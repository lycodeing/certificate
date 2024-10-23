package cn.lycodeing.certificate;

import cn.lycodeing.certificate.context.Context;
import cn.lycodeing.certificate.enums.CertProviderEnum;
import cn.lycodeing.certificate.enums.PostProcessorTypeEnum;
import cn.lycodeing.certificate.processor.CertificatePostProcessorFactoryUtils;
import cn.lycodeing.certificate.service.CertFactoryUtils;
import cn.lycodeing.certificate.service.ICertService;

import java.util.List;
import java.util.Objects;

public class CoreService {

    /**
     * 执行证书生成
     *
     * @param context                上下文信息
     * @param postProcessorTypeEnums 执行后处理类型
     */
    public static void execute(Context context, List<PostProcessorTypeEnum> postProcessorTypeEnums) {
        ICertService certService = CertFactoryUtils.getCertService(CertProviderEnum.valueOf(context.getCertProvider()));
        certService.createCert(context);
        postProcessorTypeEnums.stream()
                .map(CertificatePostProcessorFactoryUtils::getCertificatePostProcessor)
                .filter(Objects::nonNull)
                .forEach(processor -> processor.postProcess(context));
    }

}
