package edu.kit.ipd.parse.concurrency;

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
import edu.kit.ipd.parse.shallownlp.ShallowNLP;
import edu.kit.ipd.parse.srlabeler.SRLabeler;

public class ConcurrencyAgentTest {

	private static ShallowNLP snlp;
	private static GraphBuilder graphBuilder;
	private static ActionRecogMock actionRecog;
	private static SRLabeler srLabeler;
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
		actionRecog = new ActionRecogMock();
		actionRecog.init();
		concAgent = new ConcurrencyAgent();
		concAgent.init();
	}

	@Test
	public void wrappingTest() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "the dog jumps and the horse looks at once ";
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
		for (INode node : action.getKeyphrase().getAttachedNode()) {
			Assert.assertEquals(expected[i], node.getAttributeValue("value").toString());
			i++;
		}
		int[] expectedSpan = new int[] { 0, 6 };
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
