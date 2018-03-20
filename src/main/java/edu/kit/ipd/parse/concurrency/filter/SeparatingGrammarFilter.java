package edu.kit.ipd.parse.concurrency.filter;

import org.apache.commons.lang3.mutable.MutableBoolean;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.concurrency.data.KeyphraseType;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.INode;

public class SeparatingGrammarFilter extends AbstractSpecializedGrammarFilter {

	@Override
	protected void detectActions(INode[] leftActions, INode[] rightActions, MutableBoolean leftAnd, MutableBoolean rightAnd,
			Keyphrase keyphrase) {
		findLeftActions(leftActions, keyphrase);
		findRightActions(rightActions, keyphrase);
	}

	@Override
	protected ConcurrentAction interpretResults(INode[] leftActions, INode[] rightActions, MutableBoolean leftAnd, MutableBoolean rightAnd,
			Keyphrase keyphrase) throws MissingDataException {
		INode firstLeftAction = leftActions[1];
		INode firstRightAction = rightActions[1];

		ConcurrentAction result = null;
		if (firstLeftAction != null && firstRightAction != null) {
			result = SeparatingDependentNodesExtractor.extract(keyphrase, firstLeftAction, firstRightAction);
			result.setUsedType(KeyphraseType.SEPARATING);
		}

		return result;
	}

}
