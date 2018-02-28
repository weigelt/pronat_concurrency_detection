package edu.kit.ipd.parse.concurrency.filter;

import java.util.List;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.ParseGraph;

public class WrappingGrammarFilter implements ISpecializedGrammarFilter {

	private static final String ARC_TYPE_RELATION_IN_ACTION = "relationInAction";
	private static final String ARC_TYPE_RELATION = "relation";
	private static final String ATTRIBUTE_NAME_POSITION = "position";
	private static final String ATTRIBUTE_NAME_VALUE = "value";
	private static final String ATTRIBUTE_NAME_ROLE = "role";
	private static final String ATTRIBUTE_NAME_TYPE = "type";
	private static final String ATTRIBUTE_VALUE_PREDICATE_TO_PARA = "PREDICATE_TO_PARA";
	private static final String ATTRIBUTE_VALUE_PREDICATE = "PREDICATE";
	private static final String WORD_AND = "and";

	@Override
	public ConcurrentAction filter(Keyphrase keyphrase) {

		IArcType nextArcType = new ParseGraph().createArcType(ARC_TYPE_RELATION);
		//		nextArcType.addAttributeToType("String", "value");
		IArcType actionAnalyzerArcType = new ParseGraph().createArcType(ARC_TYPE_RELATION_IN_ACTION);

		INode firstLeftAction = null;
		INode secondLeftAction = null;
		INode firstRightAction = null;
		INode secondRightAction = null;

		boolean leftAnd = false;
		boolean rightAnd = false;

		INode newLeftNode = keyphrase.getAttachedNode().get(0);
		INode newRightNode = keyphrase.getAttachedNode().get(keyphrase.getAttachedNode().size() - 1);

		while (!newLeftNode.getIncomingArcsOfType(nextArcType).isEmpty() && secondLeftAction == null) {
			newLeftNode = newLeftNode.getIncomingArcsOfType(nextArcType).get(0).getSourceNode();
			if (newLeftNode.getAttributeValue(ATTRIBUTE_NAME_ROLE) != null
					&& newLeftNode.getAttributeValue(ATTRIBUTE_NAME_ROLE).toString().equalsIgnoreCase(ATTRIBUTE_VALUE_PREDICATE)) {
				if (firstLeftAction == null) {
					firstLeftAction = newLeftNode;
				} else {
					secondLeftAction = newLeftNode;
				}
			}
			if (firstLeftAction != null) {
				if (newLeftNode.getAttributeValue(ATTRIBUTE_NAME_VALUE).toString().equalsIgnoreCase(WORD_AND)) {
					leftAnd = true;
				}
			}
		}

		while (!newRightNode.getOutgoingArcsOfType(nextArcType).isEmpty() && secondRightAction == null) {
			newRightNode = newRightNode.getOutgoingArcsOfType(nextArcType).get(0).getTargetNode();
			if (newRightNode.getAttributeValue(ATTRIBUTE_NAME_ROLE) != null
					&& newRightNode.getAttributeValue(ATTRIBUTE_NAME_ROLE).toString().equalsIgnoreCase(ATTRIBUTE_VALUE_PREDICATE)) {
				if (firstRightAction == null) {
					firstRightAction = newRightNode;
				} else {
					secondRightAction = newRightNode;
				}
			}
			if (firstRightAction != null) {
				if (newRightNode.getAttributeValue(ATTRIBUTE_NAME_VALUE).toString().equalsIgnoreCase(WORD_AND)) {
					rightAnd = true;
				}
			}
		}

		INode depNodeBegin = null;
		INode depNodeEnd = null;
		int start = Integer.MAX_VALUE;
		int end = Integer.MIN_VALUE;

		if (firstRightAction != null && secondRightAction != null && rightAnd) {
			//TODO
		} else if (firstLeftAction != null && secondLeftAction != null && leftAnd) {
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
			List<? extends IArc> outgoingFirstActionArcs = firstLeftAction.getOutgoingArcsOfType(actionAnalyzerArcType);
			for (IArc iArc : outgoingFirstActionArcs) {
				if (iArc.getAttributeValue(ATTRIBUTE_NAME_TYPE).toString().equalsIgnoreCase(ATTRIBUTE_VALUE_PREDICATE_TO_PARA)) {
					INode currTargetNode = iArc.getTargetNode();
					//TODO: check if position is right of keyphrase.start
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

}
