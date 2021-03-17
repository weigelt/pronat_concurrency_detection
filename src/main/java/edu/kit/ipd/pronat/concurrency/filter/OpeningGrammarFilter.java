package edu.kit.ipd.pronat.concurrency.filter;

import edu.kit.ipd.pronat.concurrency.data.ConcurrentAction;
import org.apache.commons.lang3.mutable.MutableBoolean;

import edu.kit.ipd.pronat.concurrency.data.Keyphrase;
import edu.kit.ipd.pronat.concurrency.data.KeyphraseType;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.INode;

/**
 * @author Sebastian Weigelt
 */
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
