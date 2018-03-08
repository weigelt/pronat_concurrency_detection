package edu.kit.ipd.parse.concurrency;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import edu.kit.ipd.parse.actionRecognizer.ActionRecognizer;
import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.graphBuilder.GraphBuilder;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.INodeType;
import edu.kit.ipd.parse.luna.graph.ParseGraph;
import edu.kit.ipd.parse.luna.pipeline.PipelineStageException;
import edu.kit.ipd.parse.luna.tools.StringToHypothesis;
import edu.kit.ipd.parse.ner.NERTagger;
import edu.kit.ipd.parse.shallownlp.ShallowNLP;
import edu.kit.ipd.parse.srlabeler.SRLabeler;

public class ConcurrencyAgentTest {

	private static ShallowNLP snlp;
	private static GraphBuilder graphBuilder;
	private static ActionRecogMock actionRecog;
	private static SRLabeler srLabeler;
	private static NERTagger ner;
	private static ConcurrencyAgent concAgent;
	private PrePipelineData ppd;
	static ParseGraph pg;
	static INodeType nodeType;

	@BeforeClass
	public static void setUp() {
		graphBuilder = new GraphBuilder();
		graphBuilder.init();
		srLabeler = new SRLabeler();
		srLabeler.init();
		snlp = new ShallowNLP();
		snlp.init();
		ner = new NERTagger();
		ner.init();
		actionRecog = new ActionRecogMock();
		actionRecog.init();
		concAgent = new ConcurrencyAgent();
		concAgent.init();
	}

	@Ignore("not working yet")
	@Test
	public void separatingTest() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "the robot grabs a cup meanwhile Jack goes to the fridge";
		//@formatter:on
		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(input, true));

		IGraph graph = executePreviousStages(ppd);
		concAgent.setGraph(graph);
		concAgent.exec();
		List<ConcurrentAction> actions = concAgent.getConcurrentActions();
		Assert.assertEquals(1, actions.size());
		ConcurrentAction action = actions.get(0);
		String[] expected = new String[] { "meanwhile" };
		int i = 0;
		for (INode node : action.getKeyphrase().getAttachedNodes()) {
			Assert.assertEquals(expected[i], node.getAttributeValue("value").toString());
			i++;
		}
		int[] expectedSpanBefore = new int[] { 0, 4 };
		int[] expectedSpanAfter = new int[] { 6, 9 };
		int lastBefore = 0;
		int index = 0;
		Assert.assertEquals("Before span does not start where expected", expectedSpanBefore[0],
				action.getDependentPhrases().get(0).getAttributeValue("position"));
		for (INode node : action.getDependentPhrases()) {
			int nodePosition = (int) node.getAttributeValue("position");
			boolean isInsideSpan = expectedSpanBefore[0] <= nodePosition && nodePosition <= expectedSpanBefore[1];
			if (lastBefore == 0 && isInsideSpan == false) {
				Assert.assertEquals("Before span does not end where expected", expectedSpanBefore[1],
						action.getDependentPhrases().get(index - 1).getAttributeValue("position"));
				lastBefore = index - 1;
				Assert.assertEquals("After span does start not where expected", expectedSpanAfter[0], nodePosition);
			}
			isInsideSpan = isInsideSpan || expectedSpanAfter[0] <= nodePosition && nodePosition <= expectedSpanAfter[1];
			Assert.assertTrue("Dependent Node at position " + nodePosition + " is not inside expected spans: "
					+ Arrays.toString(expectedSpanBefore) + ", " + Arrays.toString(expectedSpanAfter), isInsideSpan);
			index++;
		}
		Assert.assertEquals("After span does not end where expected", expectedSpanAfter[1],
				action.getDependentPhrases().get(action.getDependentPhrases().size() - 1).getAttributeValue("position"));

	}

	@Test
	public void separatingTestImperative() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "Armar go to the fridge in the meantime look at the recipe";
		//@formatter:on
		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(input, true));

		IGraph graph = executePreviousStages(ppd);
		concAgent.setGraph(graph);
		concAgent.exec();
		List<ConcurrentAction> actions = concAgent.getConcurrentActions();
		Assert.assertEquals(1, actions.size());
		ConcurrentAction action = actions.get(0);
		String[] expected = new String[] { "in", "the", "meantime" };
		int i = 0;
		for (INode node : action.getKeyphrase().getAttachedNodes()) {
			Assert.assertEquals(expected[i], node.getAttributeValue("value").toString());
			i++;
		}
		int[] expectedSpanBefore = new int[] { 0, 4 };
		int[] expectedSpanAfter = new int[] { 8, 11 };
		int lastBefore = 0;
		int index = 0;
		Assert.assertEquals("Before span does not start where expected", expectedSpanBefore[0],
				action.getDependentPhrases().get(0).getAttributeValue("position"));
		for (INode node : action.getDependentPhrases()) {
			int nodePosition = (int) node.getAttributeValue("position");
			boolean isInsideSpan = expectedSpanBefore[0] <= nodePosition && nodePosition <= expectedSpanBefore[1];
			if (lastBefore == 0 && isInsideSpan == false) {
				Assert.assertEquals("Before span does not end where expected", expectedSpanBefore[1],
						action.getDependentPhrases().get(index - 1).getAttributeValue("position"));
				lastBefore = index - 1;
				Assert.assertEquals("After span does start not where expected", expectedSpanAfter[0], nodePosition);
			}
			isInsideSpan = isInsideSpan || expectedSpanAfter[0] <= nodePosition && nodePosition <= expectedSpanAfter[1];
			Assert.assertTrue("Dependent Node at position " + nodePosition + " is not inside expected spans: "
					+ Arrays.toString(expectedSpanBefore) + ", " + Arrays.toString(expectedSpanAfter), isInsideSpan);
			index++;
		}
		Assert.assertEquals("After span does not end where expected", expectedSpanAfter[1],
				action.getDependentPhrases().get(action.getDependentPhrases().size() - 1).getAttributeValue("position"));

	}

	@Test
	public void wrappingTestLeft() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "the dog jumps and the horse looks at once";
		//@formatter:on
		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(input, true));

		IGraph graph = executePreviousStages(ppd);
		concAgent.setGraph(graph);
		concAgent.exec();
		List<ConcurrentAction> actions = concAgent.getConcurrentActions();
		Assert.assertEquals(1, actions.size());
		ConcurrentAction action = actions.get(0);
		String[] expected = new String[] { "at", "once" };
		int i = 0;
		for (INode node : action.getKeyphrase().getAttachedNodes()) {
			Assert.assertEquals(expected[i], node.getAttributeValue("value").toString());
			i++;
		}
		int[] expectedSpan = new int[] { 0, 6 };
		Assert.assertEquals(expectedSpan[0], action.getDependentPhrases().get(0).getAttributeValue("position"));
		Assert.assertEquals(expectedSpan[1],
				action.getDependentPhrases().get(action.getDependentPhrases().size() - 1).getAttributeValue("position"));

	}

	@Test
	public void wrappingTestRight() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "at once the dog jumps and the horse looks to the right";
		//@formatter:on
		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(input, true));

		IGraph graph = executePreviousStages(ppd);
		concAgent.setGraph(graph);
		concAgent.exec();
		List<ConcurrentAction> actions = concAgent.getConcurrentActions();
		Assert.assertEquals(1, actions.size());
		ConcurrentAction action = actions.get(0);
		String[] expected = new String[] { "at", "once" };
		int i = 0;
		for (INode node : action.getKeyphrase().getAttachedNodes()) {
			Assert.assertEquals(expected[i], node.getAttributeValue("value").toString());
			i++;
		}
		int[] expectedSpan = new int[] { 2, 11 };
		Assert.assertEquals(expectedSpan[0], action.getDependentPhrases().get(0).getAttributeValue("position"));
		Assert.assertEquals(expectedSpan[1],
				action.getDependentPhrases().get(action.getDependentPhrases().size() - 1).getAttributeValue("position"));

	}

	@Test
	public void wrappingTestRightNoAnd() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "at once the dog jumps the horse looks to the right";
		//@formatter:on
		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(input, true));

		IGraph graph = executePreviousStages(ppd);
		concAgent.setGraph(graph);
		concAgent.exec();
		List<ConcurrentAction> actions = concAgent.getConcurrentActions();
		Assert.assertEquals(1, actions.size());
		ConcurrentAction action = actions.get(0);
		String[] expected = new String[] { "at", "once" };
		int i = 0;
		for (INode node : action.getKeyphrase().getAttachedNodes()) {
			Assert.assertEquals(expected[i], node.getAttributeValue("value").toString());
			i++;
		}
		int[] expectedSpan = new int[] { 2, 10 };
		Assert.assertEquals(expectedSpan[0], action.getDependentPhrases().get(0).getAttributeValue("position"));
		Assert.assertEquals(expectedSpan[1],
				action.getDependentPhrases().get(action.getDependentPhrases().size() - 1).getAttributeValue("position"));

	}

	@Test
	public void endingTest() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "the dog jumps and the horse looks at the same time";
		//@formatter:on
		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(input, true));

		IGraph graph = executePreviousStages(ppd);
		concAgent.setGraph(graph);
		concAgent.exec();
		List<ConcurrentAction> actions = concAgent.getConcurrentActions();
		Assert.assertEquals(1, actions.size());
		ConcurrentAction action = actions.get(0);
		String[] expected = new String[] { "at", "the", "same", "time" };
		int i = 0;
		for (INode node : action.getKeyphrase().getAttachedNodes()) {
			Assert.assertEquals(expected[i], node.getAttributeValue("value").toString());
			i++;
		}
		int[] expectedSpan = new int[] { 0, 6 };
		Assert.assertEquals(expectedSpan[0], action.getDependentPhrases().get(0).getAttributeValue("position"));
		Assert.assertEquals(expectedSpan[1],
				action.getDependentPhrases().get(action.getDependentPhrases().size() - 1).getAttributeValue("position"));

	}

	@Test
	public void openingTest() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "while the dog jumps the horse looks at the stable";
		//@formatter:on
		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(input, true));

		IGraph graph = executePreviousStages(ppd);
		concAgent.setGraph(graph);
		concAgent.exec();
		List<ConcurrentAction> actions = concAgent.getConcurrentActions();
		Assert.assertEquals(1, actions.size());
		ConcurrentAction action = actions.get(0);
		String[] expected = new String[] { "while" };
		int i = 0;
		for (INode node : action.getKeyphrase().getAttachedNodes()) {
			Assert.assertEquals(expected[i], node.getAttributeValue("value").toString());
			i++;
		}
		int[] expectedSpan = new int[] { 1, 9 };
		Assert.assertEquals(expectedSpan[0], action.getDependentPhrases().get(0).getAttributeValue("position"));
		Assert.assertEquals(expectedSpan[1],
				action.getDependentPhrases().get(action.getDependentPhrases().size() - 1).getAttributeValue("position"));

	}

	@Test
	public void openingTestProgressive() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "while the dog is jumping the horse looks at the stable";
		//@formatter:on
		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(input, true));

		IGraph graph = executePreviousStages(ppd);
		concAgent.setGraph(graph);
		concAgent.exec();
		List<ConcurrentAction> actions = concAgent.getConcurrentActions();
		Assert.assertEquals(1, actions.size());
		ConcurrentAction action = actions.get(0);
		String[] expected = new String[] { "while" };
		int i = 0;
		for (INode node : action.getKeyphrase().getAttachedNodes()) {
			Assert.assertEquals(expected[i], node.getAttributeValue("value").toString());
			i++;
		}
		int[] expectedSpan = new int[] { 1, 10 };
		Assert.assertEquals(expectedSpan[0], action.getDependentPhrases().get(0).getAttributeValue("position"));
		Assert.assertEquals(expectedSpan[1],
				action.getDependentPhrases().get(action.getDependentPhrases().size() - 1).getAttributeValue("position"));

	}

	@Ignore("WIP")
	@Test
	public void beachday0002() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "When the scene starts the Girl moves into the foreground to the center of the scene "
				+ "At the same time the Frog foottaps twice "
				+ "After that the girl waves "
				+ "The Kangaroo claps twice and wags its tail simultaneously "
				+ "All the animals turn to face the Girl "
				+ "The Frog and the Bunny hop at the same time the Kangaroo nods "
				+ "The Kangaroo moves toward the Girl "
				+ "After that the Bunny moves toward the Girl "
				+ "Then the Frog moves toward the Girl "
				+ "The Girl turns to face the LightHouse "
				+ "All the animals turn to face the LightHouse "
				+ "The Girl and all the animals move toward the Beachterrain";
		//@formatter:on
		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(input, true));

		IGraph graph = executePreviousStages(ppd);
		concAgent.setGraph(graph);
		concAgent.exec();
		List<ConcurrentAction> actions = concAgent.getConcurrentActions();
		System.out.println(actions);

	}

	private IGraph executePreviousStages(PrePipelineData ppd) {
		try {
			snlp.exec(ppd);
			srLabeler.exec(ppd);
			ner.exec(ppd);
			graphBuilder.exec(ppd);
		} catch (PipelineStageException e) {
			e.printStackTrace();
		}
		try {
			actionRecog.setGraph(ppd.getGraph());
		} catch (MissingDataException e) {
			e.printStackTrace();
		}
		actionRecog.exec();
		return actionRecog.getGraph();
	}

	private static class ActionRecogMock extends ActionRecognizer {

		public ActionRecogMock() {
			super();
		}

		@Override
		public void exec() {
			super.exec();
		}
	}

}
