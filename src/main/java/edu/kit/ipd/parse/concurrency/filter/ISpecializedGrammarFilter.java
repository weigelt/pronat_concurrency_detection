package edu.kit.ipd.parse.concurrency.filter;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.luna.data.MissingDataException;

public interface ISpecializedGrammarFilter {

	public ConcurrentAction filter(Keyphrase keyphrase) throws MissingDataException;
}
