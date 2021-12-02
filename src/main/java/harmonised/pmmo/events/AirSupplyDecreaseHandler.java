package harmonised.pmmo.events;

import harmonised.pmmo.skills.Skill;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.*;

public class AirSupplyDecreaseHandler
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static int returnPmmoAffectedRespiration(LivingEntity entity)
    {
        int respiration = EnchantmentHelper.getRespiration(entity);

        try
        {
            if(entity instanceof Player)
            {
                Player player = (Player) entity;
                int enduranceLevel = Skill.getLevel(Skill.ENDURANCE.toString(), player);
                int swimmingLevel = Skill.getLevel(Skill.SWIMMING.toString(), player);
                int respirationBoost = (int) (((double) swimmingLevel + ((double) enduranceLevel / 2.5D)) / 50D);
                respiration += Math.min(respirationBoost, 5);
            }
        }
        catch(Exception e)
        {
            LOGGER.error(e);
        }

        return respiration;
    }
}