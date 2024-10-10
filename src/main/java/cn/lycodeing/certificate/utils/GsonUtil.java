package cn.lycodeing.certificate.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class GsonUtil {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting() // 格式化输出
            .create();

    /**
     * 将 Java 对象转换为 JSON 字符串
     *
     * @param obj Java 对象
     * @return JSON 字符串
     */
    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    /**
     * 将 JSON 字符串转换为指定类型的 Java 对象
     *
     * @param json     JSON 字符串
     * @param classOfT 指定的类型
     * @param <T>      泛型类型
     * @return 转换后的 Java 对象
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return GSON.fromJson(json, classOfT);
    }

    /**
     * 将 JSON 字符串转换为指定类型的 Java 对象（使用 TypeToken）
     *
     * @param json JSON 字符串
     * @param type 指定的类型
     * @param <T>  泛型类型
     * @return 转换后的 Java 对象
     */
    public static <T> T fromJson(String json, Type type) {
        return GSON.fromJson(json, type);
    }

    /**
     * 将 JSON 字符串转换为 List
     *
     * @param json JSON 字符串
     * @param type Token
     * @param <T>  泛型类型
     * @return 转换后的 List
     */
    public static <T> List<T> fromJsonToList(String json, TypeToken<List<T>> type) {
        return GSON.fromJson(json, type.getType());
    }

    /**
     * 将 JSON 字符串转换为 Map
     *
     * @param json JSON 字符串
     * @param type Token
     * @param <K>  泛型键的类型
     * @param <V>  泛型值的类型
     * @return 转换后的 Map
     */
    public static <K, V> Map<K, V> fromJsonToMap(String json, TypeToken<Map<K, V>> type) {
        return GSON.fromJson(json, type.getType());
    }

    /**
     * 从 Reader 中读取 JSON 并转换为 Java 对象
     *
     * @param reader   Reader
     * @param classOfT 指定的对象类型
     * @param <T>      泛型类型
     * @return Java 对象
     * @throws IOException 如果发生 I/O 错误
     */
    public static <T> T fromJson(Reader reader, Class<T> classOfT) throws IOException {
        return GSON.fromJson(reader, classOfT);
    }

    /**
     * 将 Java 对象写入 Writer 中
     *
     * @param writer Writer
     * @param obj    Java 对象
     * @throws IOException 如果发生 I/O 错误
     */
    public static void toJson(Writer writer, Object obj) throws IOException {
        GSON.toJson(obj, writer);
    }
}
