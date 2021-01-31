package harmonised.pmmo.events;

import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.player.PlayerEntity;

public class EnchantmentHandler
{
    public static void handleItemEnchanted( PlayerEntity player, EnchantmentData enchantmentData )
    {
        System.out.println( "wait, this worked?" );
    }
}
