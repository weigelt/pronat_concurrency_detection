package edu.kit.ipd.parse.concurrency;

import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.parse.actionRecognizer.ActionRecognizer;
import edu.kit.ipd.parse.concurrency.data.ConcurrentAction;
import edu.kit.ipd.parse.contextanalyzer.ContextAnalyzer;
import edu.kit.ipd.parse.corefanalyzer.CorefAnalyzer;
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

public class ConcurrencyCorefTest {

	private static ShallowNLP snlp;
	private static GraphBuilder graphBuilder;
	private static ActionRecogMock actionRecog;
	private static SRLabeler srLabeler;
	private static NERTagger ner;
	private static ConcurrencyAgent concAgent;
	private static ContextAnalyzer context;
	private static CorefAnalyzer coref;
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
		context = new ContextAnalyzer();
		context.init();
		coref = new CorefAnalyzer();
		coref.init();
	}

	@Test
	public void openingCorefTest() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "while Bob jumps Alice looks at the table and runs to it";
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
		int[] expectedSpan = new int[] { 1, 11 };
		Assert.assertEquals(expectedSpan[0], action.getDependentPhrases().get(0).getAttributeValue("position"));
		Assert.assertEquals(expectedSpan[1],
				action.getDependentPhrases().get(action.getDependentPhrases().size() - 1).getAttributeValue("position"));

	}

	private IGraph executePreviousStages(PrePipelineData ppd) {
		IGraph result = null;
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
			actionRecog.exec();
			context.setGraph(actionRecog.getGraph());
			context.exec();
			coref.setGraph(context.getGraph());
			coref.exec();
			result = coref.getGraph();
		} catch (MissingDataException e) {
			e.printStackTrace();
		}

		return result;
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
