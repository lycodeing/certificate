package cn.lycodeing.certificate.processor;

import cn.lycodeing.certificate.context.Context;
import cn.lycodeing.certificate.context.FtpContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service
@Slf4j
public class FtpCertificatePostProcessor implements CertificatePostProcessor {
    /**
     * 证书后缀名crt
     */
    private static final String CRT_SUFFIX = ".crt";
    /**
     * 密钥后缀名key
     */
    private static final String KEY_SUFFIX = ".key";
    /**
     * 证书后缀名pem
     */
    private static final String PEM_SUFFIX = ".pem";


    @Override
    public void postProcess(Context context) {
        FtpContext ftpContext = (FtpContext) context;
        FTPClient ftpClient = new FTPClient();
        try {
            connectToFtpServer(ftpClient, ftpContext);
            ensureDirectoryExists(ftpClient, ftpContext);
            processFileUploads(ftpClient, ftpContext, context);
        } catch (Exception e) {
            log.error("Failed to upload certificate to FTP server, {}", e.getMessage(), e);
        } finally {
            disconnectAndLogoutFtpClient(ftpClient);
        }
    }

    private void connectToFtpServer(FTPClient ftpClient, FtpContext ftpContext) throws IOException {
        ftpClient.connect(ftpContext.getHost(), ftpContext.getPort());
        ftpClient.login(ftpContext.getUser(), ftpContext.getPassword());
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
    }

    private void ensureDirectoryExists(FTPClient ftpClient, FtpContext ftpContext) throws IOException {
        if (!ftpClient.changeWorkingDirectory(ftpContext.getTargetPath())) {
            ftpClient.makeDirectory(ftpContext.getTargetPath());
            ftpClient.changeWorkingDirectory(ftpContext.getTargetPath());
        }
    }

    private void processFileUploads(FTPClient ftpClient, FtpContext ftpContext, Context context) throws IOException {
        String certPath = context.getCertPath();
        String fileName = ftpContext.getFileName();

        uploadFile(ftpClient, certPath, context.getOutput().get("crtFileName") + CRT_SUFFIX, fileName + CRT_SUFFIX);
        uploadFile(ftpClient, certPath, context.getOutput().get("keyFileName") + KEY_SUFFIX, fileName + KEY_SUFFIX);
        uploadFile(ftpClient, certPath, context.getOutput().get("crtFileName") + PEM_SUFFIX, fileName + PEM_SUFFIX);
    }

    private void uploadFile(FTPClient ftpClient, String certPath, String localFileName, String remoteFileName) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(certPath + File.separator + localFileName)) {
            ftpClient.storeFile(remoteFileName, inputStream);
        } catch (FileNotFoundException e) {
            log.error("File not found: {}", localFileName, e);
            throw new RuntimeException("File not found while uploading certificate", e);
        }
    }

    private void disconnectAndLogoutFtpClient(FTPClient ftpClient) {
        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (Exception e) {
            log.error("Failed to disconnect or log out FTP client, {}", e.getMessage(), e);
        }
    }

}
