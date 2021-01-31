package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.SaplingGrowTreeEvent;

import java.util.Map;
import java.util.UUID;

public class GrowHandler
{
    public static void handleSaplingGrow( SaplingGrowTreeEvent event )
    {
        World world = (World) event.getWorld();
        BlockPos pos = event.getPos();
        ResourceLocation dimResLoc = world.dimension.getType().getRegistryName();
        UUID uuid = ChunkDataHandler.checkPos( dimResLoc, pos );
        ChunkDataHandler.delPos( dimResLoc, pos );

        if( uuid != null )
        {
            ResourceLocation resLoc = event.getWorld().getBlockState( pos ).getBlock().getRegistryName();
            Map<String, Double> award = XP.getXp( resLoc, JType.XP_VALUE_GROW );

            if( award.size() > 0 )
                XP.awardXpMap( uuid, award, "Growing " + resLoc + " at " + pos, true, false );
            else
                Skill.addXp( Skill.FARMING.toString(), uuid, Config.forgeConfig.defaultSaplingGrowXp.get(), "Growing " + resLoc + " at " + pos, true, false );
        }
    }

    public static void handleCropGrow( BlockEvent.CropGrowEvent.Post event )
    {
        World world = (World) event.getWorld();
        BlockPos pos = event.getPos();
        ResourceLocation resLoc = event.getWorld().getBlockState( pos ).getBlock().getRegistryName();
        BlockState state = world.getBlockState( pos );
        Block block = state.getBlock();

        UUID uuid = ChunkDataHandler.checkPos( world.dimension.getType().getRegistryName(), pos );

        if( uuid == null && JsonConfig.data.get( JType.BLOCK_SPECIFIC ).containsKey( resLoc.toString() ) && JsonConfig.data.get( JType.BLOCK_SPECIFIC ).get( resLoc.toString() ).containsKey( "growsUpwards" ) )
        {
            BlockPos tempPos = pos;

            while( world.getBlockState( tempPos.down() ).getBlock().equals( block ) && uuid == null )
            {
                tempPos = tempPos.down();
                uuid = ChunkDataHandler.checkPos( world.dimension.getType().getRegistryName(), tempPos );
            }
        }

        if( uuid != null )
        {
            int age = -1;
            int maxAge = -1;

            if( state.has( BlockStateProperties.AGE_0_1 ) )
            {
                age = state.get( BlockStateProperties.AGE_0_1 );
                maxAge = 1;
            }
            else if( state.has( BlockStateProperties.AGE_0_2 ) )
            {
                age = state.get( BlockStateProperties.AGE_0_2 );
                maxAge = 2;
            }
            else if( state.has( BlockStateProperties.AGE_0_3 ) )
            {
                age = state.get( BlockStateProperties.AGE_0_3 );
                maxAge = 3;
            }
            else if( state.has( BlockStateProperties.AGE_0_5 ) )
            {
                age = state.get( BlockStateProperties.AGE_0_5 );
                maxAge = 5;
            }
            else if( state.has( BlockStateProperties.AGE_0_7 ) )
            {
                age = state.get( BlockStateProperties.AGE_0_7 );
                maxAge = 7;
            }
            else if( state.has( BlockStateProperties.AGE_0_15 ) )
            {
                age = state.get( BlockStateProperties.AGE_0_15 );
                maxAge = 15;
            }
            else if( state.has( BlockStateProperties.AGE_0_25 ) )
            {
                age = state.get( BlockStateProperties.AGE_0_25 );
                maxAge = 25;
            }
            else if( state.has( BlockStateProperties.PICKLES_1_4 ) )
            {
                age = state.get( BlockStateProperties.PICKLES_1_4 );
                maxAge = 4;
            }

            if( age != -1 && age == maxAge )
            {
                Map<String, Double> award = XP.getXp( resLoc, JType.XP_VALUE_GROW );

                if( award.size() > 0 )
                    XP.awardXpMap( uuid, award, "Growing " + block.getRegistryName() + " at " + pos, true, false );
                else
                    Skill.addXp( Skill.FARMING.toString(), uuid, Config.forgeConfig.defaultCropGrowXp.get(), "Growing " + block.getRegistryName() + " at " + pos, true, false );
            }
        }
    }
}