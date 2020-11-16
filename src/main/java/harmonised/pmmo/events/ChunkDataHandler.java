package harmonised.pmmo.events;

import harmonised.pmmo.util.XP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
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
        NBTTagCompound levelNBT = event.getData();

        if( levelNBT != null )
        {
            if( levelNBT.contains( "placedPos" ) )
            {
                ResourceLocation dimResLoc = event.getWorld().getDimension().getType().getRegistryName();
                ChunkPos chunkPos = event.getChunk().getPos();

                if( !placedMap.containsKey( dimResLoc ) )
                    placedMap.put( dimResLoc, new HashMap<>() );

                NBTTagCompound placedPosNBT = ( (NBTTagCompound) levelNBT.get( "placedPos" ) );
                if( placedPosNBT == null )
                    return;
                Map<ChunkPos, Map<BlockPos, UUID>> chunkMap = placedMap.get( dimResLoc );
                Map<BlockPos, UUID> blockMap = new HashMap<>();
                Set<String> keySet = placedPosNBT.getKeySet();

                keySet.forEach( key ->
                {
                    NBTTagCompound entry = placedPosNBT.getCompoundTag( key );
                    blockMap.put( NBTUtil.readBlockPos( entry.getCompoundTag( "pos" ) ), NBTUtil.readUniqueId( entry.getCompoundTag( "UUID" ) ) );
                });

                chunkMap.remove( chunkPos );
                chunkMap.put( chunkPos, blockMap );
            }
        }
    }

    public static void handleChunkDataSave( ChunkDataEvent.Save event )
    {
        ResourceLocation dimResLoc = event.getWorld().getDimension().getType().getRegistryName();
        if( placedMap.containsKey( dimResLoc ) )
        {
            ChunkPos chunkPos = event.getChunk().getPos();
            if( placedMap.get( dimResLoc ).containsKey( chunkPos ) )
            {
                NBTTagCompound levelNBT = (NBTTagCompound) event.getData().get( "Level" );
                if( levelNBT == null )
                    return;

                NBTTagCompound newPlacedNBT = new NBTTagCompound();
                NBTTagCompound insidesNBT;

                int i = 0;

                for( Map.Entry<BlockPos, UUID> entry : placedMap.get( dimResLoc ).get( chunkPos ).entrySet() )
                {
                    insidesNBT = new NBTTagCompound();
                    insidesNBT.setTag( "pos", NBTUtil.writeBlockPos( entry.getKey() ) );
                    insidesNBT.setTag( "UUID", NBTUtil.writeUniqueId( entry.getValue() ) );
                    newPlacedNBT.setTag( i++ + "", insidesNBT );
                }

                levelNBT.setTag( "placedPos", newPlacedNBT );
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

//        System.out.println( chunkMap.size() );
//        System.out.println( blockMap.size() );
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

//        System.out.println( chunkMap.size() );
//        System.out.println( blockMap.size() );
    }

    public static UUID checkPos(World world, BlockPos pos )
    {
        return checkPos( world.getWorld().getDimension().getType().getRegistryName(), pos );
    }

    public static UUID checkPos( ResourceLocation dimResLoc, BlockPos blockPos )
    {
        return placedMap.getOrDefault( dimResLoc, new HashMap<>() ).getOrDefault( new ChunkPos( blockPos ), new HashMap<>() ).get( blockPos );
    }
}
