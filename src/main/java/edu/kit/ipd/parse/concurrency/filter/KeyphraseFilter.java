package edu.kit.ipd.parse.concurrency.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import edu.kit.ipd.parse.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.concurrency.data.KeyphraseType;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.tools.ConfigManager;

//TODO: Filter implementation per class: wrapping, separating etc.?
public class KeyphraseFilter {
	//TODO: put into config file
	private static Set<List<String>> wrappingKeyphrases = new HashSet<List<String>>();
	private static Set<List<String>> separatingKeyphrases = new HashSet<List<String>>();
	private static Set<List<String>> openingKeyphrases = new HashSet<List<String>>();
	private static Set<List<String>> endingKeyphrases = new HashSet<List<String>>();

	private static final String WRAPPING_PROPERTY = "WRAPPING";
	private static final String OPENING_PROPERTY = "OPENING";
	private static final String SEPARATING_PROPERTY = "SEPARATING";
	private static final String ENDING_PROPERTY = "ENDING";

	public KeyphraseFilter() {

		Properties props = ConfigManager.getConfiguration(getClass());
		extractKeyphrasesFromProperties(props.getProperty(WRAPPING_PROPERTY), wrappingKeyphrases);
		extractKeyphrasesFromProperties(props.getProperty(SEPARATING_PROPERTY), separatingKeyphrases);
		extractKeyphrasesFromProperties(props.getProperty(OPENING_PROPERTY), openingKeyphrases);
		extractKeyphrasesFromProperties(props.getProperty(ENDING_PROPERTY), endingKeyphrases);

	}

	private void extractKeyphrasesFromProperties(String property, Set<List<String>> keyphraseSet) {
		String[] prop = property.trim().split(", ");
		for (String string : prop) {
			keyphraseSet.add(new ArrayList<String>(Arrays.asList(string.trim().split(" "))));
		}
	}

	public List<Keyphrase> filter(List<INode> utteranceAsNodeList) {
		List<Keyphrase> keyphrases = new ArrayList<Keyphrase>();
		for (int i = 0; i < utteranceAsNodeList.size(); i++) {
			i = checkKeyphrases(keyphrases, utteranceAsNodeList, wrappingKeyphrases, KeyphraseType.WRAPPING, i);
			i = checkKeyphrases(keyphrases, utteranceAsNodeList, separatingKeyphrases, KeyphraseType.SEPARATING, i);
			i = checkKeyphrases(keyphrases, utteranceAsNodeList, openingKeyphrases, KeyphraseType.OPENING, i);
			i = checkKeyphrases(keyphrases, utteranceAsNodeList, endingKeyphrases, KeyphraseType.ENDING, i);
		}
		return keyphrases;
	}

	private int checkKeyphrases(List<Keyphrase> keyphrases, List<INode> utteranceAsNodeList, Set<List<String>> keyphraseStringList,
			KeyphraseType keyphraseType, int index) {
		for (List<String> keyphrase : keyphraseStringList) {
			if (index < utteranceAsNodeList.size()) {
				Keyphrase currKP = recursiveKeyphraseFind(utteranceAsNodeList, index, keyphrase, 0, new Keyphrase(keyphraseType));
				if (currKP != null) {
					keyphrases.add(currKP);
					index = index + keyphrase.size();
					break;
				}
			}
		}
		return index;
	}

	private Keyphrase recursiveKeyphraseFind(List<INode> utteranceAsNodeList, int nodeIndex, List<String> keyphrase, int kpIndex,
			Keyphrase result) {
		if (utteranceAsNodeList.size() >= nodeIndex || kpIndex >= keyphrase.size()) {
			INode currNode = utteranceAsNodeList.get(nodeIndex);
			if (currNode.getAttributeValue("value").toString().equalsIgnoreCase(keyphrase.get(kpIndex))) {
				result.addNode(currNode);
				if (kpIndex == keyphrase.size() - 1) {
					return result;
				} else {
					return recursiveKeyphraseFind(utteranceAsNodeList, nodeIndex + 1, keyphrase, kpIndex + 1, result);
				}
			}
		}
		return null;
	}
}
