package edu.kit.ipd.parse.concurrency.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.kit.ipd.parse.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.concurrency.data.KeyphraseType;
import edu.kit.ipd.parse.luna.graph.INode;

//TODO: Filter implementation per class: wrapping, separating etc.?
public class KeyphraseFilter {
	//TODO: put into config file
	private static Set<List<String>> wrappingKeyphrases = new HashSet<List<String>>();
	private static Set<List<String>> separatingKeyphrases = new HashSet<List<String>>();
	private static Set<List<String>> openingKeyphrases = new HashSet<List<String>>();
	private static Set<List<String>> endingKeyphrases = new HashSet<List<String>>();

	public KeyphraseFilter() {
		//TODO: implement as config extractor
		//TODO: completeness?
		wrappingKeyphrases.add(new ArrayList<String>(Arrays.asList("at", "once")));
		wrappingKeyphrases.add(new ArrayList<String>(Arrays.asList("simultaneously")));
		wrappingKeyphrases.add(new ArrayList<String>(Arrays.asList("coevally")));
		wrappingKeyphrases.add(new ArrayList<String>(Arrays.asList("concurrently")));
		wrappingKeyphrases.add(new ArrayList<String>(Arrays.asList("synchronistically")));

		//		separatingKeyphrases.add(new ArrayList<String>(Arrays.asList("and", "at", "the", "same", "time")));
		separatingKeyphrases.add(new ArrayList<String>(Arrays.asList("at", "the", "same", "time")));
		separatingKeyphrases.add(new ArrayList<String>(Arrays.asList("while")));
		separatingKeyphrases.add(new ArrayList<String>(Arrays.asList("meanwhile")));
		separatingKeyphrases.add(new ArrayList<String>(Arrays.asList("in", "the", "meantime")));
		//		separatingKeyphrases.add(new ArrayList<String>(Arrays.asList("and", "in", "the", "meantime")));

		openingKeyphrases.add(new ArrayList<String>(Arrays.asList("during")));
		openingKeyphrases.add(new ArrayList<String>(Arrays.asList("and", "while")));
		openingKeyphrases.add(new ArrayList<String>(Arrays.asList("while")));

		endingKeyphrases.add(new ArrayList<String>(Arrays.asList("at", "the", "same", "time")));
		endingKeyphrases.add(new ArrayList<String>(Arrays.asList("in", "the", "meantime")));
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
