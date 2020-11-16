package harmonised.pmmo.events;

import harmonised.pmmo.skills.Skill;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AirSupplyDecreaseHandler
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static int returnPmmoAffectedRespiration( EntityLiving entity )
    {
        int respiration = EnchantmentHelper.getRespirationModifier( entity );

        try
        {
            if( entity instanceof EntityPlayer )
            {
                EntityPlayer player = (EntityPlayer) entity;
                int enduranceLevel = Skill.ENDURANCE.getLevel( player );
                int swimmingLevel = Skill.SWIMMING.getLevel( player );
                int respirationBoost = (int) ( ( (double) swimmingLevel + ( (double) enduranceLevel / 2.5D ) ) / 50D );
                respiration += Math.min( respirationBoost, 5 );
            }
        }
        catch( Exception e )
        {
            LOGGER.info( e.toString() );
        }

        return respiration;
    }
}