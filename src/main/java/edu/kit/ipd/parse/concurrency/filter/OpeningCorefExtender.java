package edu.kit.ipd.parse.concurrency.filter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.concurrency.data.Utterance;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.Pair;

public class OpeningCorefExtender implements ISpecializedCorefExtender {

	@Override
	public void extendBlocks(ConcurrentAction concurrentAction, List<Pair<Integer, Integer>> boundaries, int i, Utterance utterance)
			throws MissingDataException {
		int boundary = Integer.MAX_VALUE;
		if (boundaries.size() > i + 1) {
			boundary = boundaries.get(i + 1).getLeft();
		}
		if (!concurrentAction.getDependentPhrases().isEmpty()) {
			Set<INode> entities = getEntities(concurrentAction.getDependentPhrases());
			int maxPosition = GrammarFilter
					.getPositionOfNode(concurrentAction.getDependentPhrases().get(concurrentAction.getDependentPhrases().size() - 1));
			for (INode entity : entities) {
				int result = getMaxPositionFromCorefChain(entity, maxPosition, boundary, false);
				if (checkIfExtending(result, maxPosition, boundary, false)) {
					maxPosition = result;
				}
			}
			List<INode> nodes = utterance.giveUtteranceAsNodeList();
			for (int j = GrammarFilter
					.getPositionOfNode(concurrentAction.getDependentPhrases().get(concurrentAction.getDependentPhrases().size() - 1))
					+ 1; j <= maxPosition; j++) {
				INode current = nodes.get(j);
				concurrentAction.addDependentPhrase(current);
				if (current.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_ROLE) != null && current
						.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_ROLE).toString().equals(GrammarFilter.ATTRIBUTE_VALUE_PREDICATE)
						&& !GrammarFilter.hasIncomingInsideChunkArcs(current)) {
					concurrentAction.addDependentAction(current);
				}
			}

		}

	}

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
					&& relation.getAttributeValue("name").equals("anaphoraReferent")) {
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
