package com.hk.remark.service.code;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Scanner;

/**
 * @author : HK意境
 * @ClassName : SpArray
 * @date : 2022/11/8 19:31
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public class SpArray {


    public static int get(int n){
        if (n == 1){
            return 1;
        }

        BigInteger bigInteger = new BigInteger(String.valueOf(1L));

        for (int i = 0; i < n-1; i++) {
            if (i % 2 == 0){
                bigInteger = bigInteger.add(new BigInteger("1"));
            }else {
                bigInteger = bigInteger.multiply(new BigInteger("2"));
            }
        }

        return bigInteger.mod(BigInteger.valueOf((long) (1e9+7))).intValue();
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        System.out.println(get(n));


    }




}
