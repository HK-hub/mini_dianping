package com.hk.remark.vo;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : HK意境
 * @ClassName : UserVO
 * @date : 2022/10/28 20:32
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Data
public class UserVO implements Serializable {

    private Long id;
    private String nickName;
    private String icon;

    public static Map<String, String> toStringMap(UserVO userVO) {

        Map<String, String> map = new HashMap<>();
        map.put("id", userVO.id+"");
        map.put("nickName", userVO.nickName);
        map.put("icon", userVO.icon);

        return map;
    }

}
