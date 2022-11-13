package com.hk.remark.service.code;

import java.util.Arrays;
import java.util.Scanner;

/**
 * @author : HK意境
 * @ClassName : MaxStone
 * @date : 2022/11/8 19:48
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public class MaxStone {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        int m = scanner.nextInt();
        int k = scanner.nextInt();


        int[] cols = new int[m];
        int[] rows = new int[n];
        Arrays.fill(cols, 0);
        Arrays.fill(rows, 0);

        int r = 0;
        int c = 0;

        for (int i = 0; i < k; i++) {
            int x = scanner.nextInt()-1;
            int y = scanner.nextInt() - 1;
            cols[y] += 1;
            rows[x] += 1;

            r = Math.max(r,rows[x]);
            c = Math.max(c,cols[y]);
        }

        System.out.println(c + r -1);

    }

}
