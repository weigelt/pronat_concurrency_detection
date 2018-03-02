package edu.kit.ipd.parse.concurrency.data;

import java.util.ArrayList;
import java.util.List;

import edu.kit.ipd.parse.luna.graph.INode;

public class Keyphrase {

	KeyphraseType priType = KeyphraseType.UNSET, secType = KeyphraseType.UNSET;
	List<INode> attachedNodes;

	public Keyphrase(KeyphraseType priType) {
		this.priType = priType;
		this.attachedNodes = new ArrayList<>();
	}

	public Keyphrase(KeyphraseType priType, KeyphraseType secType) {
		this.priType = priType;
		this.secType = secType;
		this.attachedNodes = new ArrayList<>();
	}

	/**
	 * @return the primary type
	 */
	public KeyphraseType getPrimaryType() {
		return priType;
	}

	/**
	 * @return the secondary type
	 */
	public KeyphraseType getSecondaryType() {
		return secType;
	}

	/**
	 * @return the attachedNode
	 */
	public List<INode> getAttachedNodes() {
		return attachedNodes;
	}

	public void addNode(INode newNode) {
		attachedNodes.add(newNode);
	}

	@Override
	public String toString() {
		String out = "[" + priType.name() + "(" + secType.name() + "): ";
		for (INode iNode : attachedNodes) {
			out += iNode.getAttributeValue("value") + "(" + iNode.getAttributeValue("position") + "), ";
		}
		out += "]";
		return out;
	}

}
