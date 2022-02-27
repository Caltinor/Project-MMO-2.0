package harmonised.pmmo.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.serialization.Codec;

import harmonised.pmmo.config.codecs.CodecTypes;
import harmonised.pmmo.util.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SerializableUUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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
    
    private static final Codec<Map<ResourceLocation, Map<ChunkPos, Map<BlockPos, UUID>>>> PLACED_CODEC =
    		Codec.unboundedMap(ResourceLocation.CODEC, 
    				Codec.unboundedMap(CodecTypes.CHUNKPOS_CODEC, 
    						Codec.unboundedMap(CodecTypes.BLOCKPOS_CODEC, SerializableUUID.CODEC)));
    
    private static final String PLACED_MAP = "placed_map";

    @SubscribeEvent
    public static void handleChunkDataLoad(ChunkDataEvent.Load event){
    	placedMap = new HashMap<>(PLACED_CODEC.parse(NbtOps.INSTANCE, event.getData().getCompound(PLACED_MAP)).result().orElse(new HashMap<>()));
    }

    @SubscribeEvent
    public static void handleChunkDataSave(ChunkDataEvent.Save event){
    	event.getData().put(PLACED_MAP, ((CompoundTag)(PLACED_CODEC.encodeStart(NbtOps.INSTANCE, placedMap).result().orElse(new CompoundTag()))));
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
        return checkPos(world.dimension().getRegistryName(), pos);
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
