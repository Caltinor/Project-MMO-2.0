package harmonised.pmmo.events;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.perks.PerkRegistry;
import harmonised.pmmo.api.perks.PerkTrigger;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.network.MessageUpdateBoolean;
import harmonised.pmmo.network.MessageUpdatePlayerNBT;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.VeinInfo;
import harmonised.pmmo.util.NBTHelper;
import harmonised.pmmo.util.ServerUtil;
import harmonised.pmmo.util.XP;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.BlockEvent;
import org.apache.logging.log4j.*;

import java.util.*;

public class WorldTickHandler
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static Map<Player, VeinInfo> activeVein;
    public static Map<Player, ArrayList<BlockPos>> veinSet;
    private static double minVeinCost, minVeinHardness, levelsPerHardnessMining, levelsPerHardnessWoodcutting, levelsPerHardnessExcavation, levelsPerHardnessFarming, levelsPerHardnessCrafting, veinMaxBlocks, maxVeinCharge, exhaustionPerBlock;
    private static int veinMaxDistance;
//    public static long lastVeinUpdateTime = System.nanoTime();

    public static void refreshVein()
    {
        activeVein = new HashMap<>();
        veinSet = new HashMap<>();

        minVeinCost = Config.getConfig("minVeinCost");
        minVeinHardness = Config.getConfig("minVeinHardness");
        levelsPerHardnessMining = Config.getConfig("levelsPerHardnessMining");
        levelsPerHardnessWoodcutting = Config.getConfig("levelsPerHardnessWoodcutting");
        levelsPerHardnessExcavation = Config.getConfig("levelsPerHardnessExcavation");
        levelsPerHardnessFarming = Config.getConfig("levelsPerHardnessFarming");
        levelsPerHardnessCrafting = Config.getConfig("levelsPerHardnessCrafting");
        veinMaxDistance = (int) Math.floor(Config.forgeConfig.veinMaxDistance.get());
        exhaustionPerBlock = Config.forgeConfig.exhaustionPerBlock.get();
        veinMaxBlocks = Config.forgeConfig.veinMaxBlocks.get();
        maxVeinCharge = Config.forgeConfig.maxVeinCharge.get();
    }

    public static void handleWorldTick(TickEvent.WorldTickEvent event)
    {
        ServerUtil.tick(event);
        int veinSpeed = (int) Math.floor(Config.forgeConfig.veinSpeed.get());
        VeinInfo veinInfo;
        Level world;
        ItemStack startItemStack;
        Item startItem;
        BlockPos veinPos;
        BlockState veinState;
        Map<String, Double> abilitiesMap;
        String regKey;
        String skill;
        double cost;
        boolean correctBlock, correctItem, correctHeldItem, isOwner;
        UUID blockUUID, playerUUID;
        int age = -1, maxAge = -2;

        if(event.world.getServer() == null)
            return;

        if(XP.getDimResLoc(event.world).equals(DimensionType.OVERWORLD_LOCATION.getRegistryName()) && event.world.getServer().getTickCount() % 200 == 0)
        {
            for (ServerPlayer player : event.world.getServer().getPlayerList().getPlayers())
            {
            	PerkRegistry.executePerk(PerkTrigger.SKILL_UP, player);
            }
        }
        
        for(Player player : event.world.getServer().getPlayerList().getPlayers())
        {
            playerUUID = player.getUUID();

            for(int i = 0; i < veinSpeed; i++)
            {
                if(activeVein.containsKey(player) && veinSet.get(player).size() > 0)
                {
                    veinInfo = activeVein.get(player);
                    world = veinInfo.world;
                    startItemStack = veinInfo.itemStack;
                    startItem = veinInfo.startItem;
                    veinPos = veinSet.get(player).get(0);
                    veinState = world.getBlockState(veinPos);
                    abilitiesMap = Config.getAbilitiesMap(player);
                    regKey = veinState.getBlock().getRegistryName().toString();
                    cost = getVeinCost(veinState, veinPos, player);
                    correctBlock = world.getBlockState(veinPos).getBlock().equals(veinInfo.state.getBlock());
                    correctItem = !startItem.canBeDepleted() || (startItemStack.getDamageValue() < startItemStack.getMaxDamage());
                    correctHeldItem = player.getMainHandItem().getItem().equals(startItem);
                    blockUUID = ChunkDataHandler.checkPos(world, veinPos);
                    isOwner = blockUUID == null || blockUUID.equals(playerUUID);
                    skill = XP.getSkill(veinState);

                    if(skill.equals(Skill.FARMING.toString()) && !(JsonConfig.data.get(JType.BLOCK_SPECIFIC).containsKey(regKey) && JsonConfig.data.get(JType.BLOCK_SPECIFIC).get(regKey).containsKey("growsUpwards")))
                    {
                        if(veinState.hasProperty(BlockStateProperties.AGE_1))
                        {
                            age = veinState.getValue(BlockStateProperties.AGE_1);
                            maxAge = 1;
                        }
                        else if(veinState.hasProperty(BlockStateProperties.AGE_2))
                        {
                            age = veinState.getValue(BlockStateProperties.AGE_2);
                            maxAge = 2;
                        }
                        else if(veinState.hasProperty(BlockStateProperties.AGE_3))
                        {
                            age = veinState.getValue(BlockStateProperties.AGE_3);
                            maxAge = 3;
                        }
                        else if(veinState.hasProperty(BlockStateProperties.AGE_5))
                        {
                            age = veinState.getValue(BlockStateProperties.AGE_5);
                            maxAge = 5;
                        }
                        else if(veinState.hasProperty(BlockStateProperties.AGE_7))
                        {
                            age = veinState.getValue(BlockStateProperties.AGE_7);
                            maxAge = 7;
                        }
                        else if(veinState.hasProperty(BlockStateProperties.AGE_15))
                        {
                            age = veinState.getValue(BlockStateProperties.AGE_15);
                            maxAge = 15;
                        }
                        else if(veinState.hasProperty(BlockStateProperties.AGE_25))
                        {
                            age = veinState.getValue(BlockStateProperties.AGE_25);
                            maxAge = 25;
                        }
                        else if(veinState.hasProperty(BlockStateProperties.PICKLES))
                        {
                            age = veinState.getValue(BlockStateProperties.PICKLES);
                            maxAge = 4;
                        }

                        if(age >= 0 && age != maxAge)
                        {
                            veinSet.get(player).remove(0);
                            return;
                        }
                    }

                    if((abilitiesMap.get("veinLeft") >= cost || player.isCreative()) && XP.isVeining.contains(player.getUUID()))
                    {
                        veinSet.get(player).remove(0);

                        BlockEvent.BreakEvent veinEvent = new BlockEvent.BreakEvent(world, veinPos, veinState, player);
                        MinecraftForge.EVENT_BUS.post(veinEvent);

                        if (!veinEvent.isCanceled())
                        {
                            if(correctBlock)
                            {
                                if(player.isCreative())
                                    world.destroyBlock(veinPos, false);
                                else if(correctItem && correctHeldItem && player.getFoodData().getFoodLevel() > 0)
                                {
                                    if(Config.forgeConfig.veiningOtherPlayerBlocksAllowed.get() || isOwner)
                                    {
                                        abilitiesMap.put("veinLeft", abilitiesMap.get("veinLeft") - cost);
                                        destroyBlock(world, veinPos, player, startItemStack);
                                        player.causeFoodExhaustion((float) exhaustionPerBlock);
                                    }
                                }
                                else
                                {
                                    activeVein.remove(player);
                                    veinSet.remove(player);
                                    NetworkHandler.sendToPlayer(new MessageUpdateBoolean(false, 0), (ServerPlayer) player);
                                }
                            }
                        }
                    }
                    else
                    {
                        activeVein.remove(player);
                        veinSet.remove(player);
                        NetworkHandler.sendToPlayer(new MessageUpdateBoolean(false, 0), (ServerPlayer) player);
                    }
                }
                else
                {
                    activeVein.remove(player);
                    veinSet.remove(player);
                    NetworkHandler.sendToPlayer(new MessageUpdateBoolean(false, 0), (ServerPlayer) player);
                }
            }
        }
    }

    public static void destroyBlock(Level world, BlockPos pos, Player player, ItemStack toolUsed)
    {
        BlockState blockstate = world.getBlockState(pos);
        FluidState ifluidstate = world.getFluidState(pos);
        world.levelEvent(2001, pos, Block.getId(blockstate));

        BlockEntity tileentity = blockstate.hasBlockEntity() ? world.getBlockEntity(pos) : null;
        Block.dropResources(blockstate, world, pos, tileentity, player, toolUsed);

        if(Config.forgeConfig.damageToolWhileVeining.get() && world.setBlock(pos, ifluidstate.createLegacyBlock(), 3) && toolUsed.isDamageableItem() && !player.isCreative())
            toolUsed.hurtAndBreak(1, player, (a) -> a.broadcastBreakEvent(InteractionHand.MAIN_HAND));
    }

    public static double getVeinLeft(Player player)
    {
        return Config.getAbilitiesMap(player).getOrDefault("veinLeft", 0D);
    }

    public static void scheduleVein(Player player, VeinInfo veinInfo)
    {
        double veinLeft = getVeinLeft(player);
        double veinCost = getVeinCost(veinInfo.state, veinInfo.pos, player);
        String blockKey = veinInfo.state.getBlock().getRegistryName().toString();
        ArrayList<BlockPos> blockPosArrayList;

        if(!(canVeinGlobal(blockKey, player) && canVeinDimension(blockKey, player) ) || !XP.checkReq(player, player.getMainHandItem().getItem().getRegistryName(), JType.REQ_TOOL))
            return;

        blockPosArrayList = getVeinShape(veinInfo, veinLeft, veinCost, player.isCreative(), false);

        if(blockPosArrayList.size() > 0)
        {
            activeVein.put(player, veinInfo);
            veinSet.put(player, blockPosArrayList);
            NetworkHandler.sendToPlayer(new MessageUpdateBoolean(true, 0), (ServerPlayer) player);
        }
    }

    public static boolean canVeinGlobal(String blockKey, Player player)
    {
        if(player.isCreative())
            return true;

        Map<String, Double> globalBlacklist = null;

        if(JsonConfig.data.get(JType.VEIN_BLACKLIST).containsKey("all_dimensions"))
            globalBlacklist = JsonConfig.data.get(JType.VEIN_BLACKLIST).get("all_dimensions");

        return globalBlacklist == null || !globalBlacklist.containsKey(blockKey);
    }

    public static boolean canVeinDimension(String blockKey, Player player)
    {
        if(player.isCreative())
            return true;

        Level world = player.level;
        if(world == null)
            return true;

        ResourceLocation dimensionKey = XP.getDimResLoc(world);
        Map<String, Double> dimensionBlacklist = null;

        if(JsonConfig.data.get(JType.VEIN_BLACKLIST).containsKey(dimensionKey.toString()))
            dimensionBlacklist = JsonConfig.data.get(JType.VEIN_BLACKLIST).get(dimensionKey.toString());

        return dimensionBlacklist == null || !dimensionBlacklist.containsKey(blockKey);
    }

    public static ArrayList<BlockPos> getVeinShape(VeinInfo veinInfo, double veinLeft, double veinCost, boolean isCreative, boolean isLooped)
    {
        Set<BlockPos> vein = new HashSet<>();
        ArrayList<BlockPos> outVein = new ArrayList<>();
        ArrayList<BlockPos> curLayer = new ArrayList<>();
        ArrayList<BlockPos> nextLayer = new ArrayList<>();
        BlockPos originPos = veinInfo.pos;
        BlockPos highestPos = originPos;
        curLayer.add(originPos);
        BlockPos curPos2;
        Block block = veinInfo.state.getBlock();
        Material material = veinInfo.state.getMaterial();
        String regKey = block.getRegistryName().toString();
        String skill = XP.getSkill(veinInfo.state);

        int yLimit = 1;

        if(JsonConfig.data.get(JType.BLOCK_SPECIFIC).containsKey(regKey))
        {
            if(JsonConfig.data.get(JType.BLOCK_SPECIFIC).get(regKey).containsKey("growsUpwards"))
                yLimit = 0;
        }

        while((isCreative || veinLeft * 1.523 > veinCost * vein.size() || (Config.forgeConfig.veinWoodTopToBottom.get() && !isLooped && skill.equals(Skill.WOODCUTTING.toString()))) && vein.size() <= veinMaxBlocks)
        {
            for(BlockPos curPos : curLayer)
            {
                if(curPos.closerThan(originPos, veinMaxDistance))
                {
                    for(int i = yLimit; i >= -yLimit; i--)
                    {
                        for(int j = 1; j >= -1; j--)
                        {
                            for(int k = 1; k >= -1; k--)
                            {
                                curPos2 = curPos.above(i).north(j).east(k);
                                if(!vein.contains(curPos2) && veinInfo.world.getBlockState(curPos2).getBlock().equals(block))
                                {
                                    vein.add(curPos2);
                                    outVein.add(curPos2);
                                    nextLayer.add(curPos2);

                                    if(curPos2.getY() > highestPos.getY())
                                        highestPos = new BlockPos(curPos2);
                                }
                            }
                        }
                    }
                }
            }

            if(nextLayer.size() == 0)
                break;

            curLayer = nextLayer;
            nextLayer = new ArrayList<>();
        }

        if(!isLooped)
        {
            if((Config.forgeConfig.veinWoodTopToBottom.get() && material.equals(Material.WOOD)) /* || block.equals(Blocks.SAND) || block.equals(Blocks.GRAVEL) */)
            veinInfo.pos = highestPos;
            return getVeinShape(veinInfo, veinLeft, veinCost, isCreative, true);
        }

        return outVein;
    }

    public static double getVeinCost(BlockState state, BlockPos pos, Player player)
    {
        String skill = XP.getSkill(state);
        double cost;
//        double startHardness = state.getBlockHardness(player.level, pos);
        double hardness = state.getDestroySpeed(player.level, pos);
        double level = APIUtils.getLevel(skill, player);

        if(hardness < minVeinHardness)
            hardness = minVeinHardness;

//        if(startHardness == 0)
//            hardness = 0;
//        System.out.println("Stone: " + player.getHeldItemMainhand().getDestroySpeed(Blocks.STONE.getDefaultState()));
//        System.out.println("Wood: " + player.getHeldItemMainhand().getDestroySpeed(Blocks.OAK_LOG.getDefaultState()));
//        System.out.println("Dirt: " + player.getHeldItemMainhand().getDestroySpeed(Blocks.DIRT.getDefaultState()));

        switch(skill)
        {
            case "mining":
                cost = hardness / (level / levelsPerHardnessMining);
                break;

            case "woodcutting":
                cost = hardness / (level / levelsPerHardnessWoodcutting);
                break;

            case "excavation":
                cost = hardness / (level / levelsPerHardnessExcavation);
                break;

            case "farming":
                cost = hardness / (level / levelsPerHardnessFarming);
                break;

            case "crafting":
                cost = hardness / (level / levelsPerHardnessCrafting);
                break;

            default:
                cost = hardness;
                break;
        }

        double mainItemSpeed = player.getMainHandItem().getDestroySpeed(state);
        if(mainItemSpeed > 1)
            cost /= Math.max(1, ((mainItemSpeed + 4) * (1 / Config.getConfig("toolSpeedVeinScale"))));

        if(cost < minVeinCost)
            cost = minVeinCost;

//        System.out.println(cost);

        return cost;
    }

    public static void updateVein(Player player, double gap)
    {
        Map<String, Double> abilitiesMap = Config.getAbilitiesMap(player);

        if(!abilitiesMap.containsKey("veinLeft"))
            abilitiesMap.put("veinLeft", maxVeinCharge);

        double veinLeft = abilitiesMap.get("veinLeft");
        if(veinLeft < 0)
            veinLeft = 0D;

        if(!activeVein.containsKey(player))
            veinLeft += Math.min(gap, 2);

        if(veinLeft > maxVeinCharge)
            veinLeft = maxVeinCharge;

        abilitiesMap.put("veinLeft", veinLeft);

        NetworkHandler.sendToPlayer(new MessageUpdatePlayerNBT(NBTHelper.mapStringToNbt(abilitiesMap), 1), (ServerPlayer) player);
    }
}
