package edu.kit.ipd.parse.concurrency.filter;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.INode;

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
		do {
			currNode = currNode.getOutgoingArcsOfType(GrammarFilter.nextArcType).get(0).getTargetNode();
			result.addDependentPhrase(currNode);
		} while (currNode != prevEnd);
		result.addDependentPhrase(followingStart);
		currNode = followingStart;
		do {
			currNode = currNode.getOutgoingArcsOfType(GrammarFilter.nextArcType).get(0).getTargetNode();
			result.addDependentPhrase(currNode);
		} while (currNode != followingEnd);
		return result;
	}
}
