package org.ansj.splitWord.analysis;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.ansj.domain.Term;
import org.ansj.splitWord.Analysis;
import org.ansj.util.Graph;
import org.ansj.util.recognition.AsianPersonRecognition;
import org.ansj.util.recognition.ForeignPersonRecognition;
import org.ansj.util.recognition.NumRecognition;
import org.ansj.util.recognition.UserDefineRecognition;

/**
 * 标准分词
 * 
 * @author ansj
 * 
 */
public class ToAnalysis extends Analysis {
	public static final int USE_USER_DEFINE = 0x01;
	public static final int RECOGNTION_PERSION = 0x02;
	public static final int ALL = USE_USER_DEFINE | RECOGNTION_PERSION;
	private int option;
	
	public ToAnalysis(Reader reader) {
		this(reader, USE_USER_DEFINE | RECOGNTION_PERSION);
	}

	public ToAnalysis(Reader reader, int option) {
		super(reader);
		this.option = option;
	}

	@Override
	protected List<Term> getResult(final Graph graph) {
		Merger merger = new Merger() {
			@Override
			public List<Term> merger() {
				graph.walkPath();

				// 数字发现
				if (graph.hasNum) {
					NumRecognition.recogntionNM(graph.terms);
				}
				
				// 用户自定义词典的识别
				if (has(USE_USER_DEFINE)) new UserDefineRecognition(graph.terms).recongnitionTerm();

				// 姓名识别
				if (has(RECOGNTION_PERSION) && graph.hasPerson) {
					// 亚洲人名识别
					new AsianPersonRecognition(graph.terms).recogntion();
					graph.walkPathByScore();
					// 外国人名识别
					new ForeignPersonRecognition(graph.terms).recogntion();
				}
				
				// 用户自定义词典的识别
				if (has(USE_USER_DEFINE)) new UserDefineRecognition(graph.terms).recongnitionTerm();
				graph.rmLittlePath();
				graph.walkPathByFreq();

				return getResult();
			}

			private List<Term> getResult() {
				List<Term> result = new ArrayList<Term>();
				int length = graph.terms.length - 1;
				for (int i = 0; i < length; i++) {
					if (graph.terms[i] != null) {
						result.add(graph.terms[i]);
					}
				}
				return result;
			}
		};
		return merger.merger();
	}

	private ToAnalysis() {
	};
	
	private boolean has(int f) {
        return (option & f) != 0;
    }

	public static List<Term> paser(String str) {
		return new ToAnalysis().paserStr(str);
	}
}
