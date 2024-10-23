import os
import subprocess
import time

# 定义 JAR 文件路径
jar_file_path = "/Users/lycodeing/IdeaProjects/certificate/target/certificate-20241023.jar"

# 读取系统变量 cert_id
cert_id = "certId"
if cert_id in os.environ:
    cert_id = os.environ['certId']
# 读取系统变量 logType 日志打印模式
logType = "INFO"
if "logType" in os.environ:
    logType = os.environ['logType']

# 需要执行的 Java 命令
java_command = f"java -DLOG_LEVEL={logType}  -jar {jar_file_path}"

print(f"Executing command: {java_command}")

log_file_path = "/Users/lycodeing/Downloads/logs/" + cert_id + ".log"

# 打开用于保存日志的文件
log_file = open(log_file_path, "a")  # 使用 'a' 模式进行追加写入

try:
    # 使用 Popen 来实现持续读取输出
    process = subprocess.Popen(java_command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)

    while True:
        # 读取输出，每次读取一行
        line = process.stdout.readline().decode('utf-8')

        # 如果没有更多的日志输出，则退出循环
        if not line:
            break

        # 将日志输出写入文件
        log_file.write(line)
        log_file.flush()

        # 可以设置一个短暂的延迟来降低 CPU 占用率，如果不需要实时性可注释掉或移除此行
        time.sleep(0.1)

finally:
    # 关闭文件
    log_file.close()
