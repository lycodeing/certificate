package cn.lycodeing.certificate.processor;

import cn.lycodeing.certificate.context.Context;

public interface CertificatePostProcessor {


    void postProcess(Context context);
}
