package agents.hashtagCreature;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.Board;
import model.HeuristicRule;
import agents.Creature;

public class HashtagCreature extends Creature {

	private static List<Set<HeuristicRule>> rules = new ArrayList<>();
	
	//LOADS THE RULZ
	static {
		// LEVEL 1 RULZ
		final Set<HeuristicRule> level1Rulz = new HashSet<>();
		level1Rulz.add(ExtremeDodgeObstaclesRule.createRule());
		rules.add(level1Rulz);
		
		// LEVEL 2 RULZ
		final Set<HeuristicRule> level2Rulz = new HashSet<>();
		level2Rulz.add(BasicDodgeObstaclesRule.createRule());
		rules.add(level2Rulz);

		// LEVEL 3 RULZ
		final Set<HeuristicRule> level3Rulz = new HashSet<>();
		level3Rulz.add(BasicMovementRule.getRule());
		rules.add(level3Rulz);

		// LEVEL 4 RULZ
		final Set<HeuristicRule> level4Rulz = new HashSet<>();
		level4Rulz.add(DefaultRule.getRule());
		rules.add(level4Rulz);
	}
	
	private static final int HEALTH = 4;
	
	private HashtagCreature(final int health, final Rectangle size, final int speed) {
		super(health, size, speed);
	}
	
	public static HashtagCreature newInstance() {
		int x = (int)(Math.random() * 500) + 250;
		int y = (int)(Math.random() * 500) + 250;
		if(x > 450 && x < 550)
			x = 550;
		if(y > 450 && y < 550)
			y = 550;
		return new HashtagCreature(HEALTH, new Rectangle(x, y, 40, 40), 1);
//		return new HashtagCreature(3, new Rectangle(20, 20, 40, 40), 1);
	}

	public static HashtagCreature newInstance(final Point p) {
		return new HashtagCreature(HEALTH, new Rectangle(p.x, p.y, 40, 40), 1);
	}
	
	@Override
	public String toString() {
		return "HashtagCreature: " + super.toString();
	}
	
	public void action() {
		final List<HeuristicRule> validRulz = new ArrayList<>();
//		int i = 0;
		for (final Set<HeuristicRule> level : rules) {
//			i++;
			for (final HeuristicRule rool : level) {
				if (rool.applies(this, Board.getInstance())) {
					validRulz.add(rool);
				}
			}
			// If at least one rule of this level applies
			if (validRulz.size() > 0) {
//				System.out.println("Level " + i + " applies");
				// Choose a random rule from the ones that apply to the situation.
				final HeuristicRule rool = validRulz.get((int) (Math.random() * validRulz.size()));
				rool.getAction().perform(this, Board.getInstance());
				return; // After applying the rule, abort, one rule per turn.
			}
		}
		// No rule applies, do nothing?
		throw new IllegalStateException("NOONE RULZ");
//		return;
	}
}
