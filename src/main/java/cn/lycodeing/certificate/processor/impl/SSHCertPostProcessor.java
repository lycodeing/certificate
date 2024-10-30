package cn.lycodeing.certificate.processor.impl;

import cn.lycodeing.certificate.context.Context;
import cn.lycodeing.certificate.context.SSHContext;
import cn.lycodeing.certificate.processor.CertPostProcessor;
import cn.lycodeing.certificate.utils.GsonUtil;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Slf4j
public class SSHCertPostProcessor implements CertPostProcessor {

    private SSHCertPostProcessor() {}

    public static SSHCertPostProcessor create() {
        return new SSHCertPostProcessor();
    }


    @Override
    public void postProcess(Context context) {
        SSHContext sshContext = GsonUtil.fromJson(context.getPostProcessorData(), SSHContext.class);
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
                    log.info("Exit status code : {}", channel.getExitStatus());
                    break;
                }
            }
            log.info("Command output: {}", sb);
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (in != null) {
                in.close();
            }
        }
    }
}


