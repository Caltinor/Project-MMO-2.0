package harmonised.pmmo.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class Util
{
    public static double mapCapped( double input, double inLow, double inHigh, double outLow, double outHigh )
    {
        if( input < inLow )
            input = inLow;
        if( input > inHigh )
            input = inHigh;

        return map( input, inLow, inHigh, outLow, outHigh );
    }

    public static double map( double input, double inLow, double inHigh, double outLow, double outHigh )
    {
        return ( (input - inLow) / (inHigh - inLow) ) * (outHigh - outLow) + outLow;
    }

    public static Vector3d getMidVec( Vector3d v1, Vector3d v2 )
    {
        return new Vector3d( (v1.x + v2.x)/2, (v1.y + v2.y)/2, (v1.z + v2.z)/2 );
    }

    public static double getDistance( BlockPos p1, BlockPos p2 )
    {
        return getDistance( new Vector3d( p1.getX(), p1.getY(), p1.getZ() ), new Vector3d( p2.getX(), p2.getY(), p2.getZ() ) );
    }
    public static double getDistance( Vector3d p1, Vector3d p2 )
    {
        return Math.sqrt( Math.pow( p2.x - p1.x, 2 ) + Math.pow( p2.y - p1.y, 2 ) + Math.pow( p2.z - p1.z, 2 ) );
    }
}
