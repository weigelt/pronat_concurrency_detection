package edu.kit.ipd.pronat.concurrency.filter;

import edu.kit.ipd.pronat.concurrency.data.ConcurrentAction;
import edu.kit.ipd.pronat.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.luna.data.MissingDataException;

/**
 * @author Sebastian Weigelt
 */
public interface ISpecializedGrammarFilter {

	public ConcurrentAction filter(Keyphrase keyphrase) throws MissingDataException;
}
