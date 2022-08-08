package com.v.core.base.utils;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by angry_beard on 2021/5/12.
 */
@Slf4j
public class BeanUtils {

    public static <T> T mapToObj(Map<String, Object> map, Class<T> beanClass) {
        if (Objects.isNull(map)) {
            return null;
        }
        T obj;
        try {
            obj = beanClass.newInstance();
            Field[] fields = beanClass.getDeclaredFields();
            for (Field field : fields) {
                int mod = field.getModifiers();
                if (Modifier.isFinal(mod) || Modifier.isStatic(mod)) {
                    continue;
                }
                field.setAccessible(true);
                field.set(obj, map.get(field.getName()));
            }
        } catch (Exception e) {
            log.warn("BeanUtils.map2Obj error:", e);
            obj = null;
        }
        return obj;
    }

    public static Map<String, Object> objToMap(Object obj) {
        if (Objects.isNull(obj)) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        try {
            Field[] declaredFields = obj.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                field.setAccessible(true);
                map.put(field.getName(), field.get(obj));
                field.setAccessible(false);
            }
        } catch (Exception e) {
            log.warn("BeanUtils.obj2Map error:", e);
        }
        return map;
    }


    public static <T> Map<String, Map<String, Object>> objToEmbedMap(T obj) {
        if (Objects.isNull(obj)) {
            return null;
        }
        Map<String, Map<String, Object>> map = new HashMap<>();
        try {
            Field[] declaredFields = obj.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                field.setAccessible(true);
                if (field.getName().equals("serialVersionUID")) {
                    continue;
                }
                if (field.getType() == Integer.class) {
                    Map<String, Object> fieldMap = new HashMap<>();
                    fieldMap.put("type", "integer");
                    map.put(field.getName(), fieldMap);
                } else if (field.getType() == String.class) {
                    Map<String, Object> fieldMap = new HashMap<>();
                    fieldMap.put("type", "text");
                    map.put(field.getName(), fieldMap);
                } else if (field.getType() == BigDecimal.class) {
                    Map<String, Object> fieldMap = new HashMap<>();
                    fieldMap.put("type", "text");
                    map.put(field.getName(), fieldMap);
                } else if (field.getType() == Long.class) {
                    Map<String, Object> fieldMap = new HashMap<>();
                    fieldMap.put("type", "long");
                    map.put(field.getName(), fieldMap);
                } else {
                    Map<String, Object> fieldMap = new HashMap<>();
                    fieldMap.put("type", "text");
                    map.put(field.getName(), fieldMap);
                }
                field.setAccessible(false);
            }
        } catch (Exception e) {
            log.warn("BeanUtils.obj2Map error:", e);
        }
        return map;
    }
}
