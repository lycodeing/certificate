package cn.lycodeing.certificate.processor;

import cn.lycodeing.certificate.context.Context;

public interface CertPostProcessor {


    void postProcess(Context context);
}
