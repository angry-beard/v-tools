package com.v.core.base.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.util.Strings;

import java.io.IOException;
import java.util.Objects;

/**
 * Created by angry_beard on 2021/3/15.
 */
public class JsonUtils {

    public static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    /**
     * 如果有不识别的属性，不会报错，只会忽略。
     */
    public static final ObjectMapper IGNORE_JSON_MAPPER;

    static {
        IGNORE_JSON_MAPPER = new ObjectMapper();
        IGNORE_JSON_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * Constructor<br>
     */
    private JsonUtils() {
    }

    /**
     * 将对象转为 json <br>
     *
     * @param object {@link Object}
     * @return json 字符串
     */
    public static String toJson(Object object) {

        try {
            return JSON_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("转换Json出错", e);
        }
    }

    /**
     * 将字符串 json 转为对象<br>
     *
     * @param <T>     转换后的类型
     * @param content json 字符串
     * @param type    转换后的类型
     * @return 转换后的结果
     */
    public static <T> T fromJson(String content, Class<T> type) {

        return toObject(JSON_MAPPER, content, type);
    }

    /**
     * 将字符串 json 转为对象，将忽略未知的字段<br>
     *
     * @param <T>     转换后的类型
     * @param content json 字符串
     * @param type    转换后的类型
     * @return 转换后的结果
     */
    public static <T> T fromIgnoreJson(String content, Class<T> type) {

        return toObject(IGNORE_JSON_MAPPER, content, type);
    }

    /**
     * 将字符串 json 转为对象<br>
     *
     * @param <T>     转换后的类型
     * @param mapper  {@link ObjectMapper}
     * @param content json 字符串
     * @param type    转换后的类型
     * @return 转换后的结果
     */
    private static <T> T toObject(ObjectMapper mapper, String content, Class<T> type) {
        if (Objects.isNull(content)
                || content.trim().equals(Strings.EMPTY)) {
            return null;
        }
        try {
            return mapper.readValue(content, type);
        } catch (IOException e) {
            throw new RuntimeException("json转换出错", e);
        }
    }
}
