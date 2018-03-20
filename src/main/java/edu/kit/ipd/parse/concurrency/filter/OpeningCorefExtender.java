package edu.kit.ipd.parse.concurrency.filter;

import java.util.List;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.Pair;

public class OpeningCorefExtender extends AbstractSpecializedCorefExtender {

	public OpeningCorefExtender() {
		isLeft = false;
	}

	@Override
	protected int getBoundary(List<Pair<Integer, Integer>> boundaries, int i) {
		int result = Integer.MAX_VALUE;
		if (boundaries.size() > i + 1) {
			result = boundaries.get(i + 1).getLeft();
		}
		return result;
	}

	@Override
	protected int getReferencePosition(ConcurrentAction concurrentAction) throws MissingDataException {
		return GrammarFilter
				.getPositionOfNode(concurrentAction.getDependentPhrases().get(concurrentAction.getDependentPhrases().size() - 1));
	}

}
