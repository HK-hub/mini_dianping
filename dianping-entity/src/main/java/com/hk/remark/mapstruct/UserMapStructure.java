package com.hk.remark.mapstruct;

import com.hk.remark.entity.UserPO;
import com.hk.remark.vo.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author : HK意境
 * @ClassName : UserMapStructure
 * @date : 2022/10/28 20:38
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Mapper
public interface UserMapStructure {

    public UserMapStructure INSTANCE = Mappers.getMapper(UserMapStructure.class);


    /**
     * PO to VO
     * @param userPO
     * @return
     */
    public UserVO userPO2UserVO(UserPO userPO);

}
