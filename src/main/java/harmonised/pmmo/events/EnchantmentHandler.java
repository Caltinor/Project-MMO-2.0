package harmonised.pmmo.events;

import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.entity.player.Player;

public class EnchantmentHandler
{
    public static void handleItemEnchanted( Player player, EnchantmentInstance enchantmentData )
    {
        System.out.println( "wait, this worked?" );
    }
}
