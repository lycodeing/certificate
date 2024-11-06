package cn.lycodeing.certificate.task;

import cn.lycodeing.certificate.enums.TaskTypeEnum;
import cn.lycodeing.certificate.task.cdn.CdnTask;
import cn.lycodeing.certificate.task.sftp.SFtpTask;
import cn.lycodeing.certificate.task.ssh.SSHTask;
import cn.lycodeing.certificate.task.ssl.CreateCertTask;

public class TaskFactoryUtils {
    public static Task getTask(TaskTypeEnum taskTypeEnum) {
        return switch (taskTypeEnum) {
            case SSL -> new CreateCertTask();
            case CDN -> new CdnTask();
            case SSH -> new SSHTask();
            case SFTP -> new SFtpTask();
        };
    }
}
