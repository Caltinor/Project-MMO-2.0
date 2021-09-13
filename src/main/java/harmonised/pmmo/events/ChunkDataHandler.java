package harmonised.pmmo.events;

import harmonised.pmmo.util.XP;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.world.ChunkDataEvent;

import java.util.*;

public class ChunkDataHandler
{
    private static Map<ResourceLocation, Map<ChunkPos, Map<BlockPos, UUID>>> placedMap = new HashMap<>();

    public static void init()
    {
        placedMap = new HashMap<>();
    }

    public static void handleChunkDataLoad( ChunkDataEvent.Load event )
    {
        CompoundTag chunkNBT = event.getData();
        if( chunkNBT != null )
        {
            CompoundTag levelNBT = chunkNBT.getCompound( "Level" );
            if( levelNBT.contains( "placedPos" ) )
            {
                Level world = (Level) event.getWorld();
                ResourceLocation dimResLoc = XP.getDimResLoc( world );
                ChunkPos chunkPos = event.getChunk().getPos();

                if( !placedMap.containsKey( dimResLoc ) )
                    placedMap.put( dimResLoc, new HashMap<>() );

                CompoundTag placedPosNBT = ( (CompoundTag) levelNBT.get( "placedPos" ) );
                if( placedPosNBT == null )
                    return;
                Map<ChunkPos, Map<BlockPos, UUID>> chunkMap = placedMap.get( dimResLoc );
                Map<BlockPos, UUID> blockMap = new HashMap<>();
                Set<String> keySet = placedPosNBT.getAllKeys();

                keySet.forEach( key ->
                {
                    CompoundTag entry = placedPosNBT.getCompound( key );
                    blockMap.put( NbtUtils.readBlockPos( entry.getCompound( "pos" ) ), new UUID( entry.getCompound( "UUID" ).getLong("M"), entry.getCompound( "UUID" ).getLong("L") ) );
                });

                chunkMap.remove( chunkPos );
                chunkMap.put( chunkPos, blockMap );
            }
        }
    }

    public static void handleChunkDataSave( ChunkDataEvent.Save event )
    {
        Level world = (Level) event.getWorld();
        ResourceLocation dimResLoc = XP.getDimResLoc( world );
        if( placedMap.containsKey( dimResLoc ) )
        {
            ChunkPos chunkPos = event.getChunk().getPos();
            if( placedMap.get( dimResLoc ).containsKey( chunkPos ) )
            {
                CompoundTag levelNBT = (CompoundTag) event.getData().get( "Level" );
                if( levelNBT == null )
                    return;

                CompoundTag newPlacedNBT = new CompoundTag();
                CompoundTag insidesNBT;

                int i = 0;

                for( Map.Entry<BlockPos, UUID> entry : placedMap.get( dimResLoc ).get( chunkPos ).entrySet() )
                {
                    insidesNBT = new CompoundTag();
                    insidesNBT.put( "pos", NbtUtils.writeBlockPos( entry.getKey() ) );
                    insidesNBT.put( "UUID", XP.writeUniqueId( entry.getValue() ) );
                    newPlacedNBT.put( i++ + "", insidesNBT );
                }

                levelNBT.put( "placedPos", newPlacedNBT );
            }
        }
    }

    public static void addPos( ResourceLocation dimResLoc, BlockPos blockPos, UUID uuid )
    {
        ChunkPos chunkPos = new ChunkPos( blockPos );

        if( !placedMap.containsKey( dimResLoc ) )
            placedMap.put( dimResLoc, new HashMap<>() );

        Map<ChunkPos, Map<BlockPos, UUID>> chunkMap = placedMap.get( dimResLoc );

        if( !chunkMap.containsKey( chunkPos ) )
            chunkMap.put( chunkPos, new HashMap<>() );

        Map<BlockPos, UUID> blockMap = chunkMap.get( chunkPos );

        blockMap.put( blockPos, uuid );
    }

    public static void delPos( ResourceLocation dimResLoc, BlockPos blockPos )
    {
        ChunkPos chunkPos = new ChunkPos( blockPos );

        if( !placedMap.containsKey( dimResLoc ) )
            placedMap.put( dimResLoc, new HashMap<>() );

        Map<ChunkPos, Map<BlockPos, UUID>> chunkMap = placedMap.get( dimResLoc );

        if( !chunkMap.containsKey( chunkPos ) )
            chunkMap.put( chunkPos, new HashMap<>() );

        Map<BlockPos, UUID> blockMap = chunkMap.get( chunkPos );

        blockMap.remove( blockPos );
    }

    public static UUID checkPos( Level world, BlockPos pos )
    {
        return checkPos( XP.getDimResLoc( world ), pos );
    }

    public static UUID checkPos( ResourceLocation dimResLoc, BlockPos blockPos )
    {
        return placedMap.getOrDefault( dimResLoc, new HashMap<>() ).getOrDefault( new ChunkPos( blockPos ), new HashMap<>() ).get( blockPos );
    }
}