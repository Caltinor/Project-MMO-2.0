package harmonised.pmmo.events;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.gui.WorldXpDrop;
import harmonised.pmmo.network.MessageDoubleTranslation;
import harmonised.pmmo.network.MessageGrow;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlockPlacedHandler
{
    private static Map<UUID, BlockPos> lastPosPlaced = new HashMap<>();

    public static boolean handlePlaced(Entity entity, BlockState state, Level world, BlockPos pos)
    {
        if(entity instanceof ServerPlayer && !(entity instanceof FakePlayer))
        {
            ServerPlayer player = (ServerPlayer) entity;
            
            if(XP.isHoldingDebugItemInOffhand(player))
                player.displayClientMessage(new TextComponent(state.getBlock().getRegistryName().toString()), false);

            if (XP.isPlayerSurvival(player))
            {
                Block block = state.getBlock();
                if(block.equals(Blocks.BEDROCK))
                    Config.getAbilitiesMap(player).put("veinLeft", Config.forgeConfig.maxVeinCharge.get());

                if(block.equals(Blocks.WATER))
                {
                    XP.awardXp(player, Skill.MAGIC.toString(), "Walking on water -gasp-", Config.forgeConfig.jesusXp.get(), true, false, false);
                    return false;
                }

                if(XP.checkReq(player, block.getRegistryName(), JType.REQ_PLACE))
                {
                    double blockHardnessLimitForPlacing = Config.forgeConfig.blockHardnessLimitForPlacing.get();
                    double blockHardness = state.getDestroySpeed(world, pos);
                    if (blockHardness > blockHardnessLimitForPlacing)
                        blockHardness = blockHardnessLimitForPlacing;
                    UUID playerUUID = player.getUUID();
                    Map<String, Double> award = new HashMap<>();
                    String sourceName = "Placing a Block";

                    if (!lastPosPlaced.containsKey(playerUUID) || !lastPosPlaced.get(playerUUID).equals(pos))
                    {
                    	BlockEntity tile = world.getBlockEntity(pos);
                        award = tile == null ? XP.getXpBypass(block.getRegistryName(), JType.XP_VALUE_PLACE) : XP.getXp(tile, JType.XP_VALUE_PLACE);

                        if(award.size() == 0)
                        {
                            if (block.equals(Blocks.FARMLAND))
                            {
                                award.put(Skill.FARMING.toString(), blockHardness);
                                sourceName = "Tilting Dirt";
                            }
                            else
                                award.put(Skill.BUILDING.toString(), blockHardness);
                        }
                    }

                    for(String awardSkillName : award.keySet())
                    {
                        WorldXpDrop xpDrop = WorldXpDrop.fromXYZ(XP.getDimResLoc(world), pos.getX() + 0.5, pos.getY() + 1.523, pos.getZ() + 0.5, 0.35, award.get(awardSkillName), awardSkillName);
                        XP.addWorldXpDrop(xpDrop, player);
                        Skill.addXp(awardSkillName, player, award.get(awardSkillName), sourceName, false, false);
                    }

                    if (lastPosPlaced.containsKey(playerUUID))
                        lastPosPlaced.replace(playerUUID, pos);
                    else
                        lastPosPlaced.put(playerUUID, pos);
                }
                else
                {
                    ItemStack mainItemStack = player.getMainHandItem();
                    ItemStack offItemStack = player.getOffhandItem();

                    if(mainItemStack.getItem() instanceof BlockItem)
                        NetworkHandler.sendToPlayer(new MessageGrow(0, mainItemStack.getCount()), (ServerPlayer) player);
                    if(offItemStack.getItem() instanceof BlockItem)
                        NetworkHandler.sendToPlayer(new MessageGrow(1, offItemStack.getCount()), (ServerPlayer) player);

                    if(JsonConfig.data.get(JType.INFO_PLANT).containsKey(block.getRegistryName().toString()) || block instanceof IPlantable)
                        NetworkHandler.sendToPlayer(new MessageDoubleTranslation("pmmo.notSkilledEnoughToPlant", block.getDescriptionId(), "", true, 2), (ServerPlayer) player);
                    else
                        NetworkHandler.sendToPlayer(new MessageDoubleTranslation("pmmo.notSkilledEnoughToPlaceDown", block.getDescriptionId(), "", true, 2), (ServerPlayer) player);

                    return true;
                }
            }
        }
        ChunkDataHandler.addPos(XP.getDimResLoc(world), pos, entity.getUUID());
        return false;
    }
}
