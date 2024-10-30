package cn.lycodeing.certificate.processor.impl;

import cn.lycodeing.certificate.context.Context;
import cn.lycodeing.certificate.context.SFtpContext;
import cn.lycodeing.certificate.processor.CertPostProcessor;
import cn.lycodeing.certificate.utils.GsonUtil;
import com.jcraft.jsch.*;
import com.sun.istack.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static cn.lycodeing.certificate.constant.CommonConstant.*;

@Slf4j
public class SFtpCertPostProcessor implements CertPostProcessor {

    private static final SFtpCertPostProcessor INSTANCE = new SFtpCertPostProcessor();


    private SFtpCertPostProcessor() {
    }

    public static SFtpCertPostProcessor create() {
        return INSTANCE;
    }

    @Override
    public void postProcess(Context context) {
        try {
            SFtpContext sFtpContext = GsonUtil.fromJson(context.getPostProcessorData(), SFtpContext.class);
            Session session = getSession(sFtpContext);

            ChannelSftp sftpChannel = getChannelSftp(session);

            createDirectory(sftpChannel, sFtpContext.getTargetPath());

            uploadFile(sftpChannel, context.getCertPath() + context.getOutput().get("crtFileName") + CERT_SUFFIX, sFtpContext.getTargetPath() + sFtpContext.getFileName() + CERT_SUFFIX);
            uploadFile(sftpChannel, context.getCertPath() + context.getOutput().get("crtFileName") + PEM_SUFFIX, sFtpContext.getTargetPath() + sFtpContext.getFileName() + PEM_SUFFIX);
            uploadFile(sftpChannel, context.getCertPath() + context.getOutput().get("crtFileName") + KEY_SUFFIX, sFtpContext.getTargetPath() + sFtpContext.getFileName() + KEY_SUFFIX);

        } catch (Exception ex) {
            log.error("Failed to upload certificate to FTP server: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to upload certificate to FTP server");
        }
    }

    @NotNull
    private ChannelSftp getChannelSftp(Session session) throws JSchException {
        Channel channel = session.openChannel("sftp");
        channel.connect();
        return (ChannelSftp) channel;
    }

    @NotNull
    private Session getSession(SFtpContext SFtpContext) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(SFtpContext.getUser(), SFtpContext.getHost(), SFtpContext.getPort());
        session.setPassword(SFtpContext.getPassword());
        // 设置会话配置
        Properties config = new java.util.Properties();
        // 设置严格主机密钥检查为关闭
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        return session;
    }


    /**
     * 上传文件到指定路径
     *
     * @param sftpChannel 用于SFTP操作的Channel对象
     * @param localFile   本地文件路径
     * @param remotePath  远程目录路径
     * @throws SftpException 如果SFTP操作失败
     */
    public void uploadFile(ChannelSftp sftpChannel, String localFile, String remotePath)
            throws SftpException {
        try (InputStream inputStream = new FileInputStream(localFile)) {
            sftpChannel.put(inputStream, remotePath);
            log.info("File uploaded successfully to {}", remotePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void createDirectory(ChannelSftp sftpChannel, String fullRemotePath) throws SftpException {
        try {
            sftpChannel.lstat(fullRemotePath); // 检查整个路径是否已存在
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                // 整个路径不存在，需要逐级创建目录
                String[] dirs = fullRemotePath.split("/");
                StringBuilder currentPath = new StringBuilder("/");

                for (String dir : dirs) {
                    if (StringUtils.isNotBlank(dir)) {
                        currentPath.append(dir).append("/");
                        try {
                            sftpChannel.lstat(currentPath.toString());
                            // 当前目录已存在，跳过创建
                        } catch (SftpException e1) {
                            if (e1.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                                // 当前目录不存在，需要创建
                                sftpChannel.mkdir(currentPath.toString());
                                log.info("Directory created: {}", currentPath.toString());
                            } else {
                                throw e1; // 抛出其他异常
                            }
                        }
                    }
                }
            } else {
                throw e; // 处理其他异常
            }
        }
    }
}