package edu.kit.ipd.parse.concurrency.filter;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.INode;

public class DependentNodesExtractor extends AbstractDependentNodesExtractor {

	// template method
	static ConcurrentAction extract(Keyphrase keyphrase, INode startingAction, INode endingAction, boolean left)
			throws MissingDataException {

		INode begin = determineBegin(startingAction, keyphrase, left);
		INode end = determineEnd(keyphrase, endingAction, left);
		return constructConcurrentAction(keyphrase, begin, end, startingAction, endingAction);
	}

	private static ConcurrentAction constructConcurrentAction(Keyphrase keyphrase, INode start, INode end, INode startingAction,
			INode endingAction) {
		ConcurrentAction result = new ConcurrentAction();
		result.setKeyphrase(keyphrase);
		result.addDependentAction(startingAction);
		result.addDependentAction(endingAction);
		result.addDependentPhrase(start);
		INode currNode = start;
		do {
			currNode = currNode.getOutgoingArcsOfType(GrammarFilter.nextArcType).get(0).getTargetNode();
			result.addDependentPhrase(currNode);
		} while (currNode != end);
		return result;
	}
}
