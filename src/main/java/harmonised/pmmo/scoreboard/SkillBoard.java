package harmonised.pmmo.scoreboard;

/*import java.util.List;

import harmonised.pmmo.storage.PmmoSavedData;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;*/

public class SkillBoard {
	/*this might need to be dropped.  It doesn't seem to work as intended.
	 * 
	public static void initScoreboard(List<String> skills) {
		for (int i = 0; i < skills.size(); i++) {
			PmmoSavedData.getServer().getScoreboard().setDisplayObjective(i, getSkillObjective(skills.get(i)));
		}
	}
	
	private static Objective getSkillObjective(String skillObjectiveName) {
		if (!PmmoSavedData.getServer().getScoreboard().hasObjective(skillObjectiveName))
			PmmoSavedData.getServer().getScoreboard().addObjective(skillObjectiveName, 
					ObjectiveCriteria.TRIGGER, 
					new TranslatableComponent("pmmo."+skillObjectiveName.toLowerCase()), 
					ObjectiveCriteria.RenderType.INTEGER);
		return  PmmoSavedData.getServer().getScoreboard().getObjective(skillObjectiveName);
										
	}

	public static void setSkillScore(String playerName, String skill, int level) {
		Scoreboard board = PmmoSavedData.getServer().getScoreboard();
		Score score = board.getOrCreatePlayerScore(playerName, getSkillObjective(skill));
		score.setScore(level);
	}*/
}
