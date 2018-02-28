package edu.kit.ipd.parse.concurrency;

import java.util.List;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.concurrency.data.Utterance;
import edu.kit.ipd.parse.concurrency.filter.GrammarFilter;
import edu.kit.ipd.parse.concurrency.filter.KeyphraseFilter;
import edu.kit.ipd.parse.luna.agent.AbstractAgent;
import edu.kit.ipd.parse.luna.graph.ParseGraph;

public class ConcurrencyAgent extends AbstractAgent {

	KeyphraseFilter kf;
	GrammarFilter gf;
	Utterance utterance;
	List<ConcurrentAction> conActions;

	@Override
	public void init() {
		kf = new KeyphraseFilter();
		gf = new GrammarFilter();
	}

	@Override
	protected void exec() {

		checkMandatoryPreconditions();
		checkOptionalPreconditionds();

		ParseGraph graphAsParseGraph = (ParseGraph) graph;
		utterance = new Utterance(graphAsParseGraph);
		List<Keyphrase> keywords = kf.filter(utterance.giveUtteranceAsNodeList());
		conActions = gf.filter(keywords);
	}

	/**
	 * Only for testing
	 * 
	 * @return
	 */
	public List<ConcurrentAction> getConcurrentActions() {
		return conActions;
	}

	private void checkOptionalPreconditionds() {
		// TODO Auto-generated method stub

	}

	private void checkMandatoryPreconditions() {
		// TODO Auto-generated method stub

	}

}
