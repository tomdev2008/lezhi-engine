package org.ansj.splitWord.analysis;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.ansj.domain.Term;
import org.ansj.splitWord.Analysis;
import org.ansj.util.Graph;
import org.ansj.util.recognition.AsianPersonRecognition;
import org.ansj.util.recognition.NumRecognition;

/**
 * 标准分词，召回前N条路径的词片，这样提高分词召回率
 * 
 * @author ansj
 * 
 */
public class ToRecallAnalysis extends Analysis {

	public ToRecallAnalysis(Reader reader) {
		super(reader);
	}

	
	@Override
	protected List<Term> getResult(final Graph graph) {
		Merger merger = new Merger() {
			@Override
			public List<Term> merger() {
				// graph.walkPath();

				// 数字发现
				if (graph.hasNum) {
					NumRecognition.recogntionNM(graph.terms);
				}

				// 姓名识别
				if (graph.hasPerson) {
					new AsianPersonRecognition(graph.terms).recogntion();
					//graph.walkPathByScore();
				}

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

	private ToRecallAnalysis() {
	};

	public static List<Term> paser(String str,int record) {
		return new ToRecallAnalysis().paserStr(str);
	}
}
