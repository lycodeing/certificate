package cn.lycodeing.certificate;

import cn.lycodeing.certificate.constant.SystemConstant;
import cn.lycodeing.certificate.context.Context;
import cn.lycodeing.certificate.enums.TaskTypeEnum;
import cn.lycodeing.certificate.task.Task;
import cn.lycodeing.certificate.task.TaskFactoryUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

@Slf4j
public class CertificateApplication {

    public static void main(String[] args) {
        Context context = buildContext();
        Task task = TaskFactoryUtils.getTask(context.getTaskType());
        int execute = task.execute(context);
        System.exit(execute);
    }


    public static Context buildContext() {
        Context context = new Context();
        context.setTaskId(System.getenv(SystemConstant.TASK_ID_KEY));
        context.setData(System.getenv(SystemConstant.TASK_TYPE_KEY));
        context.setTaskType(TaskTypeEnum.valueOf(System.getenv(SystemConstant.TASK_TYPE_KEY)));
        context.setOutput(new HashMap<>());
        return context;
    }
}
