package com.hk.remark.mapstruct;

import com.hk.remark.entity.ShopTypePO;
import com.hk.remark.vo.ShopTypeVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author : HK意境
 * @ClassName : ShopTypeMapStructure
 * @date : 2022/10/29 23:56
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Mapper
public interface ShopTypeMapStructure {

    public static ShopTypeMapStructure INSTANCE = Mappers.getMapper(ShopTypeMapStructure.class);

    /**
     * po 转 vo
     * @param shopTypePO
     * @return
     */
    public ShopTypeVO shopTypePO2VO(ShopTypePO shopTypePO);

}
