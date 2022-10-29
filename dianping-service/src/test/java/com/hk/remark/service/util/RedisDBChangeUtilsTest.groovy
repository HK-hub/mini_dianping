package com.hk.remark.service.util

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @ClassName : RedisDBChangeUtilsTest
 * @author : HK意境
 * @date : 2022/10/29 16:29
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
class RedisDBChangeUtilsTest extends Specification {

    @Unroll
    def "GetIndex"() {
        given:

        when:
        def res = RedisDBChangeUtils.getIndex(topic)


        then:
        res == index

        where:
        topic       || index
        "login:code:"  ||   1
        "login:token:"  ||  4
        "login:user:"   ||  4
        "hot:blog:"     ||  5
    }
}
