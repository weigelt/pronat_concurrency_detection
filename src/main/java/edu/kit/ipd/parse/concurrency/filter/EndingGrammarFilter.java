package edu.kit.ipd.parse.concurrency.filter;

import org.apache.commons.lang3.mutable.MutableBoolean;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.concurrency.data.KeyphraseType;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.INode;

public class EndingGrammarFilter extends AbstractSpecializedGrammarFilter {

	@Override
	protected void detectActions(INode[] leftActions, INode[] rightActions, MutableBoolean leftAnd, MutableBoolean rightAnd,
			Keyphrase keyphrase) {
		leftAnd.setValue(findLeftActions(leftActions, keyphrase));

	}

	@Override
	protected ConcurrentAction interpretResults(INode[] leftActions, INode[] rightActions, MutableBoolean leftAnd, MutableBoolean rightAnd,
			Keyphrase keyphrase) throws MissingDataException {
		INode firstLeftAction = leftActions[1];
		INode secondLeftAction = leftActions[2];

		ConcurrentAction result = null;
		if (firstLeftAction != null && secondLeftAction != null && leftAnd.getValue()) {
			result = leftAndCase(firstLeftAction, secondLeftAction, keyphrase);
			result.setUsedType(KeyphraseType.ENDING);
		} else {
			//TODO: what now?
		}
		return result;
	}

}
