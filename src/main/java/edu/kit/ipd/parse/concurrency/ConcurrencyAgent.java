package edu.kit.ipd.parse.concurrency;

import java.util.List;
import java.util.Properties;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.concurrency.data.KeyphraseType;
import edu.kit.ipd.parse.concurrency.data.Utterance;
import edu.kit.ipd.parse.concurrency.filter.CorefExtender;
import edu.kit.ipd.parse.concurrency.filter.GrammarFilter;
import edu.kit.ipd.parse.concurrency.filter.KeyphraseFilter;
import edu.kit.ipd.parse.luna.agent.AbstractAgent;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.INodeType;
import edu.kit.ipd.parse.luna.graph.ParseGraph;
import edu.kit.ipd.parse.luna.tools.ConfigManager;

@MetaInfServices(AbstractAgent.class)
public class ConcurrencyAgent extends AbstractAgent {

	private static final String ID = "concurrencyAnalyzer";

	private static final String NODE_TYPE_TOKEN = "token";
	private static final String ARC_TYPE_RELATION = "relation";
	private static final String ARC_TYPE_RELATION_IN_ACTION = "relationInAction";
	private static final String ARC_TYPE_KEY_PHRASE = "concurrentActionKeyPhrase";
	private static final String ARC_TYPE_DEPENDENT_ACTION = "dependentconcurrentAction";
	private static final String NODE_TYPE_CONCURRENT_ACTION = "concurrentAction";

	private IArcType keyPhraseType;
	private IArcType dependentActionType;
	private INodeType concurrentActionType;

	KeyphraseFilter kf;
	GrammarFilter gf;
	CorefExtender ce;
	Utterance utterance;
	List<ConcurrentAction> conActions;
	private boolean corefEnabled = false;

	public ConcurrencyAgent() {
		setId(ID);
	}

	@Override
	public void init() {
		kf = new KeyphraseFilter();
		gf = new GrammarFilter();
		ce = new CorefExtender();
		Properties props = ConfigManager.getConfiguration(getClass());
		corefEnabled = Boolean.parseBoolean(props.getProperty("COREF", "false"));
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
			if (corefEnabled) {
				ce.extendBlocks(conActions, utterance);
			}
		} catch (MissingDataException e) {
			//TODO Logger and return!
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//TODO: add optional filter for coref or eventcoref?
		writeToGraph(conActions);
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
		if (graph.hasArcType(ARC_TYPE_RELATION) && graph.hasArcType(ARC_TYPE_RELATION_IN_ACTION) && graph.hasNodeType(NODE_TYPE_TOKEN)) {
			return true;
		}
		return false;
	}

	private void writeToGraph(List<ConcurrentAction> conActions) {
		INodeType tokenType = graph.getNodeType("token");
		for (ConcurrentAction concurrentAction : conActions) {
			Keyphrase currKPtoWrite = concurrentAction.getKeyphrase();
			INode currConActNode = null;
			for (int i = 0; i < currKPtoWrite.getAttachedNodes().size(); i++) {
				INode currKPNode = currKPtoWrite.getAttachedNodes().get(i);
				if (i == 0) {
					if (!currKPNode.getIncomingArcsOfType(keyPhraseType).isEmpty() && currKPNode.getIncomingArcsOfType(keyPhraseType).get(0)
							.getSourceNode().getType().equals(concurrentActionType)) {
						// we already have a concurrent action node and this is also the source
						// TODO: what if the source node is not the concurrent action node but a token node instead?
						currConActNode = currKPNode.getIncomingArcsOfType(keyPhraseType).get(0).getSourceNode();
					} else {
						// we don't have a concurrent action node. Thus, we create one!
						currConActNode = graph.createNode(concurrentActionType);
						currConActNode.setAttributeValue("keyphrase", currKPtoWrite.getKeyphraseAsString());
						String type = concurrentAction.getUsedType().name();
						currConActNode.setAttributeValue("type", type);
						currConActNode.setAttributeValue("dependentPhrases", concurrentAction.getDependentPhrasesAsString());
						IArc newArc = graph.createArc(currConActNode, currKPNode, keyPhraseType);
						// create according arc
						newArc.setAttributeValue("verfiedByDA", false);
						newArc.setAttributeValue("type", type);
						// create dep action links
						for (int j = 0; j < concurrentAction.getDependentActions().size(); j++) {
							IArc currDepArc = graph.createArc(currConActNode, concurrentAction.getDependentActions().get(j),
									dependentActionType);
							currDepArc.setAttributeValue("verfiedByDA", false);
							currDepArc.setAttributeValue("position", j);
						}
					}
				} else {
					// i>0
					if (!currKPNode.getIncomingArcsOfType(keyPhraseType).isEmpty()) {
						// we already have keyphrase arc
						if (currKPNode.getIncomingArcsOfType(keyPhraseType).get(0).getSourceNode().getType().equals(concurrentActionType)) {
							// but it points to a node o.0
							if ((boolean) currKPNode.getIncomingArcsOfType(keyPhraseType).get(0).getAttributeValue("verfiedByDA")) {
								//TODO: might crash iff "verfiedByDA" is unset
								//but it has been verfied by the da
								cleanUp(currConActNode);
								// set the new curr con act node
								currConActNode = currKPNode.getIncomingArcsOfType(keyPhraseType).get(0).getSourceNode();
								// we have to clean up the previously build con node
								//TODO: what if null?
							}
							// we have to clean up!
							cleanUp(currKPNode.getIncomingArcsOfType(keyPhraseType).get(0).getSourceNode());

						} else if (currKPNode.getIncomingArcsOfType(keyPhraseType).get(0).getSourceNode().getType().equals(tokenType)) {
							//and it points to a token node...
							if (!currKPNode.getIncomingArcsOfType(keyPhraseType).get(0).getSourceNode()
									.equals(currKPtoWrite.getAttachedNodes().get(i - 1))) {
								// but it's not the right one
								// TODO: what now? Throw an exception?
							}
							// it's the right node... everything's fine!
						}
					} else {
						// that's the good case. We simply add a new arc from the first to the next node of the keyphrase
						// create intermediate arc
						createKeyPhraseArc(currKPtoWrite.getAttachedNodes().get(i - 1), currKPNode, convertTypeToString(currKPtoWrite));
					}
				}
			}
		}

	}

	private void cleanUp(INode sourceNode) {
		if (sourceNode == null) {
			return;
		}
		for (IArc depAction : sourceNode.getOutgoingArcsOfType(dependentActionType)) {
			graph.deleteArc(depAction);
		}
		for (IArc keyPhrase : sourceNode.getOutgoingArcsOfType(keyPhraseType)) {
			INode nextNode = keyPhrase.getTargetNode();
			graph.deleteArc(keyPhrase);

			while (nextNode.getIncomingArcsOfType(keyPhraseType).size() < 1) {
				// we stop when the node has more than one incomming keyphrase arcs,
				// i.e. it has one that comes from a conc Action node and one that comes from a token
				// (this one was deleted in the step before)
				IArc nextArc = nextNode.getOutgoingArcsOfType(keyPhraseType).get(0);
				nextNode = nextArc.getTargetNode();
				graph.deleteArc(nextArc);
			}
		}
	}

	private IArcType createKeyphraseArcType() {
		if (!graph.hasArcType(ARC_TYPE_KEY_PHRASE)) {
			IArcType kpat = graph.createArcType(ARC_TYPE_KEY_PHRASE);
			kpat.addAttributeToType("boolean", "verfiedByDA");
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
			daat.addAttributeToType("boolean", "verfiedByDA");
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

	private void createKeyPhraseArc(INode from, INode to, String type) {
		IArc newArc = graph.createArc(from, to, keyPhraseType);
		// create according arc
		newArc.setAttributeValue("verfiedByDA", false);
		newArc.setAttributeValue("type", type);
	}

	private String convertTypeToString(Keyphrase currKPtoWrite) {
		return currKPtoWrite.getSecondaryType().equals(KeyphraseType.UNSET) ? currKPtoWrite.getPrimaryType().name()
				: currKPtoWrite.getPrimaryType() + "/" + currKPtoWrite.getSecondaryType().name();
	}
}
