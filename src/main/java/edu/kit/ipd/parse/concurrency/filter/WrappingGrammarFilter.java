package edu.kit.ipd.parse.concurrency.filter;

import java.util.List;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.IArc;
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

		INode depNodeBegin = null;
		INode depNodeEnd = null;
		int start;
		int end;

		if (firstRightAction != null && secondRightAction != null && rightAnd) {
			depNodeBegin = firstRightAction;
			start = GrammarFilter.getPositionOfNode(depNodeBegin);
			List<? extends IArc> outgoingFirstActionArcs = firstRightAction.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType);
			for (IArc iArc : outgoingFirstActionArcs) {
				if (iArc.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_TYPE).toString()
						.equalsIgnoreCase(GrammarFilter.ATTRIBUTE_VALUE_PREDICATE_TO_PARA)) {
					INode currTargetNode = iArc.getTargetNode();
					if (GrammarFilter.getPositionOfNode(currTargetNode) <= GrammarFilter
							.getPositionOfNode(keyphrase.getAttachedNodes().get(keyphrase.getAttachedNodes().size() - 1))) {
						continue;
					}
					if (GrammarFilter.getPositionOfNode(currTargetNode) < start) {
						start = GrammarFilter.getPositionOfNode(currTargetNode);
						depNodeBegin = currTargetNode;
					}
				} else {
					continue;
				}
			}

			depNodeEnd = secondRightAction;
			end = GrammarFilter.getPositionOfNode(depNodeEnd);
			List<? extends IArc> outgoingSecondActionArcs = secondRightAction.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType);
			for (IArc iArc : outgoingSecondActionArcs) {
				if (iArc.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_TYPE).toString()
						.equalsIgnoreCase(GrammarFilter.ATTRIBUTE_VALUE_PREDICATE_TO_PARA)) {
					INode currTargetNode = iArc.getTargetNode();
					if (GrammarFilter.getPositionOfNode(currTargetNode) > end) {
						while (currTargetNode.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).size() > 0) {
							if (currTargetNode.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).size() == 1) {
								currTargetNode = currTargetNode.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).get(0)
										.getTargetNode();
							} else if (currTargetNode.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).size() > 1) {
								try {
									throw new Exception("mööp");
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						end = GrammarFilter.getPositionOfNode(currTargetNode);
						depNodeEnd = currTargetNode;
					}
				} else {
					continue;
				}
			}

		} else if (firstLeftAction != null && secondLeftAction != null && leftAnd) {
			depNodeBegin = secondLeftAction;
			start = GrammarFilter.getPositionOfNode(secondLeftAction);
			List<? extends IArc> outgoingSecondActionArcs = secondLeftAction.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType);
			for (IArc iArc : outgoingSecondActionArcs) {
				if (iArc.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_TYPE).toString()
						.equalsIgnoreCase(GrammarFilter.ATTRIBUTE_VALUE_PREDICATE_TO_PARA)) {
					INode currTargetNode = iArc.getTargetNode();
					if (GrammarFilter.getPositionOfNode(currTargetNode) < start) {
						start = GrammarFilter.getPositionOfNode(currTargetNode);
						depNodeBegin = currTargetNode;
					}
				} else {
					continue;
				}
			}
			//TODO: incorrect if action is composed of multiple words
			depNodeEnd = firstLeftAction;
			end = GrammarFilter.getPositionOfNode(firstLeftAction);
			List<? extends IArc> outgoingFirstActionArcs = firstLeftAction.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType);
			for (IArc iArc : outgoingFirstActionArcs) {
				if (iArc.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_TYPE).toString()
						.equalsIgnoreCase(GrammarFilter.ATTRIBUTE_VALUE_PREDICATE_TO_PARA)) {
					INode currTargetNode = iArc.getTargetNode();
					if (GrammarFilter.getPositionOfNode(currTargetNode) >= GrammarFilter
							.getPositionOfNode(keyphrase.getAttachedNodes().get(0))) {
						continue;
					}
					if (GrammarFilter.getPositionOfNode(currTargetNode) > end) {
						while (currTargetNode.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).size() > 0) {
							if (currTargetNode.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).size() == 1) {
								currTargetNode = currTargetNode.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).get(0)
										.getTargetNode();
							} else if (currTargetNode.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).size() > 1) {
								//TODO what iff the assumption (we only have a INSIDE_CHUNK node) doesn't hold?
							}
						}
						end = GrammarFilter.getPositionOfNode(currTargetNode);
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
			currNode = currNode.getOutgoingArcsOfType(GrammarFilter.nextArcType).get(0).getTargetNode();
			result.addDependentPhrase(currNode);
		} while (currNode != depNodeEnd);
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
