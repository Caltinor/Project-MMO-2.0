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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlockPlacedHandler
{
    private static final Map<UUID, BlockPos> lastPosPlaced = new HashMap<>();

    public static void handlePlaced( BlockEvent.EntityPlaceEvent event )
    {
        if( event.getEntity() instanceof EntityPlayerMP && !(event.getEntity() instanceof FakePlayer ) )
        {
            EntityPlayerMP player = (EntityPlayerMP) event.getEntity();

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
                    double blockHardness = block.getBlockHardness(block.getDefaultState(), event.getWorld(), event.getPos());
                    if ( blockHardness > blockHardnessLimitForPlacing )
                        blockHardness = blockHardnessLimitForPlacing;
                    String playerName = player.getName().toString();
                    BlockPos blockPos = event.getPos();
                    UUID playerUUID = player.getUniqueID();

                    if (!lastPosPlaced.containsKey(playerUUID) || !lastPosPlaced.get(playerUUID).equals(blockPos))
                    {
                        if (block.equals(Blocks.FARMLAND))
                            XP.awardXp( player, Skill.FARMING, "tilting dirt", blockHardness, false, false, false );
                        else
                        {
//								for( int i = 0; i < 1000; i++ )
//							{
                            XP.awardXp( player, Skill.BUILDING, "placing a block", blockHardness, false, false, false );
//							}
                        }
                    }

                    if (lastPosPlaced.containsKey(playerName))
                        lastPosPlaced.replace(playerUUID, event.getPos());
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

                    event.setCanceled( true );
                }

                ChunkDataHandler.addPos( event.getWorld().getDimension().getType().getRegistryName(), event.getPos(), player.getUniqueID() );
            }
        }
    }
}
