package com.hk.remark.manager.impl;

import cn.hutool.core.collection.ArrayIter;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.hk.remark.entity.ShopTypePO;
import com.hk.remark.manager.IShopTypeManager;
import com.hk.remark.mapper.ShopTypeMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

/**
 * @author : HK意境
 * @ClassName : ShopTypeManagerImpl
 * @date : 2022/10/29 21:31
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Component
public class ShopTypeManagerImpl implements IShopTypeManager {

    @Resource
    private ShopTypeMapper shopTypeMapper;

    @Override
    public List<ShopTypePO> queryShopTypeList(LambdaQueryChainWrapper<ShopTypePO> wrapper) {
        // 查询集合：没有查询到元素将返回empty 集合
        List<ShopTypePO> list = wrapper.list();

        // 内存去重，排序
        list = list.stream()
                .sorted(Comparator.comparingInt(ShopTypePO::getSort).reversed())
                .collect(collectingAndThen(
                        toCollection(() -> new TreeSet<>(Comparator.comparing(ShopTypePO::getName))),
                        ArrayList::new));
        // 响应对象:
        return list;
    }
}
