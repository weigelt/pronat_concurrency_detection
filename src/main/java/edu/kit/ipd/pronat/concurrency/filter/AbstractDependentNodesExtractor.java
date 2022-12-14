package edu.kit.ipd.pronat.concurrency.filter;

import java.util.List;

import edu.kit.ipd.pronat.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.INode;

/**
 * @author Sebastian Weigelt
 */
public abstract class AbstractDependentNodesExtractor {

	protected static INode determineBegin(INode startingAction, Keyphrase keyphrase, boolean left) throws MissingDataException {
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

	protected static INode determineEnd(Keyphrase keyphrase, INode endingAction, boolean left) throws MissingDataException {
		INode depNodeEnd = endingAction;
		int end = GrammarFilter.getPositionOfNode(endingAction);
		List<? extends IArc> outgoingFirstActionArcs = endingAction.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType);
		for (IArc iArc : outgoingFirstActionArcs) {
			if (iArc.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_TYPE).toString()
					.equalsIgnoreCase(GrammarFilter.ATTRIBUTE_VALUE_PREDICATE_TO_PARA)) {
				INode currTargetNode = iArc.getTargetNode();
				if (left && GrammarFilter.getPositionOfNode(currTargetNode) >= GrammarFilter
						.getPositionOfNode(keyphrase.getAttachedNodes().get(0))) {
					continue;
				}
				if (GrammarFilter.getPositionOfNode(currTargetNode) > end) {
					while (currTargetNode.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).size() > 0) {
						if (currTargetNode.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).size() == 1) {
							if (left && GrammarFilter
									.getPositionOfNode(currTargetNode.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).get(0)
											.getTargetNode()) >= GrammarFilter.getPositionOfNode(keyphrase.getAttachedNodes().get(0))) {
								break;

							}
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
		return depNodeEnd;
	}
}
