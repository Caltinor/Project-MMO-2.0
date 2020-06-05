package harmonised.pmmo.events;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.event.world.ChunkDataEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChunkDataHandler
{
    private static Map<ResourceLocation, Map<ChunkPos, Map<BlockPos, UUID>>> placedMap = new HashMap<>();
    private static Map<ChunkPos, Map<BlockPos, UUID>> chunkMap;
    private static Map<BlockPos, UUID> blockMap;

    public static void handleChunkDataLoad( ChunkDataEvent.Load event )
    {
    }

    public static void handleChunkDataSave( ChunkDataEvent.Save event )
    {
        System.out.println( ( (CompoundNBT) event.getData().get( "Level" ) ).contains( "test" ) );
        ( (CompoundNBT) event.getData().get( "Level" ) ).put( "test", new CompoundNBT() );
        System.out.println( ( (CompoundNBT) event.getData().get( "Level" ) ).contains( "test" ) );

//        ResourceLocation dimResLoc = event.getChunk().getWorldForge().getDimension().getType().getRegistryName();
//        if( placedMap.containsKey( dimResLoc ) )
//        {
//            ChunkPos chunkPos = event.getChunk().getPos();
//            if( placedMap.get( dimResLoc ).containsKey( chunkPos ) )
//            {
//                CompoundNBT levelNBT = ( (CompoundNBT) event.getData().get( "Level" ) );
//                if( levelNBT == null )
//                    return;
//
//                CompoundNBT newPlacedNBT = new CompoundNBT();
//
//                int i = 0;
//
//                for( Map.Entry<BlockPos, UUID> entry : placedMap.get( dimResLoc ).get( chunkPos ).entrySet() )
//                {
//                    newPlacedNBT = new CompoundNBT();
//                    newPlacedNBT.put( "pos", NBTUtil.writeBlockPos( entry.getKey() ) );
//                    newPlacedNBT.put( "UUID", NBTUtil.writeUniqueId( entry.getValue() ) );
//                    newPlacedNBT.put( i++ + "", newPlacedNBT );
//                }
//
//                levelNBT.put( "placedPos", newPlacedNBT );
//
//                System.out.println( levelNBT.get( "placedPos" ) );
//            }
//        }


    }

    public static void addPos( ResourceLocation dimResLoc, BlockPos blockPos, UUID uuid )
    {
        ChunkPos chunkPos = new ChunkPos( blockPos );

        if( !placedMap.containsKey( dimResLoc ) )
            placedMap.put( dimResLoc, new HashMap<>() );

        chunkMap = placedMap.get( dimResLoc );

        if( !chunkMap.containsKey( chunkPos ) )
            chunkMap.put( chunkPos, new HashMap<>() );

        blockMap = chunkMap.get( chunkPos );

        blockMap.put( blockPos, uuid );

        System.out.println( chunkMap.size() );
        System.out.println( blockMap.size() );
    }

    public static void delPos( ResourceLocation dimResLoc, BlockPos blockPos )
    {
        ChunkPos chunkPos = new ChunkPos( blockPos );

        if( !placedMap.containsKey( dimResLoc ) )
            placedMap.put( dimResLoc, new HashMap<>() );

        chunkMap = placedMap.get( dimResLoc );

        if( !chunkMap.containsKey( chunkPos ) )
            chunkMap.put( chunkPos, new HashMap<>() );

        blockMap = chunkMap.get( chunkPos );

        blockMap.remove( blockPos );

        System.out.println( chunkMap.size() );
        System.out.println( blockMap.size() );
    }
}
