package edu.kit.ipd.parse.concurrency.data;

import java.util.ArrayList;
import java.util.List;

import edu.kit.ipd.parse.luna.graph.INode;

public class Keyphrase {

	KeyphraseType type;
	List<INode> attachedNodes;

	public Keyphrase(KeyphraseType type) {
		this.type = type;
		this.attachedNodes = new ArrayList<>();
	}

	/**
	 * @return the type
	 */
	public KeyphraseType getType() {
		return type;
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
		return attachedNodes.toString();
	}

}
