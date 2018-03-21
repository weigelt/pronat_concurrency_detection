package edu.kit.ipd.parse.concurrency.filter;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.concurrency.data.Utterance;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.Pair;

public abstract class AbstractSpecializedCorefExtender implements ISpecializedCorefExtender {

	protected boolean isLeft;

	public AbstractSpecializedCorefExtender() {
		isLeft = false;
	}

	@Override
	public void extendBlocks(ConcurrentAction concurrentAction, List<Pair<Integer, Integer>> boundaries, int i, Utterance utterance)
			throws MissingDataException {
		if (!concurrentAction.getDependentPhrases().isEmpty()) {
			Set<INode> entities = getEntities(concurrentAction.getDependentPhrases());

			int refPosition = getReferencePosition(concurrentAction);
			int boundary = getBoundary(boundaries, i);
			for (INode entity : entities) {
				int result = getMaxPositionFromCorefChain(entity, refPosition, boundary, isLeft);
				if (checkIfExtending(result, refPosition, boundary, isLeft)) {
					refPosition = result;
				}
			}
			refPosition = determineFinalRefPosition(utterance.giveUtteranceAsNodeList().get(refPosition), boundary, isLeft);
			extendDependentPhrase(concurrentAction, refPosition, getReferencePosition(concurrentAction),
					utterance.giveUtteranceAsNodeList(), isLeft);

		}
	}

	private int determineFinalRefPosition(INode ref, int boundary, boolean left) throws MissingDataException {
		int result = GrammarFilter.getPositionOfNode(ref);
		INode predicateNode = getPredicateForNode(ref);
		if (predicateNode != null) {
			int position = GrammarFilter.getPositionOfNode(predicateNode);
			if ((left && position > boundary && position < result) || (!left && position < boundary && position > result)) {
				result = position;
				if (left) {
					position = determineBegin(predicateNode, boundary, left);
					result = (position < result) ? position : result;
				} else {
					position = determineEnd(predicateNode, boundary, left);
					result = (position > result) ? position : result;
				}
			}
		}
		return result;
	}

	private int determineBegin(INode startingAction, int boundary, boolean left) throws MissingDataException {
		INode depNodeBegin = startingAction;
		int start = GrammarFilter.getPositionOfNode(depNodeBegin);
		List<? extends IArc> outgoingFirstActionArcs = startingAction.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType);
		for (IArc iArc : outgoingFirstActionArcs) {
			if (iArc.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_TYPE).toString()
					.equalsIgnoreCase(GrammarFilter.ATTRIBUTE_VALUE_PREDICATE_TO_PARA)) {
				INode currTargetNode = iArc.getTargetNode();
				if ((!left && GrammarFilter.getPositionOfNode(currTargetNode) >= boundary)
						|| (left && GrammarFilter.getPositionOfNode(currTargetNode) <= boundary)) {
					continue;
				}
				if (GrammarFilter.getPositionOfNode(currTargetNode) < start) {
					start = GrammarFilter.getPositionOfNode(currTargetNode);
				}
			} else {
				continue;
			}
		}
		return start;
	}

	private int determineEnd(INode endingAction, int boundary, boolean left) throws MissingDataException {
		INode depNodeEnd = endingAction;
		int end = GrammarFilter.getPositionOfNode(endingAction);
		List<? extends IArc> outgoingFirstActionArcs = endingAction.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType);
		for (IArc iArc : outgoingFirstActionArcs) {
			if (iArc.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_TYPE).toString()
					.equalsIgnoreCase(GrammarFilter.ATTRIBUTE_VALUE_PREDICATE_TO_PARA)) {
				INode currTargetNode = iArc.getTargetNode();
				if (left && GrammarFilter.getPositionOfNode(currTargetNode) <= boundary) {
					continue;
				}
				if (GrammarFilter.getPositionOfNode(currTargetNode) > end) {
					while (currTargetNode.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).size() > 0) {
						if (currTargetNode.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).size() == 1) {
							if (left && GrammarFilter.getPositionOfNode(currTargetNode
									.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).get(0).getTargetNode()) <= boundary) {
								break;

							}
							currTargetNode = currTargetNode.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).get(0)
									.getTargetNode();
						} else if (currTargetNode.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).size() > 1) {
							//TODO what iff the assumption (we only have a INSIDE_CHUNK node) doesn't hold?
						}
					}
					end = GrammarFilter.getPositionOfNode(currTargetNode);
				}
			} else {
				continue;
			}
		}
		return end;
	}

	private INode getPredicateForNode(INode ref) {
		if (ref.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_ROLE) != null) {
			INode current = ref;
			while (!current.getIncomingArcsOfType(GrammarFilter.actionAnalyzerArcType).isEmpty()
					&& !current.getIncomingArcsOfType(GrammarFilter.actionAnalyzerArcType).get(0)
							.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_TYPE).toString().equals("NEXT_ACTION")) {

				current = current.getIncomingArcsOfType(GrammarFilter.actionAnalyzerArcType).get(0).getSourceNode();
			}
			if (current.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_ROLE) != null && current
					.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_ROLE).toString().equals(GrammarFilter.ATTRIBUTE_VALUE_PREDICATE)) {
				return current;
			}
		}
		return null;
	}

	private void extendDependentPhrase(ConcurrentAction concurrentAction, int refPosition, int referencePosition, List<INode> nodes,
			boolean left) {
		if (left) {
			for (int j = referencePosition - 1; j >= refPosition; j--) {
				extend(concurrentAction, nodes, j);
			}
		} else {
			for (int j = referencePosition + 1; j <= refPosition; j++) {
				extend(concurrentAction, nodes, j);
			}
		}
		Collections.sort(concurrentAction.getDependentPhrases(), new Comparator<INode>() {

			@Override
			public int compare(INode o1, INode o2) {
				try {
					return Integer.compare(GrammarFilter.getPositionOfNode(o1), GrammarFilter.getPositionOfNode(o2));
				} catch (MissingDataException e) {
					return 0;
				}
			}
		});

	}

	private void extend(ConcurrentAction concurrentAction, List<INode> nodes, int j) {
		INode current = nodes.get(j);
		concurrentAction.addDependentPhrase(current);
		if (current.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_ROLE) != null
				&& current.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_ROLE).toString().equals(GrammarFilter.ATTRIBUTE_VALUE_PREDICATE)
				&& !GrammarFilter.hasIncomingInsideChunkArcs(current)) {
			concurrentAction.addDependentAction(current);
		}
	}

	protected abstract int getReferencePosition(ConcurrentAction concurrentAction) throws MissingDataException;

	protected abstract int getBoundary(List<Pair<Integer, Integer>> boundaries, int i);

	private int getMaxPositionFromCorefChain(INode entity, int refPosition, int boundary, boolean left) throws MissingDataException {
		int result = refPosition;
		Set<IArc> arcs;
		if (left) {
			arcs = filterReferentRelations(entity.getOutgoingArcsOfType(CorefExtender.contextRelationArcType));
		} else {
			arcs = filterReferentRelations(entity.getIncomingArcsOfType(CorefExtender.contextRelationArcType));
		}
		if (arcs.isEmpty()) {
			return result;
		} else {
			for (IArc iArc : arcs) {
				INode positionNode;
				if (left) {
					positionNode = iArc.getTargetNode();
				} else {
					positionNode = iArc.getSourceNode();
				}
				int position = getPositionOfReferenceNode(positionNode, left);
				if (checkIfExtending(position, result, boundary, left)) {
					result = position;
					int maxChain = getMaxPositionFromCorefChain(positionNode, result, boundary, left);
					if (checkIfExtending(maxChain, result, boundary, left)) {
						result = maxChain;
					}

				}
			}
		}
		return result;
	}

	private boolean checkIfExtending(int position, int previous, int boundary, boolean left) {
		if (left) {
			if (position < previous && position > boundary) {
				return true;
			}
		} else {
			if (position > previous && position < boundary) {
				return true;
			}
		}
		return false;
	}

	private int getPositionOfReferenceNode(INode entityNode, boolean left) throws MissingDataException {
		if (left) {
			return GrammarFilter
					.getPositionOfNode(entityNode.getOutgoingArcsOfType(CorefExtender.entityReferenceArcType).get(0).getTargetNode());
		} else {
			INode current = entityNode;
			while (!current.getOutgoingArcsOfType(CorefExtender.entityReferenceArcType).isEmpty()) {
				current = current.getOutgoingArcsOfType(CorefExtender.entityReferenceArcType).get(0).getTargetNode();
			}
			return GrammarFilter.getPositionOfNode(current);
		}
	}

	private Set<IArc> filterReferentRelations(List<? extends IArc> relations) {
		Set<IArc> referentRelations = new HashSet<>();
		for (IArc relation : relations) {
			if (relation.getAttributeValue(CorefExtender.RELATION_TYPE_NAME).equals(CorefExtender.REFERENT_RELATION_TYPE)
					&& relation.getAttributeValue(CorefExtender.REFERENT_RELATION_ROLE_NAME).equals(CorefExtender.ANAPHORA_NAME_VALUE)) {
				if (isMostLikelyReferent(relation, relation.getSourceNode())) {
					referentRelations.add(relation);
				}

			}
		}
		return referentRelations;
	}

	private boolean isMostLikelyReferent(IArc relation, INode sourceNode) {
		double confidence = (double) relation.getAttributeValue(CorefExtender.CONFIDENCE_NAME);
		for (IArc rel : sourceNode.getOutgoingArcsOfType(CorefExtender.contextRelationArcType)) {
			if (relation.getAttributeValue(CorefExtender.RELATION_TYPE_NAME).equals(CorefExtender.REFERENT_RELATION_TYPE)) {
				if (confidence < (double) rel.getAttributeValue(CorefExtender.CONFIDENCE_NAME)) {
					return false;
				}
			}

		}
		return true;
	}

	private Set<INode> getEntities(List<INode> dependentPhrases) {
		Set<INode> entities = new HashSet<>();
		for (INode node : dependentPhrases) {
			List<? extends IArc> incoming = node.getIncomingArcsOfType(CorefExtender.entityReferenceArcType);
			if (!incoming.isEmpty() && incoming.get(0).getSourceNode().getType().equals(CorefExtender.entityNodeType)) {
				entities.add(incoming.get(0).getSourceNode());
			}
		}
		return entities;
	}

}
