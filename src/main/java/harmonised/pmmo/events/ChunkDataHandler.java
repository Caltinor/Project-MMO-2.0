package harmonised.pmmo.events;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.ChunkDataEvent;

import java.util.*;

public class ChunkDataHandler
{
    private static Map<Integer, Map<ChunkPos, Map<BlockPos, UUID>>> placedMap = new HashMap<>();

    public static void init()
    {
        placedMap = new HashMap<>();
    }

    public static void handleChunkDataLoad( ChunkDataEvent.Load event )
    {
        NBTTagCompound levelNBT = event.getData().getCompoundTag( "Level" );

        if( levelNBT != null )
        {
            if( levelNBT.hasKey( "placedPos" ) )
            {
                int dimid = event.getWorld().getWorldType().getId();
                ChunkPos chunkPos = event.getChunk().getPos();

                if( !placedMap.containsKey( dimid ) )
                    placedMap.put( dimid, new HashMap<>() );

                NBTTagCompound placedPosNBT = levelNBT.getCompoundTag( "placedPos" );
                Map<ChunkPos, Map<BlockPos, UUID>> chunkMap = placedMap.get( dimid );
                Map<BlockPos, UUID> blockMap = new HashMap<>();
                Set<String> keySet = placedPosNBT.getKeySet();

                keySet.forEach( key ->
                {
                    NBTTagCompound entry = placedPosNBT.getCompoundTag( key );
                    blockMap.put( NBTUtil.getPosFromTag( entry.getCompoundTag( "pos" ) ), NBTUtil.getUUIDFromTag( entry.getCompoundTag( "UUID" ) ) );
                });

                chunkMap.remove( chunkPos );
                chunkMap.put( chunkPos, blockMap );
            }
        }
    }

    public static void handleChunkDataSave( ChunkDataEvent.Save event )
    {
        int dimid = event.getWorld().getWorldType().getId();
        if( placedMap.containsKey( dimid ) )
        {
            ChunkPos chunkPos = event.getChunk().getPos();
            if( placedMap.get( dimid ).containsKey( chunkPos ) )
            {
                NBTTagCompound levelNBT = event.getData().getCompoundTag( "Level" );
                NBTTagCompound newPlacedNBT = new NBTTagCompound();
                NBTTagCompound insidesNBT;

                int i = 0;

                for( Map.Entry<BlockPos, UUID> entry : placedMap.get( dimid ).get( chunkPos ).entrySet() )
                {
                    insidesNBT = new NBTTagCompound();
                    insidesNBT.setTag( "pos", NBTUtil.createPosTag( entry.getKey() ) );
                    insidesNBT.setTag( "UUID", NBTUtil.createUUIDTag( entry.getValue() ) );
                    newPlacedNBT.setTag( i++ + "", insidesNBT );
                }

                levelNBT.setTag( "placedPos", newPlacedNBT );
            }
        }
    }

    public static void addPos( int dimid, BlockPos blockPos, UUID uuid )
    {
        ChunkPos chunkPos = new ChunkPos( blockPos );

        if( !placedMap.containsKey( dimid ) )
            placedMap.put( dimid, new HashMap<>() );

        Map<ChunkPos, Map<BlockPos, UUID>> chunkMap = placedMap.get( dimid );

        if( !chunkMap.containsKey( chunkPos ) )
            chunkMap.put( chunkPos, new HashMap<>() );

        Map<BlockPos, UUID> blockMap = chunkMap.get( chunkPos );

        blockMap.put( blockPos, uuid );

//        System.out.println( chunkMap.size() );
//        System.out.println( blockMap.size() );
    }

    public static void delPos( int dimid, BlockPos blockPos )
    {
        ChunkPos chunkPos = new ChunkPos( blockPos );

        if( !placedMap.containsKey( dimid ) )
            placedMap.put( dimid, new HashMap<>() );

        Map<ChunkPos, Map<BlockPos, UUID>> chunkMap = placedMap.get( dimid );

        if( !chunkMap.containsKey( chunkPos ) )
            chunkMap.put( chunkPos, new HashMap<>() );

        Map<BlockPos, UUID> blockMap = chunkMap.get( chunkPos );

        blockMap.remove( blockPos );

//        System.out.println( chunkMap.size() );
//        System.out.println( blockMap.size() );
    }

    public static UUID checkPos( World world, BlockPos pos )
    {
        return checkPos( world.getWorldType().getId(), pos );
    }

    public static UUID checkPos( int dimid, BlockPos blockPos )
    {
        return placedMap.getOrDefault( dimid, new HashMap<>() ).getOrDefault( new ChunkPos( blockPos ), new HashMap<>() ).get( blockPos );
    }
}
