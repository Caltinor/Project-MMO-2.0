package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.network.MessageDoubleTranslation;
import harmonised.pmmo.network.MessageGrow;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
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

    public static void handlePlaced( BlockEvent.EntityMultiPlaceEvent event )
    {
        if( event.getEntity() instanceof ServerPlayerEntity && !(event.getEntity() instanceof FakePlayer ) )
        {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();

            if ( XP.isPlayerSurvival( player ) )
            {
                Block block = event.getPlacedBlock().getBlock();

                if( block.equals( Blocks.WATER ) )
                {
                    XP.awardXp( player, Skill.MAGIC, "Walking on water -gasp-", Config.forgeConfig.jesusXp.get(), true, false, false );
                    return;
                }

                if( XP.checkReq( player, block.getRegistryName(), JType.REQ_PLACE ) )
                {
                    double blockHardnessLimitForPlacing = Config.forgeConfig.blockHardnessLimitForPlacing.get();
                    double blockHardness = event.getPlacedBlock().getBlockHardness( event.getWorld(), event.getPos() );
                    if ( blockHardness > blockHardnessLimitForPlacing )
                        blockHardness = blockHardnessLimitForPlacing;
                    String playerName = player.getName().toString();
                    BlockPos blockPos = event.getPos();
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
                        lastPosPlaced.replace(playerUUID, event.getPos());
                    else
                        lastPosPlaced.put(playerUUID, blockPos);
                }
                else
                {
                    ItemStack mainItemStack = player.getHeldItemMainhand();
                    ItemStack offItemStack = player.getHeldItemOffhand();

                    if( mainItemStack.getItem() instanceof BlockItem)
                        NetworkHandler.sendToPlayer( new MessageGrow( 0, mainItemStack.getCount() ), (ServerPlayerEntity) player );
                    if( offItemStack.getItem() instanceof BlockItem )
                        NetworkHandler.sendToPlayer( new MessageGrow( 1, offItemStack.getCount() ), (ServerPlayerEntity) player );

                    if( JsonConfig.data.get( JType.INFO_PLANT ).containsKey( block.getRegistryName().toString() ) || block instanceof IPlantable)
                        NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.notSkilledEnoughToPlant", block.getTranslationKey(), "", true, 2 ), (ServerPlayerEntity) player );
                    else
                        NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.notSkilledEnoughToPlaceDown", block.getTranslationKey(), "", true, 2 ), (ServerPlayerEntity) player );

                    event.setCanceled( true );
                }

                for( BlockSnapshot blockSnapshot : event.getReplacedBlockSnapshots() )
                {
                    ChunkDataHandler.addPos( event.getWorld().getDimension().getType().getRegistryName(), blockSnapshot.getPos(), player.getUniqueID() );
                }
            }
        }
    }
}
