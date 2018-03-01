package edu.kit.ipd.parse.concurrency.filter;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.INode;

public class OpeningGrammarFilter implements ISpecializedGrammarFilter {

	@Override
	public ConcurrentAction filter(Keyphrase keyphrase) throws MissingDataException {
		INode[] leftActions = new INode[3];
		leftActions[0] = keyphrase.getAttachedNodes().get(0);
		INode[] rightActions = new INode[3];
		rightActions[0] = keyphrase.getAttachedNodes().get(keyphrase.getAttachedNodes().size() - 1);

		GrammarFilter.findActionNodes(leftActions, true);
		GrammarFilter.findActionNodes(rightActions, false);

		INode firstLeftAction = leftActions[1];
		INode firstRightAction = rightActions[1];

		if (firstLeftAction != null && firstRightAction != null) {
			return OpeningDependentNodesExtractor.extract(keyphrase, firstLeftAction, firstRightAction);
		}
		return null;
	}

}
