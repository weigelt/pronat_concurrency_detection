package edu.kit.ipd.pronat.concurrency.filter;

import java.util.List;

import edu.kit.ipd.pronat.concurrency.data.ConcurrentAction;
import edu.kit.ipd.pronat.concurrency.data.Utterance;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.Pair;

/**
 * @author Sebastian Weigelt
 */
public interface ISpecializedCorefExtender {

	public void extendBlocks(ConcurrentAction concurrentAction, List<Pair<Integer, Integer>> boundaries, int i, Utterance utterance)
			throws MissingDataException;

}
