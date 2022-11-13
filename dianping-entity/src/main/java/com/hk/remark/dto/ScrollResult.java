package com.hk.remark.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @ClassName : ScrollResult
 * @author : HK意境
 * @date : 2022/11/12 21:04
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Data
@Accessors(chain = true)
public class ScrollResult<T> {
    private List<T> list;
    private Long minTime;
    private Integer offset;
}
