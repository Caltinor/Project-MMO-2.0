package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.Map;

public class CraftedHandler
{
    public static void handleCrafted( PlayerEvent.ItemCraftedEvent event )
    {
        if( event.getPlayer() instanceof EntityPlayerMP )
        {
            EntityPlayerMP player = (EntityPlayerMP) event.getPlayer();
            double defaultCraftingXp = Config.forgeConfig.defaultCraftingXp.get();
            double durabilityMultiplier = 1;

            ItemStack itemStack = event.getCrafting();
            ResourceLocation resLoc = itemStack.getItem().getRegistryName();
            Map<String, Double> xpValue = XP.getXp( XP.getResLoc( resLoc.toString() ), JType.XP_VALUE_CRAFT );

            Map<String, Double> award = new HashMap<>();
            if( xpValue.size() == 0 )
            {
                if( itemStack.getItem() instanceof BlockItem)
                    award.setTag( "crafting", (double) ((BlockItem) itemStack.getItem()).getBlock().blockHardness );
                else
                    award.setTag( "crafting", defaultCraftingXp );
            }
            else
                XP.addMaps( award, xpValue );

            if( itemStack.isDamageable() )
                durabilityMultiplier = (double) ( itemStack.getMaxDamage() - itemStack.getDamage() ) / (double) itemStack.getMaxDamage();

//            XP.multiplyMap( award, itemStack.getCount() );
            XP.multiplyMap( award, durabilityMultiplier );

            for( Map.Entry<String, Double> entry : award.entrySet() )
            {
                XP.awardXp( player, Skill.getSkill( entry.getKey() ), "crafting", entry.getValue(), false, false, false );
            }
        }
    }
}
