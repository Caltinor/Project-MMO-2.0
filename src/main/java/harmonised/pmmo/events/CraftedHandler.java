package harmonised.pmmo.events;

import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.XP;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.Map;

public class CraftedHandler
{
    public static void handleCrafted( PlayerEvent.ItemCraftedEvent event )
    {
        PlayerEntity player = event.getPlayer();
        if( !player.world.isRemote() )
        {
            Map<String, Double> award = new HashMap<>();
            award.put( "crafting", 1D );
            ItemStack itemStack = event.getCrafting();
            ResourceLocation resLoc = itemStack.getItem().getRegistryName();

            if( JsonConfig.data.get( "xpValueCrafting" ).containsKey( resLoc.toString() ) )
                XP.addMaps( award, XP.getXpCrafting( resLoc ) );

            XP.multiplyMap( award, itemStack.getCount() );

            for( String skillName : award.keySet() )
            {
                XP.awardXp( player, Skill.getSkill( skillName ), "crafting", award.get( skillName ), false );
            }
        }
    }
}
