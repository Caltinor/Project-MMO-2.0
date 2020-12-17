package harmonised.pmmo.events;

import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
import net.minecraftforge.event.world.BlockEvent;

import java.util.Map;
import java.util.UUID;

public class GrowHandler
{
    public static void handleSaplingGrow( SaplingGrowTreeEvent event )
    {
        World world = (World) event.getWorld();
        BlockPos pos = event.getPos();
        int dimId = world.getWorldType().getId();
        UUID uuid = ChunkDataHandler.checkPos( dimId, pos );
        ChunkDataHandler.delPos( dimId, pos );

        if( uuid != null )
        {
            ResourceLocation resLoc = event.getWorld().getBlockState( pos ).getBlock().getRegistryName();
            Map<String, Double> award = XP.getXp( resLoc, JType.XP_VALUE_GROW );

            if( award.size() > 0 )
                XP.awardXpMap( uuid, award, "Growing " + resLoc + " at " + pos, true, false );
            else
                Skill.addXp( Skill.FARMING.toString(), uuid, FConfig.defaultSaplingGrowXp, "Growing " + resLoc + " at " + pos, true, false );
        }
    }

    public static void handleCropGrow( BlockEvent.CropGrowEvent.Post event )
    {
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        ResourceLocation resLoc = event.getWorld().getBlockState( pos ).getBlock().getRegistryName();
        IBlockState state = world.getBlockState( pos );
        Block block = state.getBlock();

        UUID uuid = ChunkDataHandler.checkPos( world.getWorldType().getId(), pos );

        if( uuid == null && JsonConfig.data.get( JType.BLOCK_SPECIFIC ).containsKey( resLoc.toString() ) && JsonConfig.data.get( JType.BLOCK_SPECIFIC ).get( resLoc.toString() ).containsKey( "growsUpwards" ) )
        {
            BlockPos tempPos = pos;

            while( world.getBlockState( tempPos.down() ).getBlock().equals( block ) && uuid == null )
            {
                tempPos = tempPos.down();
                uuid = ChunkDataHandler.checkPos( world.getWorldType().getId(), tempPos );
            }
        }

        if( uuid != null && block instanceof BlockCrops )
        {
            BlockCrops blockCrops = (BlockCrops) block;

            if( blockCrops.isMaxAge( state ) )
            {
                Map<String, Double> award = XP.getXp( resLoc, JType.XP_VALUE_GROW );

                if( award.size() > 0 )
                    XP.awardXpMap( uuid, award, "Growing " + block.getRegistryName() + " at " + pos, true, false );
                else
                    Skill.addXp( Skill.FARMING.toString(), uuid, FConfig.defaultCropGrowXp, "Growing " + block.getRegistryName() + " at " + pos, true, false );
            }
        }
    }
}