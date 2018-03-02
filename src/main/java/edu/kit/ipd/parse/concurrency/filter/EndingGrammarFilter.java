package edu.kit.ipd.parse.concurrency.filter;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.INode;

public class EndingGrammarFilter implements ISpecializedGrammarFilter {

	@Override
	public ConcurrentAction filter(Keyphrase keyphrase) throws MissingDataException {
		INode[] leftActions = new INode[3];
		leftActions[0] = keyphrase.getAttachedNodes().get(0);

		boolean leftAnd = GrammarFilter.findActionNodes(leftActions, true);

		INode firstLeftAction = leftActions[1];
		INode secondLeftAction = leftActions[2];

		ConcurrentAction result = null;
		if (firstLeftAction != null && secondLeftAction != null && leftAnd) {
			result = DependentNodesExtractor.extract(keyphrase, secondLeftAction, firstLeftAction, true);
		} else {
			//TODO: what now?
		}
		return result;
	}

}
