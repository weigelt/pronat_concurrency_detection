package edu.kit.ipd.pronat.concurrency.filter;

import org.apache.commons.lang3.mutable.MutableBoolean;

import edu.kit.ipd.pronat.concurrency.data.ConcurrentAction;
import edu.kit.ipd.pronat.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.INode;

/**
 * @author Sebastian Weigelt
 */
public abstract class AbstractSpecializedGrammarFilter implements ISpecializedGrammarFilter {

	protected abstract void detectActions(INode[] leftActions, INode[] rightActions, MutableBoolean leftAnd, MutableBoolean rightAnd,
			Keyphrase keyphrase);

	//template method for filter
	@Override
	public ConcurrentAction filter(Keyphrase keyphrase) throws MissingDataException {
		INode[] leftActions = new INode[3];
		INode[] rightActions = new INode[3];

		MutableBoolean leftAnd = new MutableBoolean(false);
		MutableBoolean rightAnd = new MutableBoolean(false);
		detectActions(leftActions, rightActions, leftAnd, rightAnd, keyphrase);

		return interpretResults(leftActions, rightActions, leftAnd, rightAnd, keyphrase);
	}

	protected abstract ConcurrentAction interpretResults(INode[] leftActions, INode[] rightActions, MutableBoolean leftAnd,
			MutableBoolean rightAnd, Keyphrase keyphrase) throws MissingDataException;

	protected boolean findLeftActions(INode[] leftActions, Keyphrase keyphrase) {
		leftActions[0] = keyphrase.getAttachedNodes().get(0);

		return GrammarFilter.findActionNodes(leftActions, true);
	}

	protected boolean findRightActions(INode[] rightActions, Keyphrase keyphrase) {
		rightActions[0] = keyphrase.getAttachedNodes().get(keyphrase.getAttachedNodes().size() - 1);

		return GrammarFilter.findActionNodes(rightActions, false);
	}

	protected ConcurrentAction leftAndCase(INode firstLeftAction, INode secondLeftAction, Keyphrase keyphrase) throws MissingDataException {
		return DependentNodesExtractor.extract(keyphrase, secondLeftAction, firstLeftAction, true);
	}

	protected ConcurrentAction rightAndCase(INode firstRightAction, INode secondRightAction, Keyphrase keyphrase)
			throws MissingDataException {
		return DependentNodesExtractor.extract(keyphrase, firstRightAction, secondRightAction, false);
	}

	protected ConcurrentAction rightWithoutAndCase(INode firstRightAction, INode secondRightAction, Keyphrase keyphrase)
			throws MissingDataException {
		return DependentNodesExtractor.extract(keyphrase, firstRightAction, secondRightAction, false);
	}

}
