package cn.lycodeing.certificate.processor;

import cn.lycodeing.certificate.context.Context;
import cn.lycodeing.certificate.enums.PostProcessorTypeEnum;

public interface CertificatePostProcessor {


    void postProcess(Context context);



    boolean isPostProcessorType(PostProcessorTypeEnum postProcessorType);
}
