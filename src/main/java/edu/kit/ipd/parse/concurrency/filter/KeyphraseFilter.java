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

	public KeyphraseFilter() {
		//TODO: implement as config extractor
		wrappingKeyphrases.add(new ArrayList<String>(Arrays.asList("at", "once")));
		wrappingKeyphrases.add(new ArrayList<String>(Arrays.asList("simultaneously")));
		wrappingKeyphrases.add(new ArrayList<String>(Arrays.asList("coevally")));
		wrappingKeyphrases.add(new ArrayList<String>(Arrays.asList("coincidentally")));
		wrappingKeyphrases.add(new ArrayList<String>(Arrays.asList("concurrent")));
		wrappingKeyphrases.add(new ArrayList<String>(Arrays.asList("synchronistically")));
	}

	public List<Keyphrase> filter(List<INode> utteranceAsNodeList) {
		List<Keyphrase> keyphrases = new ArrayList<Keyphrase>();
		for (int i = 0; i < utteranceAsNodeList.size(); i++) {
			//TODO: implement remaining keyphrases
			for (List<String> keyphrase : wrappingKeyphrases) {
				Keyphrase currKP = recursiveKeyphraseFind(utteranceAsNodeList, i, keyphrase, 0, new Keyphrase(KeyphraseType.WRAPPING));
				if (currKP != null) {
					keyphrases.add(currKP);
					i = i + keyphrase.size() - 1;
					break;
				}
			}
		}
		return keyphrases;
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
