package edu.kit.ipd.pronat.concurrency.filter;

import java.util.ArrayList;
import java.util.List;

import edu.kit.ipd.pronat.concurrency.data.ConcurrentAction;
import edu.kit.ipd.pronat.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.ParseGraph;

/**
 * @author Sebastian Weigelt
 */
public class GrammarFilter {

	ISpecializedGrammarFilter spg;
	ParseGraph pgStub;
	static final String WORD_AND = "and";
	static final String ATTRIBUTE_VALUE_PREDICATE = "PREDICATE";
	static final String ATTRIBUTE_VALUE_PREDICATE_TO_PARA = "PREDICATE_TO_PARA";
	static final String ATTRIBUTE_VALUE_INSIDE_CHUNK = "INSIDE_CHUNK";
	static final String ATTRIBUTE_NAME_TYPE = "type";
	static final String ATTRIBUTE_NAME_ROLE = "role";
	static final String ATTRIBUTE_NAME_VALUE = "value";
	static final String ATTRIBUTE_NAME_POSITION = "position";
	static final String ARC_TYPE_RELATION = "relation";
	static final String ARC_TYPE_RELATION_IN_ACTION = "relationInAction";
	static final String ATTRIBUTE_NAME_INSTRUCTION = "instructionNumber";
	static IArcType actionAnalyzerArcType;
	static IArcType nextArcType;

	public GrammarFilter() {
		pgStub = new ParseGraph();
		nextArcType = pgStub.createArcType(ARC_TYPE_RELATION);
		//		nextArcType.addAttributeToType("String", "value");
		actionAnalyzerArcType = pgStub.createArcType(ARC_TYPE_RELATION_IN_ACTION);
	}

	public List<ConcurrentAction> filter(List<Keyphrase> keyphrases) throws MissingDataException {
		List<ConcurrentAction> conActions = new ArrayList<>();
		for (Keyphrase keyphrase : keyphrases) {
			switch (keyphrase.getPrimaryType()) {
			case WRAPPING:
				spg = new WrappingGrammarFilter();
				break;
			case SEPARATING:
				spg = new SeparatingGrammarFilter();
				break;
			case OPENING:
				spg = new OpeningGrammarFilter();
				break;
			case ENDING:
				spg = new EndingGrammarFilter();
				break;
			default:
				break;
			}
			ConcurrentAction result = spg.filter(keyphrase);
			if (result == null) {
				switch (keyphrase.getSecondaryType()) {
				case WRAPPING:
					spg = new WrappingGrammarFilter();
					break;
				case SEPARATING:
					spg = new SeparatingGrammarFilter();
					break;
				case OPENING:
					spg = new OpeningGrammarFilter();
					break;
				case ENDING:
					spg = new EndingGrammarFilter();
					break;
				default:
					break;
				}
				result = spg.filter(keyphrase);
			}
			if (result != null) {
				conActions.add(result);
			}
		}
		return conActions;
	}

	static int getPositionOfNode(INode node) throws MissingDataException {
		//		if (!node.getType().containsAttribute("int", ATTRIBUTE_NAME_POSITION)) {
		//			throw new MissingDataException("Node has no position attribute");
		//		}
		return (int) node.getAttributeValue(ATTRIBUTE_NAME_POSITION);
	}

	static int getInstructionNumber(INode node) {
		// TODO Auto-generated method stub
		return (int) node.getAttributeValue(ATTRIBUTE_NAME_INSTRUCTION);
	}

	static boolean findActionNodes(INode[] actions, boolean left) {
		boolean foundAnd = false;
		while ((left ? !actions[0].getIncomingArcsOfType(GrammarFilter.nextArcType).isEmpty()
				: !actions[0].getOutgoingArcsOfType(GrammarFilter.nextArcType).isEmpty()) && actions[2] == null) {
			actions[0] = left ? actions[0].getIncomingArcsOfType(GrammarFilter.nextArcType).get(0).getSourceNode()
					: actions[0].getOutgoingArcsOfType(GrammarFilter.nextArcType).get(0).getTargetNode();
			if (actions[0].getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_ROLE) != null
					&& actions[0].getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_ROLE).toString()
							.equalsIgnoreCase(GrammarFilter.ATTRIBUTE_VALUE_PREDICATE)
					&& !hasIncomingInsideChunkArcs(actions[0])) {
				if (actions[1] == null) {
					actions[1] = actions[0];
				} else {
					actions[2] = actions[0];
				}
			}
			if (actions[1] != null) {
				if (actions[0].getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_VALUE).toString().equalsIgnoreCase(GrammarFilter.WORD_AND)) {
					foundAnd = true;
				}
			}
		}
		return foundAnd;
	}

	static boolean hasIncomingInsideChunkArcs(INode start) {
		List<? extends IArc> actionArcs;
		if (!(actionArcs = start.getIncomingArcsOfType(actionAnalyzerArcType)).isEmpty()) {
			if (actionArcs.get(0).getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_TYPE).toString().equals(ATTRIBUTE_VALUE_INSIDE_CHUNK)) {
				return true;
			}
		}
		return false;
	}
}
