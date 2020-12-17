package harmonised.pmmo.events;

import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.network.MessageDoubleTranslation;
import harmonised.pmmo.network.MessageGrow;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlockPlacedHandler
{
    private static final Map<UUID, BlockPos> lastPosPlaced = new HashMap<>();

    public static boolean handlePlaced(Entity entity, IBlockState state, World world, BlockPos pos )
    {
        if( entity instanceof EntityPlayerMP && !(entity instanceof FakePlayer ) )
        {
            EntityPlayerMP player = (EntityPlayerMP) entity;

            if ( XP.isPlayerSurvival( player ) )
            {
                Block block = state.getBlock();

                if( block.equals( Blocks.WATER ) )
                {
                    XP.awardXp( player, Skill.MAGIC.toString(), "Walking on water -gasp-", FConfig.jesusXp, true, false, false );
                    return false;
                }

                if( XP.checkReq( player, block.getRegistryName(), JType.REQ_PLACE ) )
                {
                    double blockHardnessLimitForPlacing = FConfig.blockHardnessLimitForPlacing;
                    double blockHardness = state.getBlockHardness( world, pos );
                    if ( blockHardness > blockHardnessLimitForPlacing )
                        blockHardness = blockHardnessLimitForPlacing;
                    String playerName = player.getName();
                    BlockPos blockPos = pos;
                    UUID playerUUID = player.getUniqueID();
                    Map<String, Double> award = new HashMap<>();
                    String sourceName = "Placing a Block";

                    if (!lastPosPlaced.containsKey(playerUUID) || !lastPosPlaced.get(playerUUID).equals(blockPos))
                    {
                        award = XP.getXp( block.getRegistryName(), JType.XP_VALUE_PLACE );

                        if( award.size() == 0 )
                        {
                            if (block.equals( Blocks.FARMLAND ) )
                            {
                                award.put( Skill.FARMING.toString(), blockHardness );
                                sourceName = "Tilting Dirt";
                            }
                            else
                                award.put( Skill.BUILDING.toString(), blockHardness );
                        }
                    }

                    XP.awardXpMap( player.getUniqueID(), award, sourceName, false, false );

                    if (lastPosPlaced.containsKey(playerName))
                        lastPosPlaced.replace(playerUUID, pos);
                    else
                        lastPosPlaced.put(playerUUID, blockPos);
                }
                else
                {
                    ItemStack mainItemStack = player.getHeldItemMainhand();
                    ItemStack offItemStack = player.getHeldItemOffhand();

                    if( mainItemStack.getItem() instanceof ItemBlock )
                        NetworkHandler.sendToPlayer( new MessageGrow( 0, mainItemStack.getCount() ), (EntityPlayerMP) player );
                    if( offItemStack.getItem() instanceof ItemBlock )
                        NetworkHandler.sendToPlayer( new MessageGrow( 1, offItemStack.getCount() ), (EntityPlayerMP) player );

                    if( JsonConfig.data.get( JType.INFO_PLANT ).containsKey( block.getRegistryName().toString() ) || block instanceof IPlantable)
                        NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.notSkilledEnoughToPlant", block.getUnlocalizedName(), "", true, 2 ), (EntityPlayerMP) player );
                    else
                        NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.notSkilledEnoughToPlaceDown", block.getUnlocalizedName(), "", true, 2 ), (EntityPlayerMP) player );

                    return true;
                }


                ChunkDataHandler.addPos( world.getWorldType().getId(), pos, player.getUniqueID() );
            }
        }
        return false;
    }
}
