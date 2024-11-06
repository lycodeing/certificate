package cn.lycodeing.certificate.context;

import cn.lycodeing.certificate.enums.TaskTypeEnum;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Context {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 环境信息
     */
    private Map<String, String> envs;

    /**
     * 任务类型
     */
    private TaskTypeEnum taskType;

    /**
     * 证书信息
     * CDN处理器数据
     * SSH
     * SFTP
     */
    private String data;

    /**
     * 输出数据
     * 例如 域名的证书信息
     * 保存的路径地址
     * 文件名称
     */
    private Map<String, String> output = new HashMap<>();

}
