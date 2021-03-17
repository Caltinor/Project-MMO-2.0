package harmonised.pmmo.events;

import harmonised.pmmo.config.JType;
import harmonised.pmmo.gui.WorldXpDrop;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.UUID;

public class FurnaceHandler
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static void handleSmelted( ItemStack input, ItemStack output, World world, BlockPos pos, int type )
    {
        try
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
                    LOGGER.error( "Project MMO handleSmelted WRONG TYPE" );
                    return;
            }

            source += " " + input.getItem().getRegistryName();
            source += " [" + XP.getDimensionResLoc( world ).toString() + "|x" + pos.getX() + "|y" + pos.getY() + "|z" + pos.getZ() + "]";
            UUID uuid = ChunkDataHandler.checkPos( world, pos );

            if( uuid != null )
            {
                double extraChance = XP.getExtraChance( uuid, input.getItem().getRegistryName(), infoType, false ) / 100D;

                int guaranteedDrop = (int) extraChance;
                int extraDrop;

                if( XP.rollChance( extraChance % 1 ) )
                    extraDrop = 1;
                else
                    extraDrop = 0;

                int totalExtraDrops = guaranteedDrop + extraDrop;

                output.grow( totalExtraDrops );

                Map<String, Double> award = XP.multiplyMapAnyDouble( XP.getXp( input.getItem().getRegistryName(), xpType ), 1 + totalExtraDrops );


                for( String awardSkillName : award.keySet() )
                {
                    WorldXpDrop xpDrop = new WorldXpDrop( pos.getX() + 0.5, pos.getY() + 1.523, pos.getZ() + 0.5, 0.4, award.get( awardSkillName ), awardSkillName );
                    xpDrop.setDecaySpeed( 0.25 );
                    WorldRenderHandler.addWorldXpDrop( xpDrop );
                    Skill.addXp( awardSkillName, uuid, award.get( awardSkillName ), source, false, false );
                }
            }
        }
        catch( Exception e )
        {
//            LOGGER.debug( e );
        }
    }
}