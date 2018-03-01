package edu.kit.ipd.parse.concurrency.filter;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.INode;

public class WrappingGrammarFilter implements ISpecializedGrammarFilter {

	public WrappingGrammarFilter() {
	}

	@Override
	public ConcurrentAction filter(Keyphrase keyphrase) throws MissingDataException {

		INode[] leftActions = new INode[3];
		leftActions[0] = keyphrase.getAttachedNodes().get(0);
		INode[] rightActions = new INode[3];
		rightActions[0] = keyphrase.getAttachedNodes().get(keyphrase.getAttachedNodes().size() - 1);

		boolean leftAnd = GrammarFilter.findActionNodes(leftActions, true);
		boolean rightAnd = GrammarFilter.findActionNodes(rightActions, false);

		INode firstLeftAction = leftActions[1];
		INode secondLeftAction = leftActions[2];
		INode firstRightAction = rightActions[1];
		INode secondRightAction = rightActions[2];

		ConcurrentAction result = null;
		if (firstRightAction != null && secondRightAction != null && rightAnd) {
			result = WrappedDependentNodesExtractor.extract(keyphrase, firstRightAction, secondRightAction, false);
		} else if (firstLeftAction != null && secondLeftAction != null && leftAnd) {
			result = WrappedDependentNodesExtractor.extract(keyphrase, secondLeftAction, firstLeftAction, true);
		} else if (firstRightAction != null && secondRightAction != null) {
			result = WrappedDependentNodesExtractor.extract(keyphrase, firstRightAction, secondRightAction, false);
		} else {
			//TODO: what now?
		}
		return result;
	}
}
