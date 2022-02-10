package harmonised.pmmo.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import harmonised.pmmo.util.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.PistonEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class ChunkDataHandler {
    private static Map<ResourceLocation, Map<ChunkPos, Map<BlockPos, UUID>>> placedMap = new HashMap<>();
    
    private static final String LEVEL = "Level";
    private static final String PLACED_POS = "placedPos";
    private static final String PID = "UUID";
    private static final String BLOCK_POS = "pos";

    @SubscribeEvent
    public static void handleChunkDataLoad(ChunkDataEvent.Load event)
    {
        CompoundTag chunkNBT = event.getData();
        if(chunkNBT != null)
        {
            CompoundTag levelNBT = chunkNBT.getCompound(LEVEL);
            if(levelNBT.contains(PLACED_POS))
            {
                ResourceLocation dimResLoc = ((Level) event.getWorld()).dimension().location();
                ChunkPos chunkPos = event.getChunk().getPos();

                if(!placedMap.containsKey(dimResLoc))
                    placedMap.put(dimResLoc, new HashMap<>());

                ListTag placedPosNBT = levelNBT.getList(PLACED_POS, Tag.TAG_COMPOUND);
                if(placedPosNBT == null)
                    return;
                Map<ChunkPos, Map<BlockPos, UUID>> chunkMap = placedMap.get(dimResLoc);
                Map<BlockPos, UUID> blockMap = new HashMap<>();
                
                for(int i = 0; i < placedPosNBT.size(); i++) {
                	blockMap.put(BlockPos.of(placedPosNBT.getCompound(i).getLong(BLOCK_POS)), placedPosNBT.getCompound(i).getUUID(PID));
                }

                chunkMap.put(chunkPos, blockMap);
            }
        }
    }

    @SubscribeEvent
    public static void handleChunkDataSave(ChunkDataEvent.Save event)
    {
        ResourceLocation dimResLoc = ((Level)event.getWorld()).dimension().location();
        if(placedMap.containsKey(dimResLoc))
        {
            ChunkPos chunkPos = event.getChunk().getPos();
            if(placedMap.get(dimResLoc).containsKey(chunkPos))
            {
                if(!event.getData().contains(LEVEL))
                    event.getData().put(LEVEL, new CompoundTag());
                CompoundTag levelNBT = (CompoundTag) event.getData().get(LEVEL);

                ListTag placedList = new ListTag();
                for(Map.Entry<BlockPos, UUID> entry : placedMap.get(dimResLoc).get(chunkPos).entrySet())
                {
                    CompoundTag insidesNBT = new CompoundTag();
                    insidesNBT.putLong(BLOCK_POS, entry.getKey().asLong());
                    insidesNBT.putUUID(PID, entry.getValue());
                    placedList.add(insidesNBT);
                }

                levelNBT.put(PLACED_POS, placedList);
            }
        }
    }
    
    @SubscribeEvent
    public static void handlePistonPush(PistonEvent event)
    {
        if(!event.getWorld().isClientSide())
        {
            Level world = (Level) event.getWorld();
            BlockPos pistonPos = event.getPos();
            Direction direction = event.getDirection();
            UUID uuid;
            if(event.getPistonMoveType().equals(PistonEvent.PistonMoveType.EXTEND))
            {
                uuid = ChunkDataHandler.checkPos(world, pistonPos.relative(direction));
                if(uuid != null)
                {
                    ChunkDataHandler.addPos(world.dimension().location(), pistonPos.relative(direction, 2), uuid);
                    ChunkDataHandler.delPos(world.dimension().location(), pistonPos.relative(direction));
                }
            }
            else
            {
                BlockState state = world.getBlockState(pistonPos);
                if(state.hasProperty(MovingPistonBlock.TYPE) && state.getValue(MovingPistonBlock.TYPE).equals(PistonType.STICKY))
                {
                	//TODO grab a potentially pulled block UUID and make sure to update.
                    uuid = UUID.fromString("80008135-1337-3251-1523-852369874125");
                    ChunkDataHandler.addPos(world.dimension().location(), pistonPos.relative(direction), uuid);
                    ChunkDataHandler.delPos(world.dimension().location(), pistonPos.relative(direction, 2));
                }
            }
        }
    }

    public static void addPos(ResourceLocation dimResLoc, BlockPos blockPos, UUID uuid)
    {
        ChunkPos chunkPos = new ChunkPos(blockPos);
        placedMap.computeIfAbsent(dimResLoc, s -> new HashMap<>())
    		.computeIfAbsent(chunkPos, s -> new HashMap<>())
    		.put(blockPos, uuid);
    }

    public static void delPos(ResourceLocation dimResLoc, BlockPos blockPos)
    {
        ChunkPos chunkPos = new ChunkPos(blockPos);
        placedMap.computeIfAbsent(dimResLoc, s -> new HashMap<>())
        	.computeIfAbsent(chunkPos, s -> new HashMap<>())
        	.remove(blockPos);
    }

    public static UUID checkPos(Level world, BlockPos pos)
    {
        return checkPos(world.dimension().location(), pos);
    }

    public static UUID checkPos(ResourceLocation dimResLoc, BlockPos blockPos)
    {
        return placedMap.getOrDefault(dimResLoc, new HashMap<>()).getOrDefault(new ChunkPos(blockPos), new HashMap<>()).get(blockPos);
    }
    
    public static boolean playerMatchesPos(Player player, BlockPos pos) {
    	ResourceLocation dimKey = player.getLevel().dimension().getRegistryName();
    	if (placedMap.containsKey(dimKey)) {
    		ChunkPos cp = new ChunkPos(pos);
    		Map<ChunkPos, Map<BlockPos, UUID>> map = placedMap.get(dimKey);
    		if (map.containsKey(cp)) {
    			Map<BlockPos, UUID> innerMap = map.get(cp);
    			if (innerMap.containsKey(pos))
    				return innerMap.get(pos).equals(player.getUUID());
    		}
    	}
    	return false;
    }
}
