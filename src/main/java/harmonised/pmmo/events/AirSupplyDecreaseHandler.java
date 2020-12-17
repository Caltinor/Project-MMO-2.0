package harmonised.pmmo.events;

import harmonised.pmmo.skills.Skill;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AirSupplyDecreaseHandler
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static int returnPmmoAffectedRespiration(LivingEntity entity )
    {
        int respiration = EnchantmentHelper.getRespirationModifier( entity );

        try
        {
            if( entity instanceof PlayerEntity )
            {
                PlayerEntity player = (PlayerEntity) entity;
                int enduranceLevel = Skill.getLevel( Skill.ENDURANCE.toString(), player );
                int swimmingLevel = Skill.getLevel( Skill.SWIMMING.toString(), player );
                int respirationBoost = (int) ( ( (double) swimmingLevel + ( (double) enduranceLevel / 2.5D ) ) / 50D );
                respiration += Math.min( respirationBoost, 5 );
            }
        }
        catch( Exception e )
        {
            LOGGER.error( e );
        }

        return respiration;
    }
}