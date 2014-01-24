package org.buzzinate.lezhi.test;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.SimpleFSDirectory;
import org.buzzinate.lezhi.query.SegmentQueryFilter;
import org.buzzinate.lezhi.util.StringUtils;

import java.io.File;
import java.io.IOException;

public class TestMinhashQuery {
	public static void main(String[] args) throws IOException {
        DirectoryReader reader = DirectoryReader.open(new SimpleFSDirectory(new File("D:/data/user_testbed")));
		IndexSearcher searcher = new IndexSearcher(reader);
		
		String buckets = "0-7blfp 0+1a67bf 1-39wd1 1+htdp5 2-1djc5 2+ognpa 3-8uknr 3+ud424 4-10lrx 4+uu0bv 5-8bwgj 5+lsalo 6-7piuc 6+10ojyg 7-6epl 7+3flki3 8-27ix9 8+gg5h0 9-qpsi3 9+1g89ml a-1i1ja a+myz21 b-th2z b+kijo4 c-2ekdj c+h2pb5 d-a2nw0 d+1efwku e-16r8p e+1h9qfa f-562g1 f+tb8w3 g-1v6tj g+16ztib h-c58ah h+rehtt i-7fq2r i+13g6yf j-cbgkb j+1267tq";
		String minhashes = "0-yj1z9 1-1a67bf 2-15wb3a 3-qp1jv 4-7blfp 5-htdp5 6-5ug7q 7-7jhmp 8-39wd1 9-41u4t a-4cgk0 b-4jd4i c-ognpa d-bxumy e-1djc5 f-8uknr g-g5hkv h-kxjea i-ud424 j-a4jsu k-10lrx l-uu0bv m-dqdvf n-plp0z o-iy5b4 p-axwh5 q-epq9d r-eslj1 s-8bwgj t-lsalo u-9yd75 v-7piuc w-r3y8r x-10ojyg y-c121m z-3flki3 10-6epl 11-g19pd 12-k4x4c 13-1asjjh 14-7p7mr 15-fepae 16-gg5h0 17-27ix9 18-9x3hw 19-viu87 1a-1g89ml 1b-15nch0 1c-qpsi3 1d-r0ax0 1e-1i1ja 1f-36wcw 1g-myz21 1h-kanq5 1i-9mrds 1j-th2z 1k-9ts45 1l-dsy69 1m-avm6x 1n-kijo4 1o-h2pb5 1p-61p81 1q-b0vd2 1r-43sz6 1s-2ekdj 1t-1efwku 1u-qanc1 1v-ggxuz 1w-doaz5 1x-a2nw0 1y-42app 1z-1h9qfa 20-110o9n 21-16r8p 22-2nbuq 23-gkikx 24-7akpf 25-tb8w3 26-fh4jf 27-562g1 28-1v6tj 29-16ztib 2a-h9uco 2b-2j46j 2c-i2ebd 2d-egstc 2e-c58ah 2f-rehtt 2g-e7js5 2h-hmb30 2i-7fq2r 2j-13g6yf 2k-iy3b2 2l-a02y8 2m-x8365 2n-ligei 2o-cbgkb 2p-dagml 2q-1267tq 2r-i87ec";
        int topn = 40;

        BooleanQuery bq = new BooleanQuery();
        for (String bucket: StringUtils.split(buckets, ' ')) bq.add(new TermQuery(new Term("bucket", bucket)), BooleanClause.Occur.SHOULD);

        BooleanQuery mq = new BooleanQuery();
        for (String minhash: StringUtils.split(minhashes, ' ')) {
            mq.add(new TermQuery(new Term("minhashes", minhash)), BooleanClause.Occur.SHOULD);
        }

//        for (int i = 0; i < Integer.MAX_VALUE; i ++) {
//        long start = System.currentTimeMillis();
//        searcher.search(mq, bf, topn);
//        System.out.println("cost: " + (System.currentTimeMillis() - start));
//        }

        TopDocs docs = searcher.search(SegmentQueryFilter.levelQuery(mq, bq, topn * 2), topn);
        System.out.println("total hits: " + docs.totalHits);
        for (ScoreDoc sd: docs.scoreDocs) {
            System.out.println(sd.score + " <- " + searcher.doc(sd.doc).get("id"));
        }

        reader.close();
	}
}