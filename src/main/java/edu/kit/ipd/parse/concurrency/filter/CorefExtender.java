package edu.kit.ipd.parse.concurrency.filter;

import java.util.ArrayList;
import java.util.List;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.Pair;

public class CorefExtender {

	ISpecializedCorefExtender spcex;

	public CorefExtender() {

	}

	public void extendBlocks(List<ConcurrentAction> concurrentActions) throws MissingDataException {
		List<Pair<Integer, Integer>> boundaries = getBoundaries(concurrentActions);
		for (int i = 0; i < concurrentActions.size(); i++) {
			ConcurrentAction concAction = concurrentActions.get(i);
			switch (concAction.getUsedType()) {
			case WRAPPING:
				break;

			case SEPARATING:

				break;
			case OPENING:
				spcex = new OpeningCorefExtender();
				break;
			case ENDING:

				break;
			default:
				break;
			}
			spcex.extendBlocks(concAction, boundaries, i);
		}
	}

	private List<Pair<Integer, Integer>> getBoundaries(List<ConcurrentAction> concurrentActions) throws MissingDataException {
		List<Pair<Integer, Integer>> result = new ArrayList<>(concurrentActions.size());
		for (ConcurrentAction concurrentAction : concurrentActions) {

			List<INode> nodes = concurrentAction.getKeyphrase().getAttachedNodes();
			if (!nodes.isEmpty()) {
				int begin = GrammarFilter.getPositionOfNode(nodes.get(0));
				int end = GrammarFilter.getPositionOfNode(nodes.get(nodes.size() - 1));
				result.add(new Pair<Integer, Integer>(begin, end));
			}

		}
		return result;
	}
}
