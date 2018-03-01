package edu.kit.ipd.parse.concurrency.filter;

import java.util.List;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.INode;

public class OpeningDependentNodesExtractor {

	// template methode
	static ConcurrentAction extract(Keyphrase keyphrase, INode previousAction, INode followingAction) throws MissingDataException {

		INode prevBegin = determineBegin(previousAction, keyphrase, true);
		INode prevEnd = determineEnd(keyphrase, previousAction, true);
		INode followingBegin = determineBegin(followingAction, keyphrase, false);
		INode followingEnd = determineEnd(keyphrase, followingAction, false);
		return constructConcurrentAction(keyphrase, prevBegin, prevEnd, followingBegin, followingEnd);
	}

	private static INode determineBegin(INode startingAction, Keyphrase keyphrase, boolean left) throws MissingDataException {
		INode depNodeBegin = startingAction;
		int start = GrammarFilter.getPositionOfNode(depNodeBegin);
		List<? extends IArc> outgoingFirstActionArcs = startingAction.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType);
		for (IArc iArc : outgoingFirstActionArcs) {
			if (iArc.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_TYPE).toString()
					.equalsIgnoreCase(GrammarFilter.ATTRIBUTE_VALUE_PREDICATE_TO_PARA)) {
				INode currTargetNode = iArc.getTargetNode();
				if (!left && GrammarFilter.getPositionOfNode(currTargetNode) <= GrammarFilter
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
		return depNodeBegin;
	}

	private static INode determineEnd(Keyphrase keyphrase, INode endingAction, boolean left) {
		INode depNodeEnd = endingAction;
		int end = (int) endingAction.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_POSITION);
		List<? extends IArc> outgoingFirstActionArcs = endingAction.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType);
		for (IArc iArc : outgoingFirstActionArcs) {
			if (iArc.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_TYPE).toString()
					.equalsIgnoreCase(GrammarFilter.ATTRIBUTE_VALUE_PREDICATE_TO_PARA)) {
				INode currTargetNode = iArc.getTargetNode();
				if (left && (int) currTargetNode.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_POSITION) >= (int) keyphrase
						.getAttachedNodes().get(0).getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_POSITION)) {
					continue;
				}
				if ((int) currTargetNode.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_POSITION) > end) {
					while (currTargetNode.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).size() > 0) {
						if (currTargetNode.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).size() == 1) {
							currTargetNode = currTargetNode.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).get(0)
									.getTargetNode();
						} else if (currTargetNode.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).size() > 1) {
							//TODO what iff the assumption (we only have a INSIDE_CHUNK node) doesn't hold?
						}
					}
					end = (int) currTargetNode.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_POSITION);
					depNodeEnd = currTargetNode;
				}
			} else {
				continue;
			}
		}
		return depNodeEnd;
	}

	private static ConcurrentAction constructConcurrentAction(Keyphrase keyphrase, INode prevStart, INode prevEnd, INode followingStart,
			INode followingEnd) {
		ConcurrentAction result = new ConcurrentAction();
		result.setKeyphrase(keyphrase);
		result.addDependentPhrase(prevStart);
		INode currNode = prevStart;
		do {
			currNode = currNode.getOutgoingArcsOfType(GrammarFilter.nextArcType).get(0).getTargetNode();
			result.addDependentPhrase(currNode);
		} while (currNode != prevEnd);
		currNode = followingStart;
		do {
			currNode = currNode.getOutgoingArcsOfType(GrammarFilter.nextArcType).get(0).getTargetNode();
			result.addDependentPhrase(currNode);
		} while (currNode != followingEnd);
		return result;
	}
}
