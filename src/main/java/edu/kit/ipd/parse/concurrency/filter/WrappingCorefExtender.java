package edu.kit.ipd.parse.concurrency.filter;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.luna.data.MissingDataException;

public class WrappingCorefExtender extends AbstractSpecializedCorefExtender {

	@Override
	protected int getReferencePosition(ConcurrentAction concurrentAction, boolean left) throws MissingDataException {
		if (GrammarFilter.getPositionOfNode(concurrentAction.getDependentActions().get(0)) < GrammarFilter
				.getPositionOfNode(concurrentAction.getKeyphrase().getAttachedNodes().get(0))) {
			isLeft = true;
			return GrammarFilter.getPositionOfNode(concurrentAction.getDependentPhrases().get(0));

		} else {
			isLeft = false;
			return GrammarFilter
					.getPositionOfNode(concurrentAction.getDependentPhrases().get(concurrentAction.getDependentPhrases().size() - 1));
		}
	}

}
