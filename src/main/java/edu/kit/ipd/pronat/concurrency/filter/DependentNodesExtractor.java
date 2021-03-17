package edu.kit.ipd.pronat.concurrency.filter;

import edu.kit.ipd.pronat.concurrency.data.ConcurrentAction;
import edu.kit.ipd.pronat.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.INode;

/**
 * @author Sebastian Weigelt
 */
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
		while (currNode != end && !currNode.getOutgoingArcsOfType(GrammarFilter.nextArcType).isEmpty()) {
			currNode = currNode.getOutgoingArcsOfType(GrammarFilter.nextArcType).get(0).getTargetNode();
			result.addDependentPhrase(currNode);
		}
		return result;
	}
}
