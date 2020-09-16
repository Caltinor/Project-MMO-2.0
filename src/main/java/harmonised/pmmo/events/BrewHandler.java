package harmonised.pmmo.events;

import harmonised.pmmo.config.JType;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.event.brewing.PotionBrewEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.Map;
import java.util.UUID;

public class BrewHandler
{
    public static void handlePotionBrew( NonNullList<ItemStack> brewingItemStacks, World world )
    {
        ItemStack ingredient = brewingItemStacks.get(3);

        if( ingredient.getTag() != null && ingredient.getTag().hasUniqueId( "lastOwner" ) )
        {
            UUID uuid = ingredient.getTag().getUniqueId( "lastOwner" );

            double extraChance = XP.getExtraChance( uuid, brewingItemStacks.get( 3 ).getItem().getRegistryName(), JType.INFO_BREW, false ) / 100D;

            int guaranteedDrop = (int) extraChance;
            int extraDrop;

            ItemStack potion;
            int potionCount = 0;

            for( int i = 0; i < 3; i++ )
            {
                potion = brewingItemStacks.get(i);

                if( !potion.isEmpty() )
                {
                    if( XP.rollChance( extraChance % 1 ) )
                        extraDrop = 1;
                    else
                        extraDrop = 0;
                    potionCount += 1 + guaranteedDrop + extraDrop;
                    potion.grow(guaranteedDrop + extraDrop);
                }
            }

            Map<String, Double> award = XP.multiplyMap( XP.getXp( brewingItemStacks.get( 3 ).getItem().getRegistryName(), JType.XP_VALUE_BREW ), potionCount );

            XP.awardXpMap( uuid, award, "Brewing", true, false );
        }
    }
}
