package com.buzzinate.lezhi.util;

import com.buzzinate.nlp.segment.Atom;
import com.buzzinate.nlp.segment.AtomSplit;
import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Phrase {
    private final List<String> words;

    public Phrase(List<String> words) {
        this.words = words;
    }

    @Override
    public String toString() {
        return StringUtils.join(words, " ");
    }

    public static Set<String> possibleWords(List<Phrase> phrases) {
        HashSet<String> wordset = new HashSet<String>();
        for (Phrase p: phrases) {
            List<String> words = p.words;
            double[] lens = new double[words.size()];
            for (int i = 0; i < lens.length; i++) lens[i] = AtomSplit.atomLength(words.get(i));
            for (int i = 0; i < lens.length; i++) {
                wordset.add(words.get(i));
                double sumlen = lens[i];
                for (int j = i+1; j < lens.length; j++) {
                    sumlen += lens[j];
                    if (sumlen <= 5) wordset.add(Atom.join(words.subList(i, j + 1)));
                }
            }
        }
        return wordset;
    }
}
