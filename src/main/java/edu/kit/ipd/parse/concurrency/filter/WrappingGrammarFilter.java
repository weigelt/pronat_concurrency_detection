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

		boolean leftAnd = findActionNodes(leftActions, true);
		boolean rightAnd = findActionNodes(rightActions, false);

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

	private boolean findActionNodes(INode[] actions, boolean left) {
		boolean foundAnd = false;
		while ((left ? !actions[0].getIncomingArcsOfType(GrammarFilter.nextArcType).isEmpty()
				: !actions[0].getOutgoingArcsOfType(GrammarFilter.nextArcType).isEmpty()) && actions[2] == null) {
			actions[0] = left ? actions[0].getIncomingArcsOfType(GrammarFilter.nextArcType).get(0).getSourceNode()
					: actions[0].getOutgoingArcsOfType(GrammarFilter.nextArcType).get(0).getTargetNode();
			if (actions[0].getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_ROLE) != null
					&& actions[0].getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_ROLE).toString()
							.equalsIgnoreCase(GrammarFilter.ATTRIBUTE_VALUE_PREDICATE)) {
				if (actions[1] == null) {
					actions[1] = actions[0];
				} else {
					actions[2] = actions[0];
				}
			}
			if (actions[1] != null) {
				if (actions[0].getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_VALUE).toString().equalsIgnoreCase(GrammarFilter.WORD_AND)) {
					foundAnd = true;
				}
			}
		}
		return foundAnd;
	}
}
