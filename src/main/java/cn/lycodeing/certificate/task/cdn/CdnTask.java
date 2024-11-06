package cn.lycodeing.certificate.task.cdn;

import cn.lycodeing.certificate.client.cdn.*;
import cn.lycodeing.certificate.context.CdnTaskData;
import cn.lycodeing.certificate.context.Context;
import cn.lycodeing.certificate.task.Task;
import cn.lycodeing.certificate.utils.FileUtil;
import cn.lycodeing.certificate.utils.GsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class CdnTask implements Task {


    @Override
    public int execute(Context context) {
        int exitCode = 0;
        try {
            String cert = getCert(context);
            String key = getKey(context);
            CdnTaskData processorData = GsonUtil.fromJson(context.getData(), CdnTaskData.class);
            exitCode = switch (processorData.getType()) {
                case ALIYUN -> AliYunCdnClient.getInstance().executeJob(processorData, cert, key);
                case HUAWEI -> HuaWeiCdnClient.getInstance().executeJob(processorData, cert, key);
                case QI_NIU -> QiNiuCdnClient.getInstance().executeJob(processorData, cert, key);
                case TENCENT -> TencentCdnClient.getInstance().executeJob(processorData, cert, key);
                case U_CLOUD -> UCloudCdnClient.getInstance().executeJob(processorData, cert, key);
            };
        } catch (Exception e) {
            log.error("Task execution failed", e);
            return exitCode;
        }
        return exitCode;
    }


    public String getCert(Context context) throws IOException {
        return FileUtil.readFileAsString(context.getOutput().get("crtFilePath"));
    }

    public String getKey(Context context) throws IOException {
        return FileUtil.readFileAsString(context.getOutput().get("keyFilePath"));
    }

    public String getPem(Context context) throws IOException {
        return FileUtil.readFileAsString(context.getOutput().get("pemFilePath"));
    }
}
