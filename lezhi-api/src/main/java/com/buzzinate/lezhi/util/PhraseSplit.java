package com.buzzinate.lezhi.util;

import com.buzzinate.nlp.segment.AtomSplit;
import com.buzzinate.nlp.util.TextUtil;
import org.ansj.domain.Term;
import org.ansj.splitWord.Segment;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhraseSplit {
    private static Pattern[] pats = new Pattern[]{ Pattern.compile("《([^《^》]*)》"), Pattern.compile("#([^#]*)#") , Pattern.compile("【([^【^】]*)】"), Pattern.compile("“([^“^”]*)”")};

    public static void main(String[] args) throws IOException {
        System.out.println(splitTitle("中新网社区 - 中国媒体：朝鲜赌注越来越高 中国必须适当惩罚(2)"));
        System.out.println(splitTitle("该名乘客,三星i9100真伪查询352110052278091帮助查一下这"));
        System.out.println(splitTitle("张国荣《继续宠爱十年音乐会》莫文蔚演唱《只怕不再遇上》视频 - 娱乐视频 - 21CN.COM"));
        System.out.println(splitTitle("2011-34-43 为中国定制 英菲尼迪M35hL北京车展首发"));
        System.out.println(splitKeyword("Dan Haywood's Domain-Driven Design of the Using Naked Objects - infoq com"));
        System.out.println(splitKeyword("中新博客精选--揭秘：邓小平一生都拒绝辞去的职务(2)"));
        System.out.println(StringUtils.join(splitTitle("实拍日本人真实的家中生活-中新社区"), " | "));
    }

    private static Set<String> extractQuote(String text) {
        HashSet<String> words = new HashSet<String>();
        for (Pattern pat: pats) {
            Matcher matcher = pat.matcher(text);
            while (matcher.find()) {
                String phrase = matcher.group(1);
                words.add(phrase);
                words.add(substringBeforeAny(phrase, ":："));
                words.add(substringBeforeAny(phrase, "0123456789"));
                words.add(substringBeforeAny(phrase, "II"));
            }
        }
        return words;
    }

    private static String substringBeforeAny(String str, String separators) {
        int pos = StringUtils.indexOfAny(str, separators);
        if (pos == StringUtils.INDEX_NOT_FOUND) return str;
        else return str.substring(0, pos);
    }

    private static List<String> splitSnippets(String text) {
        List<String> result = new ArrayList<String>();
        int last = 0;
        char prev = ' ';
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (TextUtil.isAlphaOrDigit(prev)) {
                if (ch == '\'' || ch == '-' || Character.isWhitespace(ch)) {
                    prev = ch;
                    continue;
                }
            } else if (!Character.isLetterOrDigit(text.charAt(i))) {
                if (i > last) {
                    String sub = text.substring(last, i);
                    if (TextUtil.countNumWords(sub) > 1) result.add(sub);
                }
                last = i + 1;
            }
            prev = ch;
        }
        if (text.length() > last) {
            String sub = text.substring(last);
            if (TextUtil.countNumWords(sub) > 1) result.add(sub);
        }
        return result;
    }

    public static List<String> splitKeyword(String keyword) throws IOException {
        List<String> result = new ArrayList<String>();
        for (String subtext: splitSnippets(keyword)) {
            for (String snippet: AtomSplit.splitSnippets(subtext)) {
                result.add(replaceWhitespace(TextUtil.stemAll(snippet), '_'));
            }
        }
        return result;
    }

    private static String replaceWhitespace(String str, char mark) {
        StringBuffer sb = new StringBuffer();
        int last = 0;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (Character.isWhitespace(ch)) {
                if (i > last) sb.append(str.substring(last, i)).append(mark);
                last = i + 1;
            }
        }

        if (str.length() > last) sb.append(str.substring(last));
        return sb.toString();
    }

    public static List<Phrase> splitTitle(String text) throws IOException {
        List<Phrase> phrases = new ArrayList<Phrase>();
        for (String subtext: splitSnippets(text)) {
            for (String snippet: AtomSplit.splitSnippets(subtext)) {
                List<String> words = new ArrayList<String>();
                for (Term term: Segment.split(snippet)) words.add(TextUtil.stem(term.getName()));
                phrases.add(new Phrase(words));
            }
        }
        for (String quoteWord: extractQuote(text)) {
            quoteWord = replaceWhitespace(TextUtil.stemAll(quoteWord), '_');
            phrases.add(new Phrase(Arrays.asList(quoteWord)));
        }
        return phrases;
    }
}