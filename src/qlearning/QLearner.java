package qlearning;

import gui.GUIBoard;
import gui.GUIStarCreature;
import gui.MainWindow;

import java.awt.Point;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Board;
import qlearning.StarAgent.AgentAction;
import utils.RandomGenerator;
import agents.Cannon;
import agents.Creature;
import agents.hashtagCreature.HashtagCreature;

public class QLearner {
	// path finding
	private final double alpha = 0.1;
	private final double gamma = 0.9;
	// Epsilon starts with value 1 at episode 0, and will reach value ~0 at this episode
	private final double epsilonZeroEpisode = 3000;
//	private final double epsilonZeroEpisode = 300;
	public static final int TOTAL_EPOCHS = 10000;

	private StarAgent creature;
	private boolean visual;
	private MainWindow window;
	
	// Q(s,a)= Q(s,a) + alpha * (R(s,a) + gamma * Max(next state, all actions) -
	// Q(s,a))
	// < STATE, < ACTION, Q > >
	private final Map<BoardState, Map<AgentAction, Double>> Q = new HashMap<>();

	public QLearner() {
		visual = true;
		creature = StarAgent.newInstance(new Point(200, 200));
		for (final BoardState state : BoardState.createAll(creature)) {
			final Map<AgentAction, Double> map = new HashMap<AgentAction, Double>();
			for (final AgentAction action : AgentAction.values())
				map.put(action, 0.0);
			Q.put(state, map);
		}
		init();
	}

	public void init() {
		GUIBoard.createNewBoard();
		creature = StarAgent.newInstance();
		GUIBoard.getInstance().register(new GUIStarCreature(creature));
	}

	public static void main(String[] args) {
		long BEGIN = System.currentTimeMillis();

		QLearner learner = new QLearner();

		learner.run();

		long END = System.currentTimeMillis();
		System.out.println("Time: " + (END - BEGIN) / 1000.0 + " sec.");
	}

	void run() {
		if (visual) {
			window = MainWindow.getInstance();
			window.setVisible(true);
		}
		
		final double epsilonM = -1.0 / epsilonZeroEpisode;

		// For each epoch
		for (int i = 0; i < TOTAL_EPOCHS; i++) {
//			if (visual && i % (TOTAL_EPOCHS/100) == 0) {
//				window.setProgress(i);
//				window.repaint();
//			}
			if (visual) {
				window.setProgress(i);
				window.repaint();
				window.clearStrings();
			}
			init();
			final World world = new World(creature);
			BoardState actualState = world.getInitialState();
			GUIBoard.getInstance().clearTemps();
//			boolean allBest = i != 0 && ((i % 100 == 0 && i % 1000 != 0) || i % 2000 == 0);
			boolean allBest = i != 0 && i % (TOTAL_EPOCHS/10) == 0;
			int ticks = 0;
			int episode = 0;
			double episodeEpsilon;
//			boolean gameContinues = true;
			while (!world.endState()) { // goal state // train episodes
				if(window.paused)
					continue;
				episodeEpsilon = epsilonM * episode + 1.0; 
				episode++;
				if (visual && (i % (TOTAL_EPOCHS/10) == 0 || i % (TOTAL_EPOCHS/10) == 1) ) {
					ticks++;
					if (ticks % 10 == 0) {
						ticks = 0;
						GUIBoard.getInstance().clearTemps();
					}
					try {
						window.repaint();
						Thread.sleep(50, 0);
					} catch (InterruptedException e) { }
				}
				
				// Select one among all possible actions for the current state
				final List<AgentAction> actions = world.getActions();
				AgentAction action = null;
				if (RandomGenerator.getNext() > episodeEpsilon || allBest) {
					Double max = - Double.MAX_VALUE;
					for (final AgentAction d : actions) {
						if (Q.get(actualState).get(d) > max) {
							max = Q.get(actualState).get(d);
							action = d;
						}
					}
				} else {
					// Selection strategy is random in this example
					action = actions.get((int) (RandomGenerator.getNext() * actions.size()));
				}
				
//				if(episodeEpsilon < 0.01)
//					gameContinues = false;
//
				final BoardState nextState = world.getNextState(action);
				for (final Cannon c : Board.getInstance().getCannons()) {
					Board.getInstance().shoot(c);
				}
				for (final Creature c : Board.getInstance().getCreatures()) {
					if(c instanceof HashtagCreature)
						c.action();
				}

				// Using this possible action, consider to go to the next state
				double q = Q(actualState, action);
				double maxQ = maxQ(nextState);
				double reward = actualState.getReward(nextState);

				double value = q + alpha * (reward + gamma * maxQ - q);
				setQ(actualState, action, value);

				// Set the next state as the current state
				actualState = nextState;
				updateStrings(actualState);
			}
		}
		showResult();
	}
	
	private void updateStrings(final BoardState currentBoard) {
		window.setTitle(currentBoard.getTitle());
		window.clearStrings();
		for (final BoardState state : Q.keySet()) {
			for (final AgentAction action : Q.get(state).keySet()) {
//				if (Q.get(state).get(action) != 0)
				if (currentBoard.getState() == state.getState())
					window.putString(state.toString() + " - " + action + " : "
							+ new DecimalFormat("#.##").format(Q.get(state).get(action)));
//					window.putString(new Pair<BoardState, AgentAction>(state, action), 
//							new DecimalFormat("#.##").format(Q.get(state).get(action)));
			}
		}
	}	
	
	// Deterministico vs Estocastico
	private void showResult() {
		init();
		final World world = new World(creature);
		BoardState actualState = world.getInitialState();
		int ticks = 0;
		while (!world.endState()) { // goal state
			if(window.paused)
				continue;
			if (visual) {
				if (ticks % 100 == 0) {
					ticks = 0;
					GUIBoard.getInstance().clearTemps();
				} else {
					ticks++;
				}
				try {
					window.repaint();
					Thread.sleep(50, 0);
				} catch (InterruptedException e) {	}
			}
			
			AgentAction action = null;
			Double max = - Double.MAX_VALUE;
			for (final AgentAction d : world.getActions()) {
				if (Q.get(actualState).get(d) > max) {
					max = Q.get(actualState).get(d);
					action = d;
				}
			}

			final BoardState nextState = world.getNextState(action);

			// Set the next state as the current state
			actualState = nextState;
			updateStrings(actualState);
			for (final Cannon c : Board.getInstance().getCannons()) {
				Board.getInstance().shoot(c);
			}
			for (final Creature c : Board.getInstance().getCreatures()) {
				if (c instanceof HashtagCreature)
					c.action();
			}
		}
	}

	private Double maxQ(final BoardState state) {
		Double maxValue = - Double.MAX_VALUE;
		for (final Double value : Q.get(state).values()) {
			if (value > maxValue) {
				maxValue = value;
			}
		}
		return maxValue;
	}

	double Q(final BoardState state, final AgentAction action) {
		return Q.get(state).get(action);			
	}

	void setQ(final BoardState state, final AgentAction action, double value) {
		Q.get(state).put(action, value);
	}
}
