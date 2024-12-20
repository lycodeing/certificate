package cn.lycodeing.certificate.context;

import lombok.Data;


@Data
public class SFtpTaskData {
    /**
     * FTP服务器地址
     */
    private String host;
    /**
     * FTP服务器端口
     */
    private Integer port;
    /**
     * FTP用户名
     */
    private String user;
    /**
     * FTP密码
     */
    private String password;
    /**
     * FTP文件路径
     */
    private String targetPath;
    /**
     * 文件名
     */
    private String fileName;
}
