package edu.kit.ipd.parse.concurrency.filter;

import java.util.List;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.ParseGraph;

public class WrappingGrammarFilter implements ISpecializedGrammarFilter {

	@Override
	public ConcurrentAction filter(Keyphrase keyphrase) {

		IArcType nextArcType = new ParseGraph().createArcType("relation");
		//		nextArcType.addAttributeToType("String", "value");
		IArcType actionAnalyzerArcType = new ParseGraph().createArcType("relationInAction");

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
			if (newLeftNode.getAttributeValue("role") != null
					&& newLeftNode.getAttributeValue("role").toString().equalsIgnoreCase("PREDICATE")) {
				if (firstLeftAction == null) {
					firstLeftAction = newLeftNode;
				} else {
					secondLeftAction = newLeftNode;
				}
			}
			if (firstLeftAction != null) {
				if (newLeftNode.getAttributeValue("value").toString().equalsIgnoreCase("and")) {
					leftAnd = true;
				}
			}
		}

		while (!newRightNode.getOutgoingArcsOfType(nextArcType).isEmpty() && secondRightAction == null) {
			newRightNode = newRightNode.getOutgoingArcsOfType(nextArcType).get(0).getTargetNode();
			if (newRightNode.getAttributeValue("role") != null
					&& newRightNode.getAttributeValue("role").toString().equalsIgnoreCase("PREDICATE")) {
				if (firstRightAction == null) {
					firstRightAction = newRightNode;
				} else {
					secondRightAction = newRightNode;
				}
			}
			if (firstRightAction != null) {
				if (newRightNode.getAttributeValue("value").toString().equalsIgnoreCase("and")) {
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
				if (iArc.getAttributeValue("type").toString().equalsIgnoreCase("PREDICATE_TO_PARA")) {
					INode currTargetNode = iArc.getTargetNode();
					if ((int) currTargetNode.getAttributeValue("position") < start) {
						start = (int) currTargetNode.getAttributeValue("position");
						depNodeBegin = currTargetNode;
					}
				} else {
					continue;
				}
			}
			List<? extends IArc> outgoingFirstActionArcs = firstLeftAction.getOutgoingArcsOfType(actionAnalyzerArcType);
			for (IArc iArc : outgoingFirstActionArcs) {
				if (iArc.getAttributeValue("type").toString().equalsIgnoreCase("PREDICATE_TO_PARA")) {
					INode currTargetNode = iArc.getTargetNode();
					if ((int) currTargetNode.getAttributeValue("position") > end) {
						while (currTargetNode.getOutgoingArcsOfType(actionAnalyzerArcType).size() > 1) {
							if (currTargetNode.getOutgoingArcsOfType(actionAnalyzerArcType).size() == 1) {
								currTargetNode = currTargetNode.getOutgoingArcsOfType(actionAnalyzerArcType).get(0).getTargetNode();
							} else if (currTargetNode.getOutgoingArcsOfType(actionAnalyzerArcType).size() == 1) {
								//TODO what iff the assumption (we only have a INSIDE_CHUNK node) doesn't hold?
							}
						}
						end = (int) currTargetNode.getAttributeValue("position");
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
		} while (currNode == depNodeEnd);
		return result;
	}

}
