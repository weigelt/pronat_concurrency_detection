package edu.kit.ipd.parse.concurrency.filter;

import java.util.List;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.INode;

public class WrappedDependentNodesExtractor {

	private int start, end;
	private ConcurrentAction ca;

	// template methode
	void extract(Keyphrase keyphrase, INode startingAction, INode endingAction, boolean left) throws MissingDataException {

		INode begin = determineBegin(startingAction, keyphrase, left);
		INode end = determineEnd(keyphrase, endingAction, left);
		ca = constructSequence(keyphrase, begin, end);
	}

	private INode determineBegin(INode startingAction, Keyphrase keyphrase, boolean left) throws MissingDataException {
		INode depNodeBegin = startingAction;
		start = GrammarFilter.getPositionOfNode(depNodeBegin);
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

	private INode determineEnd(Keyphrase keyphrase, INode endingAction, boolean left) {
		INode depNodeEnd = endingAction;
		end = (int) endingAction.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_POSITION);
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

	private ConcurrentAction constructSequence(Keyphrase keyphrase, INode start, INode end) {
		ConcurrentAction result = new ConcurrentAction();
		result.setKeyphrase(keyphrase);
		result.addDependentPhrase(start);
		INode currNode = start;
		do {
			currNode = currNode.getOutgoingArcsOfType(GrammarFilter.nextArcType).get(0).getTargetNode();
			result.addDependentPhrase(currNode);
		} while (currNode != end);
		return result;
	}

	/**
	 * @return the concurrent action
	 */
	public ConcurrentAction getConcurrentAction() {
		return ca;
	}
}
