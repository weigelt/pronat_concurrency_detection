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

		do {
			newLeftNode = newLeftNode.getIncomingArcsOfType(nextArcType).get(0).getSourceNode();
			if (newLeftNode.getAttributeValue("Role").toString().equalsIgnoreCase("PREDICATE")) {
				if (firstLeftAction == null) {
					firstLeftAction = newLeftNode;
				} else {
					secondLeftAction = newLeftNode;
				}
			}
			if (newLeftNode != null) {
				if (newLeftNode.getAttributeValue("value").toString().equalsIgnoreCase("and")) {
					leftAnd = true;
				}
			}
		} while (!newLeftNode.getIncomingArcsOfType(nextArcType).isEmpty() && secondLeftAction == null);

		do {
			newRightNode = newRightNode.getOutgoingArcsOfType(nextArcType).get(0).getTargetNode();
			if (newRightNode.getAttributeValue("Role").toString().equalsIgnoreCase("PREDICATE")) {
				if (firstRightAction == null) {
					firstRightAction = newRightNode;
				} else {
					secondRightAction = newRightNode;
				}
			}
			if (newRightNode != null) {
				if (newRightNode.getAttributeValue("value").toString().equalsIgnoreCase("and")) {
					rightAnd = true;
				}
			}
		} while (!newRightNode.getOutgoingArcsOfType(nextArcType).isEmpty() && secondRightAction == null);

		INode depNodeBegin = null;
		INode depNodeEnd = null;
		int start = Integer.MAX_VALUE;
		int end = Integer.MIN_VALUE;

		if (firstRightAction != null && secondRightAction != null && rightAnd) {
			//TODO
		} else if (firstLeftAction != null && secondLeftAction != null && leftAnd) {
			List<? extends IArc> outgoingSecondActionArcs = secondLeftAction.getOutgoingArcsOfType(actionAnalyzerArcType);
			for (IArc iArc : outgoingSecondActionArcs) {
				if (iArc.getAttributeValue("type").toString().equalsIgnoreCase(" PREDICATE_TO_PARA")) {
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
				if (iArc.getAttributeValue("type").toString().equalsIgnoreCase(" PREDICATE_TO_PARA")) {
					INode currTargetNode = iArc.getTargetNode();
					if ((int) currTargetNode.getAttributeValue("position") > end) {
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
