package edu.kit.ipd.parse.concurrency;

import java.util.List;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.concurrency.data.KeyphraseType;
import edu.kit.ipd.parse.concurrency.data.Utterance;
import edu.kit.ipd.parse.concurrency.filter.GrammarFilter;
import edu.kit.ipd.parse.concurrency.filter.KeyphraseFilter;
import edu.kit.ipd.parse.luna.agent.AbstractAgent;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.INodeType;
import edu.kit.ipd.parse.luna.graph.ParseGraph;

@MetaInfServices(AbstractAgent.class)
public class ConcurrencyAgent extends AbstractAgent {

	private static final String ARC_TYPE_RELATION = "relation";
	private static final String ARC_TYPE_RELATION_IN_ACTION = "relationInAction";
	private static final String ARC_TYPE_KEY_PHRASE = "keyPhrase";
	private static final String ARC_TYPE_DEPENDENT_ACTION = "dependentAction";
	private static final String NODE_TYPE_CONCURRENT_ACTION = "concurrentAction";

	private IArcType keyPhraseType;
	private IArcType dependentActionType;
	private INodeType concurrentActionType;

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

		if (!checkMandatoryPreconditions()) {
			return;
		}

		keyPhraseType = createKeyphraseArcType();
		dependentActionType = createDependentActionArcType();
		concurrentActionType = createConcurrentActionNodeType();

		ParseGraph graphAsParseGraph = (ParseGraph) graph;
		utterance = new Utterance(graphAsParseGraph);
		List<Keyphrase> keywords = kf.filter(utterance.giveUtteranceAsNodeList());
		try {
			conActions = gf.filter(keywords);
		} catch (MissingDataException e) {
			//TODO Logger and return!
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//TODO: add optional filter for coref or eventcoref?
		//writeToGraph(conActions);
	}

	/**
	 * Only for testing
	 *
	 * @return
	 */
	public List<ConcurrentAction> getConcurrentActions() {
		return conActions;
	}

	private boolean checkMandatoryPreconditions() {
		if (graph.hasArcType("relation") && graph.hasArcType("relationInAction")) {
			return true;
		}
		return false;
	}

	private void writeToGraph(List<ConcurrentAction> conActions) {
		for (ConcurrentAction concurrentAction : conActions) {
			Keyphrase currKPtoWrite = concurrentAction.getKeyphrase();
			INode currConActNode;
			for (int i = 0; i < currKPtoWrite.getAttachedNodes().size(); i++) {
				INode currKPNode = currKPtoWrite.getAttachedNodes().get(i);
				if (i == 0) {
					if (!currKPNode.getIncomingArcsOfType(keyPhraseType).isEmpty() && currKPNode.getIncomingArcsOfType(keyPhraseType).get(0)
							.getSourceNode().getType().equals(concurrentActionType)) {
						//we already have a concurrent action node and this is also the source
						//TODO: what if the source node is not the concurrent action node but a token node instead?
						currConActNode = currKPNode.getIncomingArcsOfType(keyPhraseType).get(0).getSourceNode();
					} else {
						//we don't have a concurrent action node. Thus, we create one!
						currConActNode = graph.createNode(concurrentActionType);
						currConActNode.setAttributeValue("keyphrase", currKPtoWrite.getKeyphraseAsString());
						String type = currKPtoWrite.getSecondaryType().equals(KeyphraseType.UNSET) ? currKPtoWrite.getPrimaryType().name()
								: currKPtoWrite.getPrimaryType() + "/" + currKPtoWrite.getSecondaryType().name();
						currConActNode.setAttributeValue("type", type);
						currConActNode.setAttributeValue("dependentPhrases", concurrentAction.getDependentPhrasesAsString());
					}
				}
				//go on from here
			}
		}

	}

	private IArcType createKeyphraseArcType() {
		if (!graph.hasArcType(ARC_TYPE_KEY_PHRASE)) {
			IArcType kpat = graph.createArcType(ARC_TYPE_KEY_PHRASE);
			kpat.addAttributeToType("String", "verfiedByDA");
			kpat.addAttributeToType("String", "type");
			return kpat;
		} else {
			return graph.getArcType(ARC_TYPE_KEY_PHRASE);
		}
	}

	private IArcType createDependentActionArcType() {
		if (!graph.hasArcType(ARC_TYPE_DEPENDENT_ACTION)) {
			IArcType daat = graph.createArcType(ARC_TYPE_DEPENDENT_ACTION);
			daat.addAttributeToType("int", "position");
			daat.addAttributeToType("String", "verfiedByDA");
			return daat;
		} else {
			return graph.getArcType(ARC_TYPE_DEPENDENT_ACTION);
		}
	}

	private INodeType createConcurrentActionNodeType() {
		if (!graph.hasNodeType(NODE_TYPE_CONCURRENT_ACTION)) {
			INodeType cant = graph.createNodeType(NODE_TYPE_CONCURRENT_ACTION);
			cant.addAttributeToType("String", "keyphrase");
			cant.addAttributeToType("String", "type");
			cant.addAttributeToType("String", "dependentPhrases");
			return cant;
		} else {
			return graph.getNodeType(NODE_TYPE_CONCURRENT_ACTION);
		}
	}
}
