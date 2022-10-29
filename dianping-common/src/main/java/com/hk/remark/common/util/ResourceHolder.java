package com.hk.remark.common.util;

import cn.hutool.core.map.MapUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private static Map<String, Object> resourceMap ;

    public static void save(String key,Object resource){
        if (MapUtil.isEmpty(resourceMap)){
            resourceMap = new HashMap<>();
        }
        resourceMap.put(key, resource);
        threadLocal.set(resourceMap);
        System.out.println("resourceholder add resource:"+resourceMap);
    }

    public static Object get(String key){

        // 检查资源是否存在
        if (Objects.isNull(threadLocal.get())) {
            // 资源不存在
            return null;
        }
        Map<String,Object> resources = (Map<String, Object>) threadLocal.get();
        System.out.println("resourceholder get resource:"+resources.get(key));
        return resources.get(key);
    }

    public static void remove(){

        // 检查资源是否存在
        if (Objects.isNull(threadLocal.get())) {
            return ;
        }
        threadLocal.remove();
        resourceMap = null;
        System.out.println("ResourceHolder remove resource");
    }
}
