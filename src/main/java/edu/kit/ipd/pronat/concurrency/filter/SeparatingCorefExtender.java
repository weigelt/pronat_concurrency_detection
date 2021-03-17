package edu.kit.ipd.pronat.concurrency.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.kit.ipd.pronat.concurrency.data.ConcurrentAction;
import edu.kit.ipd.pronat.concurrency.data.Utterance;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.Pair;

/**
 * @author Sebastian Weigelt
 */
public class SeparatingCorefExtender extends AbstractSpecializedCorefExtender {

	@Override
	public void extendBlocks(ConcurrentAction concurrentAction, List<Pair<Integer, Integer>> boundaries, int i, Utterance utterance)
			throws MissingDataException {
		if (!concurrentAction.getDependentPhrases().isEmpty()) {
			List<List<INode>> splitted = splitDependentPhrases(concurrentAction);
			boolean left = true;
			for (List<INode> split : splitted) {
				Set<INode> entities = getEntities(split);

				int refPosition = getReferencePosition(concurrentAction, left);
				int boundary = getBoundary(boundaries, i, left);
				for (INode entity : entities) {
					int result = getMaxPositionFromCorefChain(entity, refPosition, boundary, left);
					if (checkIfExtending(result, refPosition, boundary, left)) {
						refPosition = result;
					}
				}
				refPosition = determineFinalRefPosition(utterance.giveUtteranceAsNodeList().get(refPosition), boundary, left);
				extendDependentPhrase(concurrentAction, refPosition, getReferencePosition(concurrentAction, left),
						utterance.giveUtteranceAsNodeList(), left);
				left = false;
			}
		}

	}

	private List<List<INode>> splitDependentPhrases(ConcurrentAction concurrentAction) throws MissingDataException {
		List<List<INode>> result = new ArrayList<>();
		List<INode> left = new ArrayList<>();
		List<INode> right = new ArrayList<>();
		int splittingPosition = GrammarFilter.getPositionOfNode(concurrentAction.getKeyphrase().getAttachedNodes().get(0));
		for (INode node : concurrentAction.getDependentPhrases()) {
			if (GrammarFilter.getPositionOfNode(node) < splittingPosition) {
				left.add(node);
			} else {
				right.add(node);
			}
		}
		result.add(left);
		result.add(right);
		return result;

	}

}
