package com.hk.remark.web.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @ClassName : ResourceHolder
 * @author : HK意境
 * @date : 2022/10/28 13:51
 * @description : 请求资源保存
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public class ResourceHolder {
    private static final ThreadLocal threadLocal = new ThreadLocal();
    private static Map<String, Object> resourceMap = new HashMap<>();

    public static void save(String key,Object resource){
        resourceMap.put(key, resource);
        threadLocal.set(resourceMap);
    }

    public static Object get(String key){

        // 检查资源是否存在
        if (Objects.isNull(threadLocal.get())) {
            // 资源不存在
            return null;
        }
        Map<String,Object> resources = (Map<String, Object>) threadLocal.get();
        return resources.get(key);
    }

    public static void remove(){

        // 检查资源是否存在
        if (Objects.isNull(threadLocal.get())) {
            return ;
        }
        threadLocal.remove();
        resourceMap = null;
    }
}
