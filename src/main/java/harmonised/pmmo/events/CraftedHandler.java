package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.HashMap;
import java.util.Map;

public class CraftedHandler
{
    public static void handleCrafted( PlayerEvent.ItemCraftedEvent event )
    {
        if( event.player instanceof EntityPlayerMP )
        {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            double defaultCraftingXp = Config.forgeConfig.defaultCraftingXp.get();
            double durabilityMultiplier = 1;

            ItemStack itemStack = event.crafting;
            ResourceLocation resLoc = itemStack.getItem().getRegistryName();
            Map<String, Double> xpValue = XP.getXp( XP.getResLoc( resLoc.toString() ), JType.XP_VALUE_CRAFT );
            Block block = ((ItemBlock) itemStack.getItem()).getBlock();

            Map<String, Double> award = new HashMap<>();
            if( xpValue.size() == 0 )
            {
                if( itemStack.getItem() instanceof ItemBlock)
                    award.put( "crafting", (double) block.getBlockHardness( block.getDefaultState(), player.world, player.getPosition() ) );
                else
                    award.put( "crafting", defaultCraftingXp );
            }
            else
                XP.addMaps( award, xpValue );

            if( itemStack.isItemStackDamageable() )
                durabilityMultiplier = (double) ( itemStack.getMaxDamage() - itemStack.getItemDamage() ) / (double) itemStack.getMaxDamage();

//            XP.multiplyMap( award, itemStack.getCount() );
            XP.multiplyMap( award, durabilityMultiplier );

            for( Map.Entry<String, Double> entry : award.entrySet() )
            {
                XP.awardXp( player, Skill.getSkill( entry.getKey() ), "crafting", entry.getValue(), false, false, false );
            }
        }
    }
}
