package edu.kit.ipd.pronat.concurrency.filter;

import edu.kit.ipd.pronat.concurrency.data.ConcurrentAction;
import edu.kit.ipd.pronat.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.INode;

/**
 * @author Sebastian Weigelt
 */
public class SeparatingDependentNodesExtractor extends AbstractDependentNodesExtractor {

	// template method
	static ConcurrentAction extract(Keyphrase keyphrase, INode previousAction, INode followingAction) throws MissingDataException {

		INode prevBegin = determineBegin(previousAction, keyphrase, true);
		INode prevEnd = determineEnd(keyphrase, previousAction, true);
		INode followingBegin = determineBegin(followingAction, keyphrase, false);
		INode followingEnd = determineEnd(keyphrase, followingAction, false);
		return constructConcurrentAction(keyphrase, prevBegin, prevEnd, followingBegin, followingEnd, previousAction, followingAction);
	}

	private static ConcurrentAction constructConcurrentAction(Keyphrase keyphrase, INode prevStart, INode prevEnd, INode followingStart,
			INode followingEnd, INode previousAction, INode followingAction) {
		ConcurrentAction result = new ConcurrentAction();
		result.setKeyphrase(keyphrase);
		result.addDependentAction(previousAction);
		result.addDependentAction(followingAction);
		result.addDependentPhrase(prevStart);
		INode currNode = prevStart;
		while (currNode != prevEnd && !currNode.getOutgoingArcsOfType(GrammarFilter.nextArcType).isEmpty()) {
			currNode = currNode.getOutgoingArcsOfType(GrammarFilter.nextArcType).get(0).getTargetNode();
			result.addDependentPhrase(currNode);
		}
		result.addDependentPhrase(followingStart);
		currNode = followingStart;
		while (currNode != followingEnd && !currNode.getOutgoingArcsOfType(GrammarFilter.nextArcType).isEmpty()) {
			currNode = currNode.getOutgoingArcsOfType(GrammarFilter.nextArcType).get(0).getTargetNode();
			result.addDependentPhrase(currNode);
		}
		return result;
	}
}
