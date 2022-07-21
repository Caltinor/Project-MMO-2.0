package harmonised.pmmo.features.anticheese;

import java.util.Map;

/**This class was originally implemented as a successor feature to the anti-cheese
 * behavior in legacy PMMO.  The intent behind the feature was to implement countermeasures
 * to obvious and universal means of abusing the XP system.  Things like being AFK 
 * and getting a steady stream of XP were one aspect of this feature.  
 * 
 * During the development of PMMO 2.0 it was assessed that such a measure was not 
 * needed. It was my assessment that the new features offered enough configuration
 * flexibility that ops would not feel these features added any value.  As such no
 * real attempt was made or planned to complete this class.  
 * 
 * That said, this class is embedded in every single event that awards xp and to
 * delete it would mean removing this single method call from every single event.
 * Is that hard? No, but it means I would have to reference the delete commit to 
 * go back and add this back if ever a reason was identified that justified adding
 * an anti-cheese behavior.  Therefore, it is much easier to leave this bypass 
 * method in place until it is either needed or truly determined to be obsolete.
 * 
 * @author Caltinor
 *
 */
public class CheeseTracker {

	public static Map<String, Long> applyAntiCheese(Map<String, Long> awardIn) {
		return awardIn;
	}
}
