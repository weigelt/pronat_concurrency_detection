package edu.kit.ipd.parse.concurrency.filter;

import java.util.ArrayList;
import java.util.List;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.INodeType;
import edu.kit.ipd.parse.luna.graph.Pair;
import edu.kit.ipd.parse.luna.graph.ParseGraph;

public class CorefExtender {

	ISpecializedCorefExtender spcex;
	private final ParseGraph pgStub = new ParseGraph();
	static IArcType referenceArcType;
	static INodeType entityNodeType;

	private static final String REFERENCE = "reference";
	private static final String ENTITY_NODE_TYPE = "contextEntity";

	public CorefExtender() {
		referenceArcType = pgStub.createArcType(REFERENCE);
		entityNodeType = pgStub.createNodeType(ENTITY_NODE_TYPE);
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
