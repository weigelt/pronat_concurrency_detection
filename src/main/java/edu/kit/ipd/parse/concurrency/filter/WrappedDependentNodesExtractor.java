package edu.kit.ipd.parse.concurrency.filter;

import java.util.List;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.INode;

public abstract class WrappedDependentNodesExtractor {

	private int start, end;
	ConcurrentAction ca;

	// template methode
	void extract(Keyphrase keyphrase, INode firstAction, INode secondAction) {

		INode begin = determineBegin(secondAction);
		INode end = determineEnd(keyphrase, secondAction);
		ca = constructSequence(keyphrase, begin, end);
	}

	// TODO refactor as template method for other sequence types
	private INode determineBegin(INode secondAction) {
		INode depNodeBegin = secondAction;
		start = (int) secondAction.getAttributeValue(WrappingGrammarFilter.ATTRIBUTE_NAME_POSITION);
		List<? extends IArc> outgoingSecondActionArcs = secondAction.getOutgoingArcsOfType(WrappingGrammarFilter.actionAnalyzerArcType);
		for (IArc iArc : outgoingSecondActionArcs) {
			if (iArc.getAttributeValue(WrappingGrammarFilter.ATTRIBUTE_NAME_TYPE).toString()
					.equalsIgnoreCase(WrappingGrammarFilter.ATTRIBUTE_VALUE_PREDICATE_TO_PARA)) {
				INode currTargetNode = iArc.getTargetNode();
				if ((int) currTargetNode.getAttributeValue(WrappingGrammarFilter.ATTRIBUTE_NAME_POSITION) < start) {
					start = (int) currTargetNode.getAttributeValue(WrappingGrammarFilter.ATTRIBUTE_NAME_POSITION);
					depNodeBegin = currTargetNode;
				}
			} else {
				continue;
			}
		}
		return depNodeBegin;
	}

	//TODO same here
	private INode determineEnd(Keyphrase keyphrase, INode firstAction) {
		INode depNodeEnd = firstAction;
		end = (int) firstAction.getAttributeValue(WrappingGrammarFilter.ATTRIBUTE_NAME_POSITION);
		List<? extends IArc> outgoingFirstActionArcs = firstAction.getOutgoingArcsOfType(WrappingGrammarFilter.actionAnalyzerArcType);
		for (IArc iArc : outgoingFirstActionArcs) {
			if (iArc.getAttributeValue(WrappingGrammarFilter.ATTRIBUTE_NAME_TYPE).toString()
					.equalsIgnoreCase(WrappingGrammarFilter.ATTRIBUTE_VALUE_PREDICATE_TO_PARA)) {
				INode currTargetNode = iArc.getTargetNode();
				if ((int) currTargetNode.getAttributeValue(WrappingGrammarFilter.ATTRIBUTE_NAME_POSITION) >= (int) keyphrase
						.getAttachedNode().get(0).getAttributeValue(WrappingGrammarFilter.ATTRIBUTE_NAME_POSITION)) {
					continue;
				}
				if ((int) currTargetNode.getAttributeValue(WrappingGrammarFilter.ATTRIBUTE_NAME_POSITION) > end) {
					while (currTargetNode.getOutgoingArcsOfType(WrappingGrammarFilter.actionAnalyzerArcType).size() > 0) {
						if (currTargetNode.getOutgoingArcsOfType(WrappingGrammarFilter.actionAnalyzerArcType).size() == 1) {
							currTargetNode = currTargetNode.getOutgoingArcsOfType(WrappingGrammarFilter.actionAnalyzerArcType).get(0)
									.getTargetNode();
						} else if (currTargetNode.getOutgoingArcsOfType(WrappingGrammarFilter.actionAnalyzerArcType).size() > 1) {
							//TODO what iff the assumption (we only have a INSIDE_CHUNK node) doesn't hold?
						}
					}
					end = (int) currTargetNode.getAttributeValue(WrappingGrammarFilter.ATTRIBUTE_NAME_POSITION);
					depNodeEnd = currTargetNode;
				}
			} else {
				continue;
			}
		}
		return depNodeEnd;
	}

	private ConcurrentAction constructSequence(Keyphrase keyphrase, INode start, INode end) {
		// TODO Auto-generated method stub
		return null;
	}

}
