package edu.kit.ipd.parse.concurrency.filter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.Pair;

public class OpeningCorefExtender implements ISpecializedCorefExtender {

	@Override
	public void extendBlocks(ConcurrentAction concurrentAction, List<Pair<Integer, Integer>> boundaries, int i)
			throws MissingDataException {
		Set<INode> entities = getEntities(concurrentAction.getDependentPhrases());

	}

	private Set<INode> getEntities(List<INode> dependentPhrases) {
		Set<INode> entities = new HashSet<>();
		for (INode node : dependentPhrases) {
			List<? extends IArc> outgoing = node.getOutgoingArcsOfType(CorefExtender.referenceArcType);
			if (!outgoing.isEmpty() && outgoing.get(0).getSourceNode().getType().equals(CorefExtender.entityNodeType)) {
				entities.add(outgoing.get(0).getSourceNode());
			}
		}
		return entities;
	}

}
