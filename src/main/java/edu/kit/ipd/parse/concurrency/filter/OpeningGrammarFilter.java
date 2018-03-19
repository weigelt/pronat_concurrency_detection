package edu.kit.ipd.parse.concurrency.filter;

import org.apache.commons.lang3.mutable.MutableBoolean;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.concurrency.data.KeyphraseType;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.INode;

public class OpeningGrammarFilter extends AbstractSpecializedGrammarFilter {

	@Override
	protected void detectActions(INode[] leftActions, INode[] rightActions, MutableBoolean leftAnd, MutableBoolean rightAnd,
			Keyphrase keyphrase) {
		rightAnd.setValue(findRightActions(rightActions, keyphrase));

	}

	@Override
	protected ConcurrentAction interpretResults(INode[] leftActions, INode[] rightActions, MutableBoolean leftAnd, MutableBoolean rightAnd,
			Keyphrase keyphrase) throws MissingDataException {
		INode firstRightAction = rightActions[1];
		INode secondRightAction = rightActions[2];

		ConcurrentAction result = null;
		if (firstRightAction != null && secondRightAction != null) {
			result = rightWithoutAndCase(firstRightAction, secondRightAction, keyphrase);
			result.setUsedType(KeyphraseType.OPENING);
		} else {
			//TODO: what now?
		}

		return result;
	}

}
