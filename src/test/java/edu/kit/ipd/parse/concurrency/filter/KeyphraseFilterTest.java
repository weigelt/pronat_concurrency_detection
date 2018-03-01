package edu.kit.ipd.parse.concurrency.filter;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.parse.concurrency.data.Keyphrase;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.INodeType;
import edu.kit.ipd.parse.luna.graph.ParseGraph;

public class KeyphraseFilterTest {
	static ParseGraph pg;
	static INodeType nodeType;

	@BeforeClass
	public static void setUp() {
		pg = new ParseGraph();
		nodeType = pg.createNodeType("test");
		nodeType.addAttributeToType("String", "value");
	}

	@Test
	public void testFilterKeyphraseAtEnd() {
		String input = "the dog and the hore bark at once";
		List<INode> inputNodeList = new ArrayList<>();
		String[] splitted = input.split(" ");
		for (String string : splitted) {
			INode currNode = pg.createNode(nodeType);
			currNode.setAttributeValue("value", string);
			inputNodeList.add(currNode);
		}
		String[] expected = new String[] { "at", "once" };
		List<Keyphrase> result = new KeyphraseFilter().filter(inputNodeList);
		for (Keyphrase keyphrase : result) {
			int i = 0;
			for (INode node : keyphrase.getAttachedNodes()) {
				Assert.assertEquals(expected[i], node.getAttributeValue("value").toString());
				i++;
			}
		}
	}

	@Test
	public void testFilterKeyphraseMid() {
		String input = "the dog at once and the hore bark";
		List<INode> inputNodeList = new ArrayList<>();
		String[] splitted = input.split(" ");
		for (String string : splitted) {
			INode currNode = pg.createNode(nodeType);
			currNode.setAttributeValue("value", string);
			inputNodeList.add(currNode);
		}
		String[] expected = new String[] { "at", "once" };
		List<Keyphrase> result = new KeyphraseFilter().filter(inputNodeList);
		for (Keyphrase keyphrase : result) {
			int i = 0;
			for (INode node : keyphrase.getAttachedNodes()) {
				Assert.assertEquals(expected[i], node.getAttributeValue("value").toString());
				i++;
			}
		}
	}

	@Test
	public void testFilterKeyphraseAtStart() {
		String input = "at once the dog and the hore bark";
		List<INode> inputNodeList = new ArrayList<>();
		String[] splitted = input.split(" ");
		for (String string : splitted) {
			INode currNode = pg.createNode(nodeType);
			currNode.setAttributeValue("value", string);
			inputNodeList.add(currNode);
		}
		String[] expected = new String[] { "at", "once" };
		List<Keyphrase> result = new KeyphraseFilter().filter(inputNodeList);
		for (Keyphrase keyphrase : result) {
			int i = 0;
			for (INode node : keyphrase.getAttachedNodes()) {
				Assert.assertEquals(expected[i], node.getAttributeValue("value").toString());
				i++;
			}
		}
	}

	@Test
	public void testFilterKeywordAtEnd() {
		String input = "the dog and the hore bark simultaneously";
		List<INode> inputNodeList = new ArrayList<>();
		String[] splitted = input.split(" ");
		for (String string : splitted) {
			INode currNode = pg.createNode(nodeType);
			currNode.setAttributeValue("value", string);
			inputNodeList.add(currNode);
		}
		String[] expected = new String[] { "simultaneously" };
		List<Keyphrase> result = new KeyphraseFilter().filter(inputNodeList);
		for (Keyphrase keyphrase : result) {
			int i = 0;
			for (INode node : keyphrase.getAttachedNodes()) {
				Assert.assertEquals(expected[i], node.getAttributeValue("value").toString());
				i++;
			}
		}
	}
}
