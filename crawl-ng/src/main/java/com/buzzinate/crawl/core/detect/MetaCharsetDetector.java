package com.buzzinate.crawl.core.detect;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetaCharsetDetector {
	private static int CHUNK_SIZE = 6000;
	private static Pattern metaPattern = Pattern.compile("<meta\\s+([^>]*http-equiv=\"?content-type\"?[^>]*)>", Pattern.CASE_INSENSITIVE);
	private static Pattern xmlnsPattern = Pattern.compile("<html\\s+([^>]*xmlns=\"?[^>]*)>", Pattern.CASE_INSENSITIVE);
	private static Pattern langPattern = Pattern.compile("lang=\"?([a-z][_\\-0-9a-z]*)", Pattern.CASE_INSENSITIVE);
	private static Pattern charsetPattern = Pattern.compile("charset=\\s*([a-z][_\\-0-9a-z]*)", Pattern.CASE_INSENSITIVE);
	
	public static List<String> sniffCharacterEncoding(byte[] content) {
		int length = content.length < CHUNK_SIZE ? content.length : CHUNK_SIZE;
		// We don't care about non-ASCII parts so that it's sufficient
		// to just inflate each byte to a 16-bit value by padding.
		// For instance, the sequence {0x41, 0x82, 0xb7} will be turned into
		// {U+0041, U+0082, U+00B7}.
		String str = new String(content, 0, length);
		
		List<String> charsets = new ArrayList<String>();
		Matcher metaMatcher = metaPattern.matcher(str);
		if (metaMatcher.find()) {
			Matcher charsetMatcher = charsetPattern.matcher(metaMatcher.group(1));
			if (charsetMatcher.find()) charsets.add(new String(charsetMatcher.group(1)));
		}
		
		Matcher xmlnsMatcher = xmlnsPattern.matcher(str);
		if (xmlnsMatcher.find()) {
			Matcher langMatcher = langPattern.matcher(xmlnsMatcher.group(1));
			if (langMatcher.find()) charsets.add(new String(langMatcher.group(1)));
		}
		return charsets;
	}
}