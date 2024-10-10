package cn.lycodeing.certificate;

public interface ICertService {


   void createCert(String domain, String subDomain, String cert_path, String email,String accessKey,String accessSecret);
}
