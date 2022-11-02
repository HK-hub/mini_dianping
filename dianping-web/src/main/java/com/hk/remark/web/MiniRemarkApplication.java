package com.hk.remark.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author : HK意境
 * @ClassName : MiniRemarkApplication
 * @date : 2022/10/26 21:41
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.hk.remark.mapper")
@SpringBootApplication(scanBasePackages = "com.hk.remark")
public class MiniRemarkApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniRemarkApplication.class, args);
    }

}
