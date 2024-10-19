package cn.lycodeing.certificate.processor;

import cn.lycodeing.certificate.context.Context;
import cn.lycodeing.certificate.context.SSHContext;
import cn.lycodeing.certificate.enums.PostProcessorTypeEnum;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Service
@Slf4j
public class SSHCertificatePostProcessor implements CertificatePostProcessor {
    @Override
    public void postProcess(Context context) {
        SSHContext sshContext = (SSHContext) context;
        try {
            JSch jsch = new JSch();
            // 创建Session实例
            Session session = jsch.getSession(sshContext.getUser(), sshContext.getHost(), sshContext.getPort());

            // 设置登录凭证
            session.setPassword(sshContext.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");  // 接受任何公钥

            // 连接到远程主机
            session.connect();

            // 执行命令
            executeCommand(session, sshContext.getCommand());

            // 关闭会话
            session.disconnect();
        } catch (Exception e) {
            log.error("Error executing command: {}", e.getMessage(), e);
        }
    }

    public void executeCommand(Session session, String command) throws Exception {
        Channel channel = null;
        BufferedReader in = null;

        try {
            // 打开一个会话通道
            channel = session.openChannel("exec");

            // 将命令发送给远程服务器
            ((ChannelExec) channel).setCommand(command);

            // 获取输入流以便读取输出结果
            channel.setInputStream(null);

            ((ChannelExec) channel).setErrStream(System.err);

            // 接收输出
            InputStream inStream = channel.getInputStream();
            in = new BufferedReader(new InputStreamReader(inStream));

            // 连接通道，开始执行
            channel.connect();

            // 输出执行结果
            String result = "";
            StringBuilder sb = new StringBuilder();
            while (true) {
                while (in.ready()) {
                    sb.append(in.readLine()).append("\n");
                }
                if (channel.isClosed()) {
                    System.out.println("Exit status: " + channel.getExitStatus());
                    break;
                }
                Thread.sleep(1000);
            }
            result = sb.toString();
            System.out.println(result);
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (in != null) {
                in.close();
            }
        }
    }

    @Override
    public boolean isPostProcessorType(PostProcessorTypeEnum postProcessorType) {
        return PostProcessorTypeEnum.SSH.equals(postProcessorType);
    }
}


