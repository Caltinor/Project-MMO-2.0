package harmonised.pmmo.events;

import harmonised.pmmo.skills.Skill;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class AirSupplyDecreaseHandler
{
    public static int returnPmmoAffectedRespiration(LivingEntity entity )
    {
        int respiration = EnchantmentHelper.getRespirationModifier( entity );

        if( entity instanceof PlayerEntity )
        {
            PlayerEntity player = (PlayerEntity) entity;
            int enduranceLevel = Skill.ENDURANCE.getLevel( player );
            int swimmingLevel = Skill.SWIMMING.getLevel( player );
            int respirationBoost = (int) ( ( (double) swimmingLevel + ( (double) enduranceLevel / 2.5D ) ) / 50D );
            respiration += Math.min( respirationBoost, 5 );
        }

        return respiration;
    }
}
