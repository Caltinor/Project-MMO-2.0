package harmonised.pmmo.skills;

import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlacedBlocks
{
	public static final HashMap<Integer, HashSet<Long>> placedBlockMap = new HashMap<Integer, HashSet<Long>>();

	public static void removeOre( World world, BlockPos pos )
	{
		int dimension = world.getDimension().getType().getId();
		if (!placedBlockMap.containsKey( dimension ))
	        return;
		
		Long posKey = pos.toLong();
	    HashSet<Long> dimBlockSet = placedBlockMap.get( dimension );
	    
		if( dimBlockSet.contains( posKey ) )
		{
	    	dimBlockSet.remove( posKey );
//			System.out.println( "ORE REMOVED" );
		}
	}
	
	public static boolean isPlayerPlaced(World world, BlockPos pos)
	{
		int dimension = world.getDimension().getType().getId();
	    if (!placedBlockMap.containsKey( dimension ))
	        return false;

	    Long posKey = pos.toLong();
	    HashSet<Long> dimBlockSet = placedBlockMap.get( dimension );
	    
	    if( dimBlockSet.contains( posKey ) )
	    {
//	    	dimBlockSet.remove( posKey );
	    	return true;
	    }
	    else
	    	return false;
	    
	}
	
	public static void orePlaced( World world, BlockPos pos )
	{
		int dimension = world.getDimension().getType().getId();
		
		if( !placedBlockMap.containsKey( dimension ) )
			placedBlockMap.put( dimension, new HashSet<Long>() );
		
		placedBlockMap.get( dimension ).add( pos.toLong() );
//		System.out.println( "ORE PLACED" );
	}
}
