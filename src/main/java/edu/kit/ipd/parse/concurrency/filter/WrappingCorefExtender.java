package edu.kit.ipd.parse.concurrency.filter;

import java.util.List;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.Pair;

public class WrappingCorefExtender extends AbstractSpecializedCorefExtender {

	@Override
	protected int getReferencePosition(ConcurrentAction concurrentAction) throws MissingDataException {
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

	@Override
	protected int getBoundary(List<Pair<Integer, Integer>> boundaries, int i) {
		int result;
		if (isLeft) {
			result = Integer.MIN_VALUE;
			if (i > 0) {
				result = boundaries.get(i - 1).getRight();
			}
		} else {
			result = Integer.MAX_VALUE;
			if (boundaries.size() > i + 1) {
				result = boundaries.get(i + 1).getLeft();
			}
		}
		return result;
	}

}
