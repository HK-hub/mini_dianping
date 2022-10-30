package com.hk.remark.mapstruct;

import com.hk.remark.entity.ShopPO;
import com.hk.remark.vo.ShopVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author : HK意境
 * @ClassName : ShopMapStructure
 * @date : 2022/10/29 21:05
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Mapper
public interface ShopMapStructure {

    public static ShopMapStructure INSTANCE = Mappers.getMapper(ShopMapStructure.class);

    // PO to VO
    public ShopVO shopPO2ShopVO(ShopPO shopPO);

    // VO to PO
    public ShopPO shopVO2ShopPO(ShopVO shopVO);



}
