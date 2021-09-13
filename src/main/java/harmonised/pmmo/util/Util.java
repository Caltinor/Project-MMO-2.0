package harmonised.pmmo.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.Map;

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
        if( inLow == inHigh )
            return outHigh;
        else
            return ( (input - inLow) / (inHigh - inLow) ) * (outHigh - outLow) + outLow;
    }

    public static Vec3 getMidVec( Vec3 v1, Vec3 v2 )
    {
        return new Vec3( (v1.x + v2.x)/2, (v1.y + v2.y)/2, (v1.z + v2.z)/2 );
    }

    public static double getDistance( BlockPos p1, BlockPos p2 )
    {
        return getDistance( new Vec3( p1.getX(), p1.getY(), p1.getZ() ), new Vec3( p2.getX(), p2.getY(), p2.getZ() ) );
    }
    public static double getDistance( Vec3 p1, Vec3 p2 )
    {
        return Math.sqrt( Math.pow( p2.x - p1.x, 2 ) + Math.pow( p2.y - p1.y, 2 ) + Math.pow( p2.z - p1.z, 2 ) );
    }

    public static double getHorizontalDistance( Vec3 p1, Vec3 p2 )
    {
        return Math.sqrt( Math.pow( p2.x - p1.x, 2 ) + Math.pow( p2.z - p1.z, 2 ) );
    }

    public static <T> T orDefault( T input, T defaultTo )
    {
        return input == null ? defaultTo : input;
    }

    public static void binaryPrint( byte input )
    {
        StringBuilder msg = new StringBuilder();
        for( int i = 7; i >= 0; i-- )
        {
            msg.append( (1 << i & input) > 0 ? 1 : 0 );
        }
        System.out.println( msg );
    }

    public static void binaryPrint( int input )
    {
        StringBuilder msg = new StringBuilder();
        for( int i = 0; i < 4; i++ )
        {
            for( int j = 7; j >= 0; j-- )
            {
                msg.append( (1 << i*8 + j & input) > 0 ? 1 : 0 );
            }
            msg.append( " " );
        }
        System.out.println( msg );
    }

    public static Vec3 blockPosToVector( BlockPos pos )
    {
        return new Vec3( pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5 );
    }

    public static int hueToRGB( float hue, float saturation, float brightness )
    {
        float r = 0, g = 0, b = 0;

        float chroma = brightness * saturation;
        float hue1 = hue/60F;
        float x = chroma * (1- Math.abs((hue1 % 2) - 1));
        switch( (int) hue1 )
        {
            case 0:
                r = chroma;
                g = x;
                b = 0;
            break;

            case 1:
                r = x;
                g = chroma;
                b = 0;
                break;

            case 2:
                r = 0;
                g = chroma;
                b = x;
                break;

            case 3:
                r = 0;
                g = x;
                b = chroma;
                break;

            case 4:
                r = x;
                g = 0;
                b = chroma;
                break;

            case 5:
                r = chroma;
                g = 0;
                b = x;
                break;
        }

        float m = brightness - chroma;
        int r1 = (int) ((r + m) * 255);
        int g1 = (int) ((g + m) * 255);
        int b1 = (int) ((b + m) * 255);
        return r1 << 16 | b1 << 8 | g1;
    }

    public static <T> boolean mapIsAnyAbove1( Map<T, Double> input )
    {
        for( Map.Entry<T, Double> entry : input.entrySet() )
        {
            if( entry.getValue() > 1 )
                return true;
        }
        return false;
    }

    public static <T> boolean mapIsAnyAbove1String( Map<String, Map<T, Double>> input, String key )
    {
        if( input != null && input.containsKey( key ) )
        {
            for( Map.Entry<T, Double> entry : input.get( key ).entrySet() )
            {
                if( entry.getValue() > 1 )
                    return true;
            }
        }
        return false;
    }

    public static boolean isProduction()
    {
        return FMLEnvironment.production;
    }
}
