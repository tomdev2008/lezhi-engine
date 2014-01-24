package com.buzzinate.util;

import java.util.Arrays;

public class MinhashUtil {
    private static final boolean[] digitmark = new boolean[128];
    private static final int RADIX = Character.MAX_RADIX;
    private static final int MAX_HASHES = 200;

    static {
        Arrays.fill(digitmark, false);
        for (char ch = '0'; ch <= '9'; ch ++) digitmark[ch] = true;
        for (char ch = 'a'; ch <= 'z'; ch ++) digitmark[ch] = true;
    }

    private MinhashUtil() { }

    public static int[] init(int n) {
        int[] r = new int[n];
        Arrays.fill(r, Integer.MAX_VALUE);
        return r;
    }

    public static int merge(int[] minhashes, int[] delta) {
        assert minhashes.length == delta.length;
        int changed = 0;
        for (int i = 0; i < minhashes.length; i++) {
            if (minhashes[i] > delta[i]) {
                minhashes[i] = delta[i];
                changed += 1;
            }
        }
        return changed;
    }

    public static String format(int[] minhashes, String sep) {
        StringBuffer sb = new StringBuffer(256);
        for (int i = 0; i < minhashes.length; i++) {
            if (i > 0) sb.append(sep);
            sb.append(Integer.toString(i, RADIX)).append("-").append(Integer.toString(minhashes[i], RADIX));
        }
        return sb.toString();
    }

    public static int[] parse(String str) {
        int[] minhashes = new int[MAX_HASHES];
        int idx = 0;
        int last = 0;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (digitmark[ch] == false) {
                if (i > last && ch != '-') minhashes[idx++] = Integer.parseInt(str.substring(last, i), RADIX);
                last = i + 1;
            }
        }
        if (str.length() > last) minhashes[idx++] = Integer.parseInt(str.substring(last), RADIX);
        return Arrays.copyOf(minhashes, idx);
    }

    public static String buckets(int[] minhashes, int groupSize, String sep) {
        StringBuffer sb = new StringBuffer(128);
        for (int i = 0; i < minhashes.length; i+= groupSize) {
            int min = minhashes[i];
            int max = min;
            for (int j = i + 1; j < i + groupSize && j < minhashes.length; j++) {
                if (minhashes[j] < min) min = minhashes[j];
                else if (minhashes[j] > max) max = minhashes[j];
            }
            int g = i / groupSize;
            if (i > 0) sb.append(sep);
            sb.append(Integer.toString(g, RADIX)).append("-").append(Integer.toString(min, RADIX)).append(sep);
            sb.append(Integer.toString(g, RADIX)).append("+").append(Integer.toString(max, RADIX));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String str = format(new int[]{1, 3453, 3, 42342342, 52, 623423423}, " ");
        int[] p = parse(str);
        System.out.println(str);
        System.out.println(Arrays.toString(p));
        System.out.println(buckets(p, 3, " "));
    }
}
