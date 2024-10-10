package cn.lycodeing.certificate.service;

import cn.lycodeing.certificate.dto.CertDTO;
import cn.lycodeing.certificate.enums.CertProviderEnum;

public interface ICertService {

    /**
     * 申请证书
     *
     * @param certDTO 申请证书参数
     */
    void createCert(CertDTO certDTO);


    /**
     * 证书供应商类型
     */
    boolean isCertType(CertProviderEnum certType);

}
