package harmonised.pmmo.events;

import harmonised.pmmo.config.JType;
import harmonised.pmmo.util.XP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.UUID;

public class BrewHandler
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static void handlePotionBrew( NonNullList<ItemStack> brewingItemStacks, World world )
    {
        try
        {
            ItemStack ingredient = brewingItemStacks.get(3);

            if( ingredient.getTag() != null && ingredient.getTag().contains( "lastOwner" ) )
            {
                UUID uuid = UUID.fromString( ingredient.getTag().getString( "lastOwner" ) );

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
        catch( Exception e )
        {
            LOGGER.error( e );
        }
    }
}