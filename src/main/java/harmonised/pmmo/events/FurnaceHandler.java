package harmonised.pmmo.events;

import harmonised.pmmo.config.JType;
import harmonised.pmmo.util.LogHandler;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import java.util.Map;
import java.util.UUID;

public class FurnaceHandler
{
    public static void handleSmelted( ItemStack input, ItemStack output, World world, int type )
    {
        JType infoType, xpType;
        String source;

        switch( type )
        {
            case 0:     //SMELT
                infoType = JType.INFO_SMELT;
                xpType = JType.XP_VALUE_SMELT;
                source = "Smelting";
                break;

            case 1:     //COOK
                infoType = JType.INFO_COOK;
                xpType = JType.XP_VALUE_COOK;
                source = "Cooking";
                break;

            default:    //INVALID
                LogHandler.LOGGER.error( "Project MMO handleSmelted WRONG TYPE" );
                return;
        }

        if( input.getTag() != null && input.getTag().hasUniqueId( "lastOwner" ) )
        {
            UUID uuid = input.getTag().getUniqueId( "lastOwner" );
            PlayerEntity player = world.getServer().getPlayerList().getPlayerByUUID( uuid );
            if( player != null )
            {
                double extraChance = XP.getExtraChance( player, input.getItem().getRegistryName(), infoType ) / 100D;

                int guaranteedDrop = (int) extraChance;
                int extraDrop;

                if( XP.rollChance( extraChance % 1 ) )
                    extraDrop = 1;
                else
                    extraDrop = 0;

                int totalExtraDrops = guaranteedDrop + extraDrop;

                output.grow( totalExtraDrops );

                Map<String, Double> award = XP.multiplyMap( XP.getXp( input.getItem().getRegistryName(), xpType ), 1 + totalExtraDrops );

                XP.awardXpMapDouble( player, award, source, true, false );
            }
        }
//        if( output.getTag() != null && output.getTag().hasUniqueId( "lastOwner" ) )
//        {
//            if( output.getTag().size() == 1 )
//                output.setTag( null );
//            else
//                output.getTag().remove( "lastOwner" );
//        }
    }
}
