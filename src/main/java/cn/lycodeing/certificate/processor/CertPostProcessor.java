package cn.lycodeing.certificate.processor;

import cn.lycodeing.certificate.context.Context;
import cn.lycodeing.certificate.enums.PostProcessorTypeEnum;

public interface CertPostProcessor {


    void postProcess(Context context);



    boolean isPostProcessorType(PostProcessorTypeEnum postProcessorType);
}
