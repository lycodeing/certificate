package cn.lycodeing.certificate.service;

import cn.lycodeing.certificate.context.Context;
import cn.lycodeing.certificate.enums.CertProviderEnum;

public interface ICertService {

    /**
     * 申请证书
     *
     * @param context 申请证书参数
     */
    void createCert(Context context);


    /**
     * 证书供应商类型
     */
    boolean isCertType(CertProviderEnum certType);

}
