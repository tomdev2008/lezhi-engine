package com.buzzinate.util;

public class ByteUtil {
	public static byte[] int2bytes(int a) {
		byte[] result = new byte[4];

		result[0] = (byte) (a >> 24 & 0xff);
		result[1] = (byte) (a >> 16 & 0xff);
		result[2] = (byte) (a >> 8 & 0xff);
		result[3] = (byte) (a & 0xff);
		return result;
	}
}