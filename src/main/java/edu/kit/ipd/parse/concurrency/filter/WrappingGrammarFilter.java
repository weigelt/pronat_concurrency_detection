package edu.kit.ipd.parse.concurrency.filter;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.ParseGraph;

public class WrappingGrammarFilter implements ISpecializedGrammarFilter {

	@Override
	public ConcurrentAction filter(Keyphrase keyphrase) {

		IArcType nextArcType = new ParseGraph().createArcType("relation");
		//		nextArcType.addAttributeToType("String", "value");

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
		} while (!newRightNode.getOutgoingArcsOfType(nextArcType).isEmpty() && secondLeftAction == null);

		//if ()

		return null;
	}

}
