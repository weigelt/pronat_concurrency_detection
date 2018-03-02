package edu.kit.ipd.parse.concurrency.data;

import java.util.ArrayList;
import java.util.List;

import edu.kit.ipd.parse.luna.graph.INode;

public class ConcurrentAction {
	private Keyphrase keyphrase;
	private List<INode> dependentPhrases;
	private List<INode> dependentActions;

	public ConcurrentAction() {
		this.dependentPhrases = new ArrayList<>();
	}

	/**
	 * @return the keyphrase
	 */
	public Keyphrase getKeyphrase() {
		return keyphrase;
	}

	/**
	 * @param keyphrase
	 *            the keyphrase to set
	 */
	public void setKeyphrase(Keyphrase keyphrase) {
		this.keyphrase = keyphrase;
	}

	/**
	 * @return the dependentPhrases
	 */
	public List<INode> getDependentPhrases() {
		return dependentPhrases;
	}

	/**
	 * @param dependentPhrases
	 *            the dependentPhrases to set
	 */
	public void setDependentPhrases(List<INode> dependentPhrases) {
		this.dependentPhrases = dependentPhrases;
	}

	/**
	 * @param dependentPhrase
	 *            adds a dependent phrase to the list of dep. phrases
	 */
	public void addDependentPhrase(INode dependentPhrase) {
		dependentPhrases.add(dependentPhrase);
	}

	/**
	 * @return the dependentActions
	 */
	public List<INode> getDependentActions() {
		return dependentActions;
	}

	/**
	 * @param dependentActions
	 *            the dependentActions to set
	 */
	public void setDependentActions(List<INode> dependentActions) {
		this.dependentActions = dependentActions;
	}

	/**
	 * @param dependentAction
	 *            adds a dependent action to the list of dep. phrases
	 */
	public void addDependentAction(INode dependentAction) {
		dependentActions.add(dependentAction);
	}

	@Override
	public String toString() {
		String out = "[keyphrase: " + getKeyphrase().toString() + " dependentNodes: ";
		for (INode iNode : dependentPhrases) {
			out += iNode.getAttributeValue("value") + "(" + iNode.getAttributeValue("position") + "), ";
		}
		out += "]";
		return out;
	}
}
