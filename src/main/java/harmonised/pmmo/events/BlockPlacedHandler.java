package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.gui.WorldXpDrop;
import harmonised.pmmo.network.MessageDoubleTranslation;
import harmonised.pmmo.network.MessageGrow;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlockPlacedHandler
{
    private static Map<UUID, BlockPos> lastPosPlaced = new HashMap<>();

    public static boolean handlePlaced( Entity entity, BlockState state, World world, BlockPos pos )
    {
        if( entity instanceof ServerPlayerEntity && !(entity instanceof FakePlayer) )
        {
            ServerPlayerEntity player = (ServerPlayerEntity) entity;
            
            if( XP.isHoldingDebugItemInOffhand( player ) )
                player.sendStatusMessage( new StringTextComponent( state.getBlock().getRegistryName().toString() ), false );

            if ( XP.isPlayerSurvival( player ) )
            {
                Block block = state.getBlock();
                if( block.equals( Blocks.BEDROCK ) )
                    Config.getAbilitiesMap( player ).put( "veinLeft", Config.forgeConfig.maxVeinCharge.get() );

                if( block.equals( Blocks.WATER ) )
                {
                    XP.awardXp( player, Skill.MAGIC.toString(), "Walking on water -gasp-", Config.forgeConfig.jesusXp.get(), true, false, false );
                    return false;
                }

                if( XP.checkReq( player, block.getRegistryName(), JType.REQ_PLACE ) )
                {
                    double blockHardnessLimitForPlacing = Config.forgeConfig.blockHardnessLimitForPlacing.get();
                    double blockHardness = state.getBlockHardness( world, pos );
                    if ( blockHardness > blockHardnessLimitForPlacing )
                        blockHardness = blockHardnessLimitForPlacing;
                    String playerName = player.getName().toString();
                    UUID playerUUID = player.getUniqueID();
                    Map<String, Double> award = new HashMap<>();
                    String sourceName = "Placing a Block";

                    if (!lastPosPlaced.containsKey(playerUUID) || !lastPosPlaced.get(playerUUID).equals(pos) )
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

                    for( String awardSkillName : award.keySet() )
                    {
                        WorldXpDrop xpDrop = new WorldXpDrop( pos.getX() + 0.5, pos.getY() + 1.523, pos.getZ() + 0.5, 0.35, award.get( awardSkillName ), awardSkillName );
                        WorldRenderHandler.addWorldXpDrop( xpDrop );
                        Skill.addXp( awardSkillName, player, award.get( awardSkillName ), sourceName, false, false );
                    }

                    if (lastPosPlaced.containsKey(playerName))
                        lastPosPlaced.replace(playerUUID, pos);
                    else
                        lastPosPlaced.put(playerUUID, pos);
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

                    return true;
                }

                ChunkDataHandler.addPos( XP.getDimensionResLoc( world ), pos, player.getUniqueID() );
            }
        }
        return false;
    }
}
