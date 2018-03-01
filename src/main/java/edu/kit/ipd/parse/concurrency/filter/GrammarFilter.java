package edu.kit.ipd.parse.concurrency.filter;

import java.util.ArrayList;
import java.util.List;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.ParseGraph;

public class GrammarFilter {

	ISpecializedGrammarFilter spg;
	ParseGraph pgStub;
	static final String WORD_AND = "and";
	static final String ATTRIBUTE_VALUE_PREDICATE = "PREDICATE";
	static final String ATTRIBUTE_VALUE_PREDICATE_TO_PARA = "PREDICATE_TO_PARA";
	static final String ATTRIBUTE_NAME_TYPE = "type";
	static final String ATTRIBUTE_NAME_ROLE = "role";
	static final String ATTRIBUTE_NAME_VALUE = "value";
	static final String ATTRIBUTE_NAME_POSITION = "position";
	static final String ARC_TYPE_RELATION = "relation";
	static final String ARC_TYPE_RELATION_IN_ACTION = "relationInAction";
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
			switch (keyphrase.getType()) {
			case WRAPPING:
				spg = new WrappingGrammarFilter();
				break;
			case OPENING:
				spg = new OpeningGrammarFilter();
				break;
			case SEPARATING:
				spg = new SeparatingGrammarFilter();
				break;
			default:
				break;
			}
			conActions.add(spg.filter(keyphrase));
		}
		return conActions;
	}

	static int getPositionOfNode(INode node) throws MissingDataException {
		//		if (!node.getType().containsAttribute("int", ATTRIBUTE_NAME_POSITION)) {
		//			throw new MissingDataException("Node has no position attribute");
		//		}
		return (int) node.getAttributeValue(ATTRIBUTE_NAME_POSITION);
	}
}
