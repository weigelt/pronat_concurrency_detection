package edu.kit.ipd.parse.concurrency;

import java.util.List;

import edu.kit.ipd.parse.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.concurrency.data.Utterance;
import edu.kit.ipd.parse.concurrency.filter.KeyphraseFilter;
import edu.kit.ipd.parse.luna.agent.AbstractAgent;
import edu.kit.ipd.parse.luna.graph.ParseGraph;

public class ConcurrencyAgent extends AbstractAgent {

	KeyphraseFilter kf;
	Utterance utterance;

	@Override
	public void init() {
		kf = new KeyphraseFilter();
	}

	@Override
	protected void exec() {

		checkMandatoryPreconditions();
		checkOptionalPreconditionds();

		ParseGraph graphAsParseGraph = (ParseGraph) graph;
		List<Keyphrase> keywords = kf.filter(utterance.giveUtteranceAsNodeList());

	}

	private void checkOptionalPreconditionds() {
		// TODO Auto-generated method stub

	}

	private void checkMandatoryPreconditions() {
		// TODO Auto-generated method stub

	}

}
