package edu.kit.ipd.parse.concurrency.filter;

import java.util.List;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.ParseGraph;

public class WrappingGrammarFilter implements ISpecializedGrammarFilter {

	private ParseGraph pgStub;
	static IArcType nextArcType;
	static IArcType actionAnalyzerArcType;

	private static final String ARC_TYPE_RELATION_IN_ACTION = "relationInAction";
	private static final String ARC_TYPE_RELATION = "relation";
	static final String ATTRIBUTE_NAME_POSITION = "position";
	private static final String ATTRIBUTE_NAME_VALUE = "value";
	private static final String ATTRIBUTE_NAME_ROLE = "role";
	static final String ATTRIBUTE_NAME_TYPE = "type";
	static final String ATTRIBUTE_VALUE_PREDICATE_TO_PARA = "PREDICATE_TO_PARA";
	private static final String ATTRIBUTE_VALUE_PREDICATE = "PREDICATE";
	private static final String WORD_AND = "and";

	public WrappingGrammarFilter() {
		pgStub = new ParseGraph();
		nextArcType = pgStub.createArcType(ARC_TYPE_RELATION);
		//		nextArcType.addAttributeToType("String", "value");
		actionAnalyzerArcType = pgStub.createArcType(ARC_TYPE_RELATION_IN_ACTION);
	}

	@Override
	public ConcurrentAction filter(Keyphrase keyphrase) {

		INode[] leftActions = new INode[3];
		leftActions[0] = keyphrase.getAttachedNode().get(0);
		INode[] rightActions = new INode[3];
		rightActions[0] = keyphrase.getAttachedNode().get(keyphrase.getAttachedNode().size() - 1);

		boolean leftAnd = findActionNodes(leftActions, true);
		boolean rightAnd = findActionNodes(rightActions, false);

		INode firstLeftAction = leftActions[1];
		INode secondLeftAction = leftActions[2];
		INode firstRightAction = rightActions[1];
		INode secondRightAction = rightActions[2];

		INode depNodeBegin = null;
		INode depNodeEnd = null;
		int start;
		int end;

		if (firstRightAction != null && secondRightAction != null && rightAnd) {
			//TODO
		} else if (firstLeftAction != null && secondLeftAction != null && leftAnd) {
			depNodeBegin = secondLeftAction;
			start = (int) secondLeftAction.getAttributeValue(ATTRIBUTE_NAME_POSITION);
			List<? extends IArc> outgoingSecondActionArcs = secondLeftAction.getOutgoingArcsOfType(actionAnalyzerArcType);
			for (IArc iArc : outgoingSecondActionArcs) {
				if (iArc.getAttributeValue(ATTRIBUTE_NAME_TYPE).toString().equalsIgnoreCase(ATTRIBUTE_VALUE_PREDICATE_TO_PARA)) {
					INode currTargetNode = iArc.getTargetNode();
					if ((int) currTargetNode.getAttributeValue(ATTRIBUTE_NAME_POSITION) < start) {
						start = (int) currTargetNode.getAttributeValue(ATTRIBUTE_NAME_POSITION);
						depNodeBegin = currTargetNode;
					}
				} else {
					continue;
				}
			}
			//TODO: incorrect if action is composed of multiple words
			depNodeEnd = firstLeftAction;
			end = (int) firstLeftAction.getAttributeValue(ATTRIBUTE_NAME_POSITION);
			List<? extends IArc> outgoingFirstActionArcs = firstLeftAction.getOutgoingArcsOfType(actionAnalyzerArcType);
			for (IArc iArc : outgoingFirstActionArcs) {
				if (iArc.getAttributeValue(ATTRIBUTE_NAME_TYPE).toString().equalsIgnoreCase(ATTRIBUTE_VALUE_PREDICATE_TO_PARA)) {
					INode currTargetNode = iArc.getTargetNode();
					if ((int) currTargetNode.getAttributeValue(ATTRIBUTE_NAME_POSITION) >= (int) keyphrase.getAttachedNode().get(0)
							.getAttributeValue(ATTRIBUTE_NAME_POSITION)) {
						continue;
					}
					if ((int) currTargetNode.getAttributeValue(ATTRIBUTE_NAME_POSITION) > end) {
						while (currTargetNode.getOutgoingArcsOfType(actionAnalyzerArcType).size() > 0) {
							if (currTargetNode.getOutgoingArcsOfType(actionAnalyzerArcType).size() == 1) {
								currTargetNode = currTargetNode.getOutgoingArcsOfType(actionAnalyzerArcType).get(0).getTargetNode();
							} else if (currTargetNode.getOutgoingArcsOfType(actionAnalyzerArcType).size() > 1) {
								//TODO what iff the assumption (we only have a INSIDE_CHUNK node) doesn't hold?
							}
						}
						end = (int) currTargetNode.getAttributeValue(ATTRIBUTE_NAME_POSITION);
						depNodeEnd = currTargetNode;
					}
				} else {
					continue;
				}
			}
		} else if (firstRightAction != null && secondRightAction != null) {
			//TODO
		} else {
			//TODO
		}
		ConcurrentAction result = new ConcurrentAction();
		result.setKeyphrase(keyphrase);
		result.addDependentPhrase(depNodeBegin);
		INode currNode = depNodeBegin;
		do {
			currNode = currNode.getOutgoingArcsOfType(nextArcType).get(0).getTargetNode();
			result.addDependentPhrase(currNode);
		} while (currNode != depNodeEnd);
		return result;
	}

	private boolean findActionNodes(INode[] actions, boolean left) {
		boolean foundAnd = false;
		while ((left ? !actions[0].getIncomingArcsOfType(nextArcType).isEmpty() : !actions[0].getOutgoingArcsOfType(nextArcType).isEmpty())
				&& actions[2] == null) {
			actions[0] = (left ? actions[0].getIncomingArcsOfType(nextArcType).get(0)
					: actions[0].getOutgoingArcsOfType(nextArcType).get(0)).getSourceNode();
			if (actions[0].getAttributeValue(ATTRIBUTE_NAME_ROLE) != null
					&& actions[0].getAttributeValue(ATTRIBUTE_NAME_ROLE).toString().equalsIgnoreCase(ATTRIBUTE_VALUE_PREDICATE)) {
				if (actions[1] == null) {
					actions[1] = actions[0];
				} else {
					actions[2] = actions[0];
				}
			}
			if (actions[1] != null) {
				if (actions[0].getAttributeValue(ATTRIBUTE_NAME_VALUE).toString().equalsIgnoreCase(WORD_AND)) {
					foundAnd = true;
				}
			}
		}
		return foundAnd;
	}

}
