package edu.kit.ipd.parse.concurrency.filter;

import java.util.ArrayList;
import java.util.List;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.concurrency.data.Keyphrase;

public class GrammarFilter {

	ISpecializedGrammarFilter spg;

	public GrammarFilter() {
		//TODO: constructor
	}

	public List<ConcurrentAction> filter(List<Keyphrase> keyphrases) {
		List<ConcurrentAction> conActions = new ArrayList<>();
		for (Keyphrase keyphrase : keyphrases) {
			switch (keyphrase.getType()) {
			case WRAPPING:
				spg = new WrappingGrammarFilter();
				break;
			case OPENING:
				spg = new OpeningGrammarFilter();
				break;
			case SEPARATING:
				spg = new SeparatingGrammarFilter();
				break;
			default:
				break;
			}
			conActions.add(spg.filter());
		}
		return conActions;
	}
}
