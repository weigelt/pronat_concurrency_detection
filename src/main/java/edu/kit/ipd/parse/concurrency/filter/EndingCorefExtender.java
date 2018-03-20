package edu.kit.ipd.parse.concurrency.filter;

import java.util.List;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.Pair;

public class EndingCorefExtender extends AbstractSpecializedCorefExtender {

	public EndingCorefExtender() {
		isLeft = true;
	}

	@Override
	protected int getBoundary(List<Pair<Integer, Integer>> boundaries, int i) {
		int result = Integer.MIN_VALUE;
		if (i > 0) {
			result = boundaries.get(i - 1).getRight();
		}
		return result;
	}

	@Override
	protected int getReferencePosition(ConcurrentAction concurrentAction) throws MissingDataException {
		return GrammarFilter.getPositionOfNode(concurrentAction.getDependentPhrases().get(0));
	}

}
