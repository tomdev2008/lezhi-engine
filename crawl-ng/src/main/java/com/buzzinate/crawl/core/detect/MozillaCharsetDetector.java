package com.buzzinate.crawl.core.detect;

import org.mozilla.universalchardet.UniversalDetector;

public class MozillaCharsetDetector {
	private static ThreadLocal<UniversalDetector> detector = new ThreadLocal<UniversalDetector>() {
		@Override
		protected UniversalDetector initialValue() {
			return new UniversalDetector(null) ;
		}
	};
	
	public static String detect(byte[] bs) {
		UniversalDetector d = detector.get();
		d.handleData(bs, 0, bs.length);
		d.dataEnd();
		String charset = d.getDetectedCharset();
		d.reset();
		return charset;
	}
}